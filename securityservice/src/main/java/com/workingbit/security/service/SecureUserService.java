package com.workingbit.security.service;

import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumAuthority;
import com.workingbit.share.util.SecureUtils;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.util.Optional;
import java.util.Set;

import static com.workingbit.security.SecurityEmbedded.appProperties;
import static com.workingbit.security.SecurityEmbedded.secureUserDao;
import static com.workingbit.share.common.RequestConstants.SESSION_LENGTH;
import static com.workingbit.share.util.Utils.*;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
public class SecureUserService {

  private Logger logger = LoggerFactory.getLogger(SecureUserService.class);

  public Optional<AuthUser> register(UserCredentials userCredentials) {
    try {
      String username = userCredentials.getUsername();
      boolean duplicateName = secureUserDao.findByUsername(username).isPresent();
      if (duplicateName) {
        return Optional.empty();
      }
      SecureUser secureUser = new SecureUser();
      Utils.setRandomIdAndCreatedAt(secureUser);
      secureUser.setUsername(username);

      secureUser.setTokenLength(appProperties.tokenLength());

      // hash credentials
      hashCredentials(userCredentials, secureUser);

      // encrypt random token
      TokenPair accessToken = getAccessToken(secureUser);

      // save encrypted token and userSession
      String userSession = getUserSession();
      secureUser.setSecureToken(accessToken.secureToken);
      secureUser.setAccessToken(accessToken.accessToken);
      secureUser.setUserSession(userSession);
      secureUser.addAuthority(EnumAuthority.AUTHOR);
      secureUserDao.save(secureUser);

      // send access token and userSession
      AuthUser authUser = AuthUser.simpleUser(secureUser.getDomainId(), username, accessToken.accessToken, userSession, secureUser.getAuthorities());
      return Optional.of(authUser);
    } catch (Exception e) {
      logger.error("UNREGISTERED: " + userCredentials, e.getMessage());
      return Optional.empty();
    }
  }

  public Optional<AuthUser> authorize(UserCredentials userCredentials) {
    String username = userCredentials.getUsername();
    return secureUserDao.findByUsername(username)
        .map(secureUser -> {
          try {
            // hash credentials
            String credentials = userCredentials.getCredentials();
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
              DomainId userId = secureUser.getDomainId();
              Set<EnumAuthority> authorities = secureUser.getAuthorities();
              AuthUser authUser = AuthUser.simpleUser(userId, username, accessToken.accessToken, userSession, authorities);
              logger.info("AUTHORIZED: " + authUser);
              return authUser;
            }
          } catch (Exception e) {
            logger.error("UNAUTHORIZED: " + userCredentials, e.getMessage());
          }
          return null;
        });
  }

  public Optional<AuthUser> authenticate(AuthUser authUser) {
    if (authUser.getAuthorities().contains(EnumAuthority.ANONYMOUS)) {
      return Optional.empty();
    }
    String session = authUser.getUserSession();
    String accessToken = authUser.getAccessToken();
    Optional<SecureUser> secureUserOptional = secureUserDao.findBySession(session);
    return secureUserOptional.map((secureUser) -> {
      boolean isAuth = isAuthed(accessToken, secureUser);
      if (isAuth) {
        if (!authUser.getAuthorities().contains(EnumAuthority.INTERNAL)) {
          TokenPair updatedAccessToken = getAccessToken(secureUser);
          secureUser.setAccessToken(updatedAccessToken.accessToken);
          secureUser.setSecureToken(updatedAccessToken.secureToken);
          secureUserDao.save(secureUser);
          authUser.setTimestamp(getTimestamp());
          authUser.setAccessToken(updatedAccessToken.accessToken);
        }

        authUser.setUsername(secureUser.getUsername());
        authUser.setUserId(secureUser.getDomainId());
        authUser.setAuthorities(secureUser.getAuthorities());
        logger.info("AUTHENTICATED: " + authUser);
        return authUser;
      }
      logger.info("Unsuccessful authentication attempt " + authUser);
      return null;
    });
  }

  public Optional<UserInfo> userInfo(AuthUser authUser) {
    String accessToken = authUser.getAccessToken();
    Optional<SecureUser> secureUserOptional = getSecureUserByIdOrSession(authUser);
    return secureUserOptional.map((secureUser) -> {
      boolean isAuth = isAuthed(accessToken, secureUser);
      if (isAuth) {
        return extractUserInfo(secureUser);
      }
      return null;
    });
  }

  public Optional<UserInfo> saveUserInfo(UserInfo userInfo, AuthUser authUser) {
    String accessToken = authUser.getAccessToken();
    Optional<SecureUser> secureUserOptional = getSecureUserByIdOrSession(authUser);
    return secureUserOptional.map((secureUser) -> {
      boolean isAuth = isAuthed(accessToken, secureUser);
      boolean canUpdate = userInfo.getUserId().equals(secureUser.getDomainId())
          || secureUser.getAuthorities().contains(EnumAuthority.ADMIN);
      boolean superAuthority = false;
      if (StringUtils.isNotBlank(authUser.getSuperHash())) {
        superAuthority = authUser.getSuperHash()
            .equals(getSuperHash());
      }
      if (isAuth && canUpdate || superAuthority) {
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

  private Optional<SecureUser> getSecureUserByIdOrSession(AuthUser authUser) {
    Optional<SecureUser> secureUserOptional;
    if (authUser.getUserId() != null) {
      secureUserOptional = secureUserDao.findById(authUser.getUserId());
    } else {
      secureUserOptional = secureUserDao.findBySession(authUser.getUserSession());
    }
    return secureUserOptional;
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
    return getRandomString(SESSION_LENGTH);
  }

  private void hashCredentials(UserCredentials userCredentials, SecureUser secureUser) {
    String credentials = userCredentials.getCredentials();
    String salt = getRandomString20();
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
    String secureToken = getRandomString(secureUser.getTokenLength());
    int encLength = 16;
    String initVector = getRandomString(encLength);
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

  private UserInfo updateUserInfo(SecureUser secureUser, UserInfo userInfo) {
    secureUser.setAuthorities(userInfo.getAuthorities());
    secureUserDao.save(secureUser);
    return userInfo;
  }

  private UserInfo extractUserInfo(SecureUser secureUser) {
    return new UserInfo(secureUser.getDomainId(), secureUser.getUsername(), secureUser.getAuthorities());
  }

  private String getSuperHash() {
    return SecureUtils.digest(System.getenv(appProperties.superHashEnvName()));
  }
}
