package com.workingbit.security.service;

import com.workingbit.share.model.*;
import com.workingbit.share.util.SecureUtils;
import com.workingbit.share.util.Utils;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static com.workingbit.security.SecurityApplication.appProperties;
import static com.workingbit.security.SecurityApplication.secureUserDao;
import static com.workingbit.share.util.Utils.getRandomString;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
public class SecureUserService {

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
        hashCredentials(registerUser, tokenLengthInt, secureUser);

        // encrypt random token
        String accessToken = getAccessToken(secureUser, tokenLengthInt);

        // save encrypted token and userSession
        String userSession = getUserSession();
        secureUser.setAccessToken(accessToken);
        secureUser.setUserSession(userSession);
        secureUser.setRole(EnumSecureRole.AUTHOR);
        secureUserDao.save(secureUser);

        // send access token and userSession
        return new AuthUser(secureUser.getId(), username, accessToken, userSession, secureUser.getRole());
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
            int tokenLengthInt = secureUser.getTokenLength();
            // hash credentials
            String credentials = registerUser.getCredentials();
            String salt = secureUser.getSalt();
            String clientDigest = SecureUtils.digest(credentials + salt);

            if (clientDigest.equals(secureUser.getDigest())) {
              // encrypt random token
              String accessToken = getAccessToken(secureUser, tokenLengthInt);

              // save encrypted token and userSession
              String userSession = getUserSession();
              secureUser.setAccessToken(accessToken);
              secureUser.setUserSession(userSession);
              secureUserDao.save(secureUser);

              // send access token and userSession
              String userId = secureUser.getId();
              EnumSecureRole role = secureUser.getRole();
              return new AuthUser(userId, username, accessToken, userSession, role);
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
      String key = secureUser.getKey();
      String initVector = secureUser.getInitVector();
      String tokenDecrypted = SecureUtils.decrypt(key, initVector, accessToken);
      boolean isAuth = secureUser.getToken().equals(tokenDecrypted);
      if (isAuth) {
        authUser.setUserId(secureUser.getId());
        authUser.setRole(secureUser.getRole());
        return authUser;
      }
      return null;
    });
  }

  public Optional<UserInfo> userInfo(AuthUser authUser) {
    String accessToken = authUser.getAccessToken();
    Optional<SecureUser> secureUserOptional = secureUserDao.findById(authUser.getUserId());
    return secureUserOptional.map((secureUser) -> {
      String key = secureUser.getKey();
      String initVector = secureUser.getInitVector();
      String tokenDecrypted = SecureUtils.decrypt(key, initVector, accessToken);
      boolean isAuth = secureUser.getToken().equals(tokenDecrypted);
      if (isAuth) {
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
      String key = secureUser.getKey();
      String initVector = secureUser.getInitVector();
      String tokenDecrypted = SecureUtils.decrypt(key, initVector, accessToken);
      boolean isAuth = secureUser.getToken().equals(tokenDecrypted);
      if (isAuth) {
        secureUser.setToken("");
        secureUser.setAccessToken("");
        secureUser.setKey("");
        secureUser.setTokenLength(0);
        secureUser.setInitVector("");
        secureUser.setUserSession("");
        secureUserDao.save(secureUser);
      }
      return AuthUser.anonymous();
    });
  }

  private String getUserSession() {
    return Utils.getRandomString(appProperties.sessionLength());
  }

  private void hashCredentials(RegisterUser registerUser, int tokenLengthInt, SecureUser secureUser) throws NoSuchAlgorithmException {
    String credentials = registerUser.getCredentials();
    String salt = ":" + Utils.getRandomString(tokenLengthInt);
    secureUser.setSalt(salt);
    String digest = SecureUtils.digest(credentials + salt);
    secureUser.setDigest(digest);
  }

  private String getAccessToken(SecureUser secureUser, int tokenLengthInt) {
    String randomToken = Utils.getRandomString(tokenLengthInt);
    secureUser.setToken(randomToken);
    int encLength = 16;
    String initVector = Utils.getRandomString(encLength);
    secureUser.setInitVector(initVector);
    String key = getRandomString(encLength);
    secureUser.setKey(key);
    return SecureUtils.encrypt(key, initVector, randomToken);
  }
}
