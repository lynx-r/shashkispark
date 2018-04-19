package com.workingbit.security.service;

import com.workingbit.share.model.*;
import com.workingbit.share.util.SecureUtils;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static com.workingbit.security.SecurityApplication.appProperties;
import static com.workingbit.security.SecurityApplication.secureUserDao;
import static com.workingbit.share.util.Utils.getRandomString;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
public class SecureUserService {

  private Logger logger = LoggerFactory.getLogger(SecureUserService.class);

  public Optional<AuthUser> register(RegisterUser registerUser, Optional<AuthUser> token) {
    return token.map(authUser -> {
      try {
        String username = registerUser.getUsername();
        boolean duplicateName = secureUserDao.findByUsername(username).isPresent();
        if (duplicateName) {
          return null;
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
        secureUser.setRole(EnumSecureRole.AUTHOR);
        secureUserDao.save(secureUser);

        // send access token and userSession
        logger.info("REGISTER: " + username + " with " + accessToken);
        return new AuthUser(secureUser.getId(), username, accessToken.accessToken, userSession, secureUser.getRole());
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    });
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
              EnumSecureRole role = secureUser.getRole();

              logger.info("AUTHORIZE:" + username + " with " + accessToken);
              return new AuthUser(userId, username, accessToken.accessToken, userSession, role);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          return null;
        });
  }

  public Optional<AuthUser> authenticate(AuthUser authUser) {
    String session = authUser.getUserSession();
    String accessToken = authUser.getAccessToken();
    Optional<SecureUser> secureUserOptional = secureUserDao.findBySession(session);
    return secureUserOptional.map((secureUser) -> {
      boolean isAuth = isAuthed(accessToken, secureUser);
      if (isAuth) {
        TokenPair updatedAccessToken = getAccessToken(secureUser);
        secureUser.setAccessToken(updatedAccessToken.accessToken);
        secureUser.setSecureToken(updatedAccessToken.secureToken);
        secureUserDao.save(secureUser);

        authUser.setAccessToken(updatedAccessToken.accessToken);
        authUser.setUsername(secureUser.getUsername());
        authUser.setUserId(secureUser.getId());
        authUser.setRole(secureUser.getRole());

        logger.info("AUTHENTICATE: " + secureUser.getUsername() + " with " + updatedAccessToken);
        return authUser;
      }
      logger.info("AUTHENTICATE FAILED: " + secureUser);
      return null;
    });
  }

  public Optional<UserInfo> userInfo(AuthUser authUser) {
    String accessToken = authUser.getAccessToken();
    Optional<SecureUser> secureUserOptional = secureUserDao.findById(authUser.getUserId());
    return secureUserOptional.map((secureUser) -> {
      boolean isAuth = isAuthed(accessToken, secureUser);
      if (isAuth) {
        logger.info("USER_INFO: " + secureUser.getUsername());
        return new UserInfo(secureUser.getUsername());
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
        secureUser.setKey("");
        secureUser.setTokenLength(0);
        secureUser.setInitVector("");
        secureUser.setUserSession("");
        secureUserDao.save(secureUser);
        logger.info("LOGOUT: " + secureUser.getUsername());
      }
      return AuthUser.anonymous();
    });
  }

  private boolean isAuthed(String accessToken, SecureUser secureUser) {
    String key = secureUser.getKey();
    String initVector = secureUser.getInitVector();
    String tokenDecrypted = SecureUtils.decrypt(key, initVector, accessToken);
    return secureUser.getSecureToken().equals(tokenDecrypted);
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
