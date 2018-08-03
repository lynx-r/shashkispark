package com.workingbit.security.service;

import com.workingbit.share.common.AppMessages;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumAuthority;
import com.workingbit.share.util.SecureUtils;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static com.workingbit.security.SecurityEmbedded.*;
import static com.workingbit.share.common.RequestConstants.SESSION_LENGTH;
import static com.workingbit.share.util.Utils.getRandomString;
import static com.workingbit.share.util.Utils.getTimestamp;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
public class SecureUserService {

  private Logger logger = LoggerFactory.getLogger(SecureUserService.class);

  public AuthUser preAuthorize(UserCredentials userCredentials) {
    return passwordService.findByEmail(userCredentials.getEmail())
        .map(secureAuth -> {
          // используем конкретную сигму пользователя
          String data = secureAuth.getEmail() + appProperties.domain() + secureAuth.getSigma();
          String salt = SecureUtils.digest(data);
          return AuthUser.authRequest(userCredentials.getEmail(), salt, appProperties.cost(), appProperties.misc());
        })
        .orElseGet(() -> {
          // используем системную сигму
          String data = userCredentials.getEmail() + appProperties.domain() + appProperties.sysSigma();
          String salt = SecureUtils.digest(data);
          return AuthUser.authRequest(userCredentials.getEmail(), salt, appProperties.cost(), appProperties.misc());
        });
  }

  public AuthUser preRegister(UserCredentials userCredentials) {
    String email = userCredentials.getEmail();
    SecureAuth secureAuth = new SecureAuth();
    secureAuth.setUserId(DomainId.getRandomID());
    secureAuth.setGroupId(Utils.getRandomString7());
    secureAuth.setEmail(email);

    secureAuth.setTokenLength(appProperties.tokenLength());

    String sigma = Utils.getRandomString32();
    String data = email + appProperties.domain() + sigma;
    String salt = SecureUtils.digest(data);
    secureAuth.setSigma(sigma);
    secureAuth.addAuthority(EnumAuthority.AUTHOR);
//    secureAuth.setMisc(appProperties.misc());
//    secureAuth.setCost(appProperties.cost());

    passwordService.registerUser(secureAuth);
    return AuthUser.authRequest(userCredentials.getEmail(), salt, appProperties.cost(), appProperties.misc());
  }

  @NotNull
  public AuthUser register(@NotNull UserCredentials registeredUser) {
    String email = registeredUser.getEmail();
    return passwordService.findByEmail(email)
        .map(secureAuth -> {
          var secureAuthTokens = getUpdateSecureAuthTokens(secureAuth);

          String userSession = getUserSession();
          secureAuthTokens.setUserSession(userSession);

          String passwordHash = registeredUser.getPasswordHash();
          passwordHash = SecureUtils.digest(passwordHash);
          secureAuthTokens.setPasswordHash(passwordHash);

          secureAuthTokens.addAuthority(EnumAuthority.AUTHOR);
          passwordService.save(secureAuthTokens);

          SiteUserInfo siteUserInfo = new SiteUserInfo();
          siteUserInfo.setDomainId(secureAuthTokens.getUserId());
          siteUserInfo.setEmail(email);
          siteUserInfo.setAuthorities(secureAuthTokens.getAuthorities());
          siteUserInfo.setUpdatedAt(LocalDateTime.now());
          siteUserInfoDao.save(siteUserInfo);

          String contentHtml = String.format("Зарегистрировался новый пользователь: %s", siteUserInfo.getEmail());
          String subject = "Зарегистрировался новый пользователь";
          emailUtils.mailAdmin(subject, contentHtml);
          return AuthUser.simpleUser(secureAuthTokens.getUserId(), email, secureAuthTokens.getAccessToken(), userSession, siteUserInfo.getAuthorities());
        })
        .orElseThrow(RequestException::forbidden);
  }

  @NotNull
  public AuthUser authorize(@NotNull UserCredentials userCredentials) {
    String email = userCredentials.getEmail();
    return passwordService.findByEmail(email)
        .map(secureAuth -> {
          String passwordHash = userCredentials.getPasswordHash();
          passwordHash = SecureUtils.digest(passwordHash);

          if (passwordHash.equals(secureAuth.getPasswordHash())) {
            SiteUserInfo siteUserInfo = siteUserInfoDao.findByEmail(email);
            // encrypt random token
            SecureAuth secureAuthTokens = getUpdateSecureAuthTokens(secureAuth);
            passwordService.save(secureAuthTokens);

            // save encrypted token and userSession
            String userSession = getUserSession();
            secureAuth.setUserSession(userSession);

            // send access token and userSession
            DomainId userId = secureAuth.getUserId();
            Set<EnumAuthority> authorities = siteUserInfo.getAuthorities();
            AuthUser authUser = AuthUser.simpleUser(userId, email, secureAuthTokens.getAccessToken(), userSession, authorities);
            logger.info("AUTHORIZED: " + authUser);
            return authUser;
          }
          return null;
        })
        .orElseThrow(RequestException::forbidden);
  }

  @NotNull
  public AuthUser authenticate(@NotNull AuthUser authUser) {
    if (authUser.getAuthorities().contains(EnumAuthority.ANONYMOUS)) {
      throw RequestException.forbidden();
    }
    String accessToken = authUser.getAccessToken();
    return isAuthUserSecure(accessToken, authUser)
        .map(secureAuth -> {
          if (!authUser.getAuthorities().contains(EnumAuthority.INTERNAL)) {
            SecureAuth updatedAccessToken = getUpdateSecureAuthTokens(secureAuth);
            secureAuth.setAccessToken(updatedAccessToken.getAccessToken());
            secureAuth.setSecureToken(updatedAccessToken.getSecureToken());

            authUser.setTimestamp(getTimestamp());
            authUser.setAccessToken(updatedAccessToken.getAccessToken());
          }

          authUser.setEmail(secureAuth.getEmail());
          authUser.setUserId(secureAuth.getUserId());
          authUser.setAuthorities(secureAuth.getAuthorities());
          logger.info("AUTHENTICATED: " + authUser);
          return authUser;
        })
        .orElseThrow(RequestException::forbidden);
  }

  @NotNull
  public UserInfo userInfo(@NotNull AuthUser authUser) {
    String accessToken = authUser.getAccessToken();
    return isAuthUserSecure(accessToken, authUser)
        .map(secureAuth -> {
          SiteUserInfo byId = siteUserInfoDao.findById(authUser.getUserId());
          return extractUserInfo(byId, secureAuth);
        })
        .orElseThrow(RequestException::forbidden);
  }

  @NotNull
  public UserInfo saveUserInfo(@NotNull UserInfo userInfo, @NotNull AuthUser authUser) {
    String accessToken = authUser.getAccessToken();
    return isAuthUserSecure(accessToken, authUser)
        .map(secureAuth -> {
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

              if (!userBeforeSave.getEmail().equals(userInfo.getUsername())) {
                SiteUserInfo byUsername = siteUserInfoDao.findByEmail(userInfo.getUsername());
                if (byUsername == null) {
                  userBeforeSave.setEmail(userInfo.getUsername());
                  SecureAuth secureAuthUpdated = secureAuth.deepClone();
                  secureAuthUpdated.setEmail(userInfo.getUsername());
                  // todo обновлять автора в статьях
                  passwordService.save(secureAuthUpdated);
                } else {
                  throw RequestException.badRequest(ErrorMessages.USERNAME_IS_BUSY);
                }
              }
              userBeforeSave.setCreditCard(userInfo.getCreditCard());
              siteUserInfoDao.save(userBeforeSave);
              return userInfo;
            }
          }
          return null;
        })
        .orElseThrow(RequestException::forbidden);
  }

  @NotNull
  public AuthUser logout(@NotNull AuthUser authUser) {
    String accessToken = authUser.getAccessToken();
    return isAuthUserSecure(accessToken, authUser)
        .map(secureAuth -> {
          SiteUserInfo userInfo = siteUserInfoDao.findByEmail(secureAuth.getEmail());
          userInfo.setLoggedOutTime(LocalDateTime.now());
          siteUserInfoDao.save(userInfo);
          return AuthUser.anonymous();
        })
        .orElseThrow(RequestException::forbidden);
  }

  public ResultPayload resetPassword(UserCredentials credentials) {
    String email = credentials.getEmail();
    AuthUser authUser = new AuthUser();
    authUser.setEmail(email);
    return passwordService.findByEmail(email)
        .map(secureAuth -> {
          SecureAuth newSecureAuth = secureAuth.deepClone();

          String password = Utils.getRandomString7();
          credentials.setPasswordHash(password);

          String contentHtml = String.format(AppMessages.RESET_EMAIL_HTML, password);
          emailUtils.mail(secureAuth.getEmail(), credentials.getEmail(),
              AppMessages.RESET_EMAIL_SUBJECT, contentHtml, contentHtml);

          // encrypt random token
          secureAuth = getUpdateSecureAuthTokens(newSecureAuth);

          // save encrypted token and userSession
          String userSession = getUserSession();
          newSecureAuth.setUserSession(userSession);

          passwordService.save(newSecureAuth);
          return new ResultPayload(true);
        })
        .orElseThrow(RequestException::forbidden);
  }

  private Optional<SecureAuth> isAuthUserSecure(String accessToken, @NotNull AuthUser authUser) {
    return passwordService.findByEmail(authUser.getEmail())
        .map(secureAuth -> {
          String key = secureAuth.getKey();
          String initVector = secureAuth.getInitVector();
          try {
            String tokenDecrypted = SecureUtils.decrypt(key, initVector, accessToken);
            if (secureAuth.getSecureToken().equals(tokenDecrypted)) {
              return secureAuth;
            }
          } catch (@NotNull IllegalBlockSizeException | BadPaddingException e) {
            logger.warn(String.format("Unable to decrypt accessToken %s", accessToken));
          }
          return null;
        });
  }

  private String getUserSession() {
    return getRandomString(SESSION_LENGTH);
  }

  /**
   * Set params for encryption generate secure token and encrypt it
   *
   * @param secureAuth user
   * @return access and secure token
   */
  @NotNull
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
    return new UserInfo(siteUserInfo.getDomainId(), siteUserInfo.getEmail(), siteUserInfo.getCreditCard(),
        secureAuth.getAuthorities());
  }

  private String getSuperHash() {
    return SecureUtils.digest(System.getenv(appProperties.superHashEnvName()));
  }
}
