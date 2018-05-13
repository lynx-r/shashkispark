package com.workingbit.security.service;

import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.exception.DaoException;
import com.workingbit.share.exception.RequestException;
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

  public AuthUser register(UserCredentials userCredentials) {
    try {
      String username = userCredentials.getUsername();
      boolean duplicateName = true;
      try {
        secureUserDao.findByUsername(username);
      } catch (DaoException e) {
        duplicateName = false;
      }
      if (duplicateName) {
        throw RequestException.forbidden();
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
      return AuthUser.simpleUser(secureUser.getDomainId(), username, accessToken.accessToken, userSession, secureUser.getAuthorities());
    } catch (Exception e) {
      logger.error("UNREGISTERED: " + userCredentials, e.getMessage());
    }
    throw RequestException.forbidden(ErrorMessages.USERNAME_IS_BUSY);
  }

  public AuthUser authorize(UserCredentials userCredentials) {
    try {
      String username = userCredentials.getUsername();
      var secureUser = secureUserDao.findByUsername(username);
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
    } catch (DaoException e) {
      logger.error("USER NOT FOUND: " + userCredentials, e.getMessage());
    } catch (Exception e) {
      logger.error("UNAUTHORIZED: " + userCredentials, e.getMessage());
      throw RequestException.internalServerError();
    }
    throw RequestException.forbidden(ErrorMessages.INVALID_USERNAME_OR_PASSWORD);
  }

  public AuthUser authenticate(AuthUser authUser) {
    if (authUser.getAuthorities().contains(EnumAuthority.ANONYMOUS)) {
      throw RequestException.forbidden();
    }
    String session = authUser.getUserSession();
    String accessToken = authUser.getAccessToken();
    SecureUser secureUser;
    try {
      secureUser = secureUserDao.findBySession(session);
    } catch (DaoException e) {
      throw RequestException.forbidden();
    }
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
    throw RequestException.forbidden();
  }

  public UserInfo userInfo(AuthUser authUser) {
    String accessToken = authUser.getAccessToken();
    SecureUser secureUser = getSecureUserByIdOrSession(authUser);
    boolean isAuth = isAuthed(accessToken, secureUser);
    if (isAuth) {
      return extractUserInfo(secureUser);
    }
    throw RequestException.forbidden();
  }

  public UserInfo saveUserInfo(UserInfo userInfo, AuthUser authUser) {
    String accessToken = authUser.getAccessToken();
    SecureUser secureUser = getSecureUserByIdOrSession(authUser);
    boolean isAuth = isAuthed(accessToken, secureUser);
    boolean canUpdate = userInfo.getUserId().equals(secureUser.getDomainId())
        || secureUser.getAuthorities().contains(EnumAuthority.ADMIN);
    boolean superAuthority = false;
    if (StringUtils.isNotBlank(authUser.getSuperHash())) {
      superAuthority = authUser.getSuperHash()
          .equals(getSuperHash());
    }
    if (isAuth && canUpdate || superAuthority) {
      SecureUser userBeforeSave = secureUserDao.findById(userInfo.getUserId());
      if (userBeforeSave != null) {
        return updateUserInfo(userBeforeSave, userInfo);
      }
    }
    throw RequestException.forbidden();
  }

  public AuthUser logout(AuthUser authUser) {
    String session = authUser.getUserSession();
    String accessToken = authUser.getAccessToken();
    SecureUser secureUser = secureUserDao.findBySession(session);
    boolean isAuth = isAuthed(accessToken, secureUser);
    if (isAuth) {
      secureUser.setSecureToken("");
      secureUser.setAccessToken("");
      secureUser.setUserSession("");
      secureUserDao.save(secureUser);
    }
    return AuthUser.anonymous();
  }

  private SecureUser getSecureUserByIdOrSession(AuthUser authUser) {
    if (authUser.getUserId() != null) {
      return secureUserDao.findById(authUser.getUserId());
    } else {
      return secureUserDao.findBySession(authUser.getUserSession());
    }
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
