package com.workingbit.security.service;

import com.workingbit.share.model.*;
import com.workingbit.share.util.SecureUtils;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.Set;

import static com.workingbit.security.SecurityEmbedded.appProperties;
import static com.workingbit.security.SecurityEmbedded.secureUserDao;
import static com.workingbit.share.util.Utils.getRandomString;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
public class SecureUserService {

  private Logger logger = LoggerFactory.getLogger(SecureUserService.class);

  public Optional<AuthUser> register(RegisterUser registerUser) {
    try {
      String username = registerUser.getUsername();
      boolean duplicateName = secureUserDao.findByUsername(username).isPresent();
      if (duplicateName) {
        return Optional.empty();
      }
      SecureUser secureUser = new SecureUser();
      Utils.setRandomIdAndCreatedAt(secureUser);
      secureUser.setUsername(username);

      int tokenLengthInt = appProperties.tokenLength();
      secureUser.setTokenLength(tokenLengthInt);

      // hash credentials
      hashCredentials(registerUser, secureUser);

      // encrypt random token
      TokenPair accessToken = getAccessToken(secureUser);

      // save encrypted token and userSession
      String userSession = getUserSession();
      secureUser.setSecureToken(accessToken.secureToken);
      secureUser.setAccessToken(accessToken.accessToken);
      secureUser.setUserSession(userSession);
      secureUser.addRole(EnumSecureRole.AUTHOR);
      secureUserDao.save(secureUser);

      // send access token and userSession
      AuthUser authUser = new AuthUser(secureUser.getId(), username, accessToken.accessToken, userSession, 0, secureUser.getRoles());
      return Optional.of(authUser);
    } catch (Exception e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<AuthUser> authorize(RegisterUser registerUser) {
    String username = registerUser.getUsername();
    return secureUserDao.findByUsername(username)
        .map(secureUser -> {
          try {
            // hash credentials
            String credentials = registerUser.getCredentials();
            String salt = secureUser.getSalt();
            String clientDigest = SecureUtils.digest(credentials + salt);

            if (clientDigest.equals(secureUser.getDigest())) {
              // encrypt random token
              TokenPair accessToken = getAccessToken(secureUser);

              // save encrypted token and userSession
              String userSession = getUserSession();
              secureUser.setSecureToken(accessToken.secureToken);
              secureUser.setAccessToken(accessToken.accessToken);
              secureUser.setUserSession(userSession);
              secureUserDao.save(secureUser);

              // send access token and userSession
              String userId = secureUser.getId();
              Set<EnumSecureRole> roles = secureUser.getRoles();
              return new AuthUser(userId, username, accessToken.accessToken, userSession, 0, roles);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          return null;
        });
  }

  public Optional<AuthUser> authenticate(Optional<AuthUser> token) {
    return token
        .filter(authUser -> !authUser.getRoles().contains(EnumSecureRole.ANONYMOUS))
        .map(authUser -> {
          String session = authUser.getUserSession();
          String accessToken = authUser.getAccessToken();
          Optional<SecureUser> secureUserOptional = secureUserDao.findBySession(session);
          return secureUserOptional.map((secureUser) -> {
            boolean isAuth = isAuthed(accessToken, secureUser);
            if (isAuth) {
              if (!authUser.getRoles().contains(EnumSecureRole.INTERNAL)) {
                TokenPair updatedAccessToken = getAccessToken(secureUser);
                secureUser.setAccessToken(updatedAccessToken.accessToken);
                secureUser.setSecureToken(updatedAccessToken.secureToken);
                secureUserDao.save(secureUser);
                authUser.setCounter(authUser.getCounter() + 1);
                authUser.setAccessToken(updatedAccessToken.accessToken);
              }

              authUser.setUsername(secureUser.getUsername());
              authUser.setUserId(secureUser.getId());
              authUser.setRoles(secureUser.getRoles());
              return authUser;
            }
            return null;
          });
        }).orElse(Optional.of(AuthUser.anonymous()));
  }

  public Optional<UserInfo> userInfo(AuthUser authUser) {
    String accessToken = authUser.getAccessToken();
    Optional<SecureUser> secureUserOptional = secureUserDao.findById(authUser.getUserId());
    return secureUserOptional.map((secureUser) -> {
      boolean isAuth = isAuthed(accessToken, secureUser);
      if (isAuth) {
        return extractUserInfo(secureUser);
      }
      return null;
    });
  }

  public Optional<UserInfo> saveUserInfo(UserInfo userInfo, Optional<AuthUser> token) {
    if (!token.isPresent()) {
      return Optional.of(userInfo);
    }
    AuthUser authUser = token.get();
    String accessToken = authUser.getAccessToken();
    Optional<SecureUser> secureUserOptional = secureUserDao.findById(authUser.getUserId());
    return secureUserOptional.map((secureUser) -> {
      boolean isAuth = isAuthed(accessToken, secureUser);
      boolean canUpdate = userInfo.getUserId().equals(secureUser.getId())
          || secureUser.getRoles().contains(EnumSecureRole.ADMIN);
      if (isAuth && canUpdate) {
        Optional<SecureUser> userBeforeSave = secureUserDao.findById(userInfo.getUserId());
        if (userBeforeSave.isPresent()) {
          return updateUserInfo(userBeforeSave.get(), userInfo);
        }
      }
      return null;
    });
  }

  public Optional<AuthUser> logout(AuthUser authUser) {
    String session = authUser.getUserSession();
    String accessToken = authUser.getAccessToken();
    Optional<SecureUser> secureUserOptional = secureUserDao.findBySession(session);
    return secureUserOptional.map((secureUser) -> {
      boolean isAuth = isAuthed(accessToken, secureUser);
      if (isAuth) {
        secureUser.setSecureToken("");
        secureUser.setAccessToken("");
        secureUser.setUserSession("");
        secureUserDao.save(secureUser);
      }
      return AuthUser.anonymous();
    });
  }

  private UserInfo updateUserInfo(SecureUser secureUser, UserInfo userInfo) {
    secureUser.setRoles(userInfo.getRoles());
    secureUserDao.save(secureUser);
    return extractUserInfo(secureUser);
  }

  private UserInfo extractUserInfo(SecureUser secureUser) {
    return new UserInfo(secureUser.getId(), secureUser.getUsername(), secureUser.getRoles());
  }

  private boolean isAuthed(String accessToken, SecureUser secureUser) {
    String key = secureUser.getKey();
    String initVector = secureUser.getInitVector();
    try {
      String tokenDecrypted = SecureUtils.decrypt(key, initVector, accessToken);
      return secureUser.getSecureToken().equals(tokenDecrypted);
    } catch (IllegalBlockSizeException | BadPaddingException e) {
      logger.error(String.format("Unable to decrypt accessToken %s", accessToken));
      return false;
    }
  }

  private String getUserSession() {
    return Utils.getRandomString(appProperties.sessionLength());
  }

  private void hashCredentials(RegisterUser registerUser, SecureUser secureUser) throws NoSuchAlgorithmException {
    String credentials = registerUser.getCredentials();
    String salt = ":" + Utils.getRandomString(secureUser.getTokenLength());
    secureUser.setSalt(salt);
    String digest = SecureUtils.digest(credentials + salt);
    secureUser.setDigest(digest);
  }

  /**
   * Set params for encryption generate secure token and encrypt it
   *
   * @param secureUser user
   * @return access and secure token
   */
  private TokenPair getAccessToken(SecureUser secureUser) {
    String secureToken = Utils.getRandomString(secureUser.getTokenLength());
    int encLength = 16;
    String initVector = Utils.getRandomString(encLength);
    String key = getRandomString(encLength);
    secureUser.setInitVector(initVector);
    secureUser.setKey(key);
    String accessToken = SecureUtils.encrypt(key, initVector, secureToken);
    logger.info("Emit new access token: " + accessToken);
    return new TokenPair(accessToken, secureToken);
  }

  private static class TokenPair {
    String accessToken;
    String secureToken;

    TokenPair(String accessToken, String secureToken) {
      this.accessToken = accessToken;
      this.secureToken = secureToken;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("accessToken", accessToken)
          .toString();
    }
  }
}
