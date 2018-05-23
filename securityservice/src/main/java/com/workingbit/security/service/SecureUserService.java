package com.workingbit.security.service;

import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.exception.DaoException;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumAuthority;
import com.workingbit.share.util.SecureUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.time.LocalDateTime;
import java.util.Set;

import static com.workingbit.orchestrate.OrchestrateModule.orchestralService;
import static com.workingbit.security.SecurityEmbedded.appProperties;
import static com.workingbit.security.SecurityEmbedded.siteUserInfoDao;
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
        siteUserInfoDao.findByUsername(username);
      } catch (DaoException e) {
        duplicateName = false;
      }
      if (duplicateName) {
        throw RequestException.forbidden();
      }
      SecureAuth secureAuth = new SecureAuth();
      secureAuth.setUserId(DomainId.getRandomID());
      secureAuth.setUsername(username);
      secureAuth.setTokenLength(appProperties.tokenLength());

      // hash credentials
      hashCredentials(userCredentials, secureAuth);

      // encrypt random token
      secureAuth = getUpdateSecureAuthTokens(secureAuth);

      // save encrypted token and userSession
      String userSession = getUserSession();
      secureAuth.setUserSession(userSession);
      secureAuth.addAuthority(EnumAuthority.AUTHOR);

      SiteUserInfo siteUserInfo = new SiteUserInfo();
      siteUserInfo.setDomainId(secureAuth.getUserId());
      siteUserInfo.setUsername(username);
      siteUserInfo.setCreditCard(userCredentials.getCreditCard());
      siteUserInfo.setUpdatedAt(LocalDateTime.now());
      siteUserInfoDao.save(siteUserInfo);

      // send access token and userSession
      orchestralService.cacheSecureAuth(secureAuth);
      return AuthUser.simpleUser(secureAuth.getUserId(), username, secureAuth.getAccessToken(), userSession, secureAuth.getAuthorities());
    } catch (Exception e) {
      logger.error("UNREGISTERED: " + userCredentials, e.getMessage());
    }
    throw RequestException.forbidden(ErrorMessages.USERNAME_IS_BUSY);
  }

  public AuthUser authorize(UserCredentials userCredentials) {
    String username = userCredentials.getUsername();
    SecureAuth secureAuth = orchestralService.getSecureAuthUsername(username);
    if (secureAuth == null) {
      throw RequestException.forbidden(ErrorMessages.INVALID_USERNAME_OR_PASSWORD);
    }
    try {
      // hash credentials
      String credentials = userCredentials.getCredentials();
      String salt = secureAuth.getSalt();
      String clientDigest = SecureUtils.digest(credentials + salt);

      if (clientDigest.equals(secureAuth.getDigest()) && !EnumAuthority.isBanned(secureAuth.getAuthorities())) {
        // encrypt random token
        SecureAuth accessToken = getUpdateSecureAuthTokens(secureAuth);

        // save encrypted token and userSession
        String userSession = getUserSession();
        secureAuth.setUserSession(userSession);

        // send access token and userSession
        DomainId userId = secureAuth.getUserId();
        Set<EnumAuthority> authorities = secureAuth.getAuthorities();
        AuthUser authUser = AuthUser.simpleUser(userId, username, accessToken.getAccessToken(), userSession, authorities);
        orchestralService.cacheSecureAuth(secureAuth);
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
    String accessToken = authUser.getAccessToken();
    SecureAuth secureAuth = isAuthUserSecure(accessToken, authUser);
    if (secureAuth != null) {
      if (!authUser.getAuthorities().contains(EnumAuthority.INTERNAL)) {
        SecureAuth updatedAccessToken = getUpdateSecureAuthTokens(secureAuth);
        secureAuth.setAccessToken(updatedAccessToken.getAccessToken());
        secureAuth.setSecureToken(updatedAccessToken.getSecureToken());
        orchestralService.cacheSecureAuth(secureAuth);

        authUser.setTimestamp(getTimestamp());
        authUser.setAccessToken(updatedAccessToken.getAccessToken());
      }

      authUser.setUsername(secureAuth.getUsername());
      authUser.setUserId(secureAuth.getUserId());
      authUser.setAuthorities(secureAuth.getAuthorities());
      logger.info("AUTHENTICATED: " + authUser);
      return authUser;
    }
    logger.info("Unsuccessful authentication attempt " + authUser);
    throw RequestException.forbidden();
  }

  public UserInfo userInfo(AuthUser authUser) {
    String accessToken = authUser.getAccessToken();
    SecureAuth secureAuth = isAuthUserSecure(accessToken, authUser);
    if (secureAuth != null) {
      SiteUserInfo byId = siteUserInfoDao.findById(authUser.getUserId());
      return extractUserInfo(byId, secureAuth);
    }
    throw RequestException.forbidden();
  }

  public UserInfo saveUserInfo(UserInfo userInfo, AuthUser authUser) {
    String accessToken = authUser.getAccessToken();
    SecureAuth secureAuth = isAuthUserSecure(accessToken, authUser);
    if (secureAuth == null) {
      throw RequestException.forbidden(ErrorMessages.UNABLE_TO_AUTHENTICATE);
    }
    SiteUserInfo byId = siteUserInfoDao.findById(authUser.getUserId());
    boolean canUpdate = userInfo.getUserId().equals(byId.getDomainId())
        || secureAuth.getAuthorities().contains(EnumAuthority.ADMIN);
    boolean superAuthority = false;
    if (StringUtils.isNotBlank(authUser.getSuperHash())) {
      superAuthority = authUser.getSuperHash()
          .equals(getSuperHash());
    }
    if (canUpdate || superAuthority) {
      SiteUserInfo userBeforeSave = siteUserInfoDao.findById(userInfo.getUserId());
      if (userBeforeSave != null) {
        secureAuth.setAuthorities(userInfo.getAuthorities());
        orchestralService.cacheSecureAuth(secureAuth);

        if (!userBeforeSave.getUsername().equals(userInfo.getUsername())) {
          SiteUserInfo byUsername = siteUserInfoDao.findByUsername(userInfo.getUsername());
          if (byUsername == null) {
            userBeforeSave.setUsername(userInfo.getUsername());
          } else {
            throw RequestException.badRequest(ErrorMessages.USERNAME_IS_BUSY);
          }
        }
        userBeforeSave.setCreditCard(userInfo.getCreditCard());
        siteUserInfoDao.save(userBeforeSave);
        return userInfo;
      }
    }
    throw RequestException.forbidden();
  }

  public AuthUser logout(AuthUser authUser) {
    String accessToken = authUser.getAccessToken();
    SecureAuth secureAuth = isAuthUserSecure(accessToken, authUser);
    if (secureAuth != null) {
      orchestralService.removeSecureAuth(authUser);
    }
    return AuthUser.anonymous();
  }

  private SecureAuth isAuthUserSecure(String accessToken, AuthUser authUser) {
    SecureAuth secureAuth = getSecureAuth(authUser);
    if (secureAuth == null) {
      return null;
    }
    String key = secureAuth.getKey();
    String initVector = secureAuth.getInitVector();
    try {
      String tokenDecrypted = SecureUtils.decrypt(key, initVector, accessToken);
      if (secureAuth.getSecureToken().equals(tokenDecrypted)) {
        return secureAuth;
      }
    } catch (IllegalBlockSizeException | BadPaddingException e) {
      logger.error(String.format("Unable to decrypt accessToken %s", accessToken));
    }
    return null;
  }

  private SecureAuth getSecureAuth(AuthUser authUser) {
    SecureAuth secureAuth = null;
    if (StringUtils.isNotBlank(authUser.getUserSession())) {
      secureAuth = orchestralService.getSecureAuth(authUser.getUserSession());
      if (secureAuth == null && StringUtils.isNotBlank(authUser.getUsername())) {
        secureAuth = orchestralService.getSecureAuthUsername(authUser.getUsername());
        if (secureAuth == null) {
          throw RequestException.forbidden("ILLEGAL ACCESS");
        }
      }
    }
    return secureAuth;
  }

  private String getUserSession() {
    return getRandomString(SESSION_LENGTH);
  }

  private void hashCredentials(UserCredentials userCredentials, SecureAuth secureUser) {
    String credentials = userCredentials.getCredentials();
    String salt = getRandomString20();
    secureUser.setSalt(salt);
    String digest = SecureUtils.digest(credentials + salt);
    secureUser.setDigest(digest);
  }

  /**
   * Set params for encryption generate secure token and encrypt it
   *
   * @param secureAuth user
   * @return access and secure token
   */
  private SecureAuth getUpdateSecureAuthTokens(SecureAuth secureAuth) {
    String secureToken = getRandomString(secureAuth.getTokenLength());
    int encLength = 16;
    String initVector = getRandomString(encLength);
    String key = getRandomString(encLength);
    secureAuth.setInitVector(initVector);
    secureAuth.setKey(key);
    String accessToken = SecureUtils.encrypt(key, initVector, secureToken);
    logger.info("Emit new access token: " + accessToken);
    secureAuth.setAccessToken(accessToken);
    secureAuth.setSecureToken(secureToken);
    return secureAuth;
  }

  private UserInfo extractUserInfo(SiteUserInfo siteUserInfo, SecureAuth secureAuth) {
    return new UserInfo(siteUserInfo.getDomainId(), siteUserInfo.getUsername(), siteUserInfo.getCreditCard(),
        secureAuth.getAuthorities());
  }

  private String getSuperHash() {
    return SecureUtils.digest(System.getenv(appProperties.superHashEnvName()));
  }
}
