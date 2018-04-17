package com.workingbit.security.service;

import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.RegisterUser;
import com.workingbit.share.model.SecureUser;
import com.workingbit.share.util.SecureUtils;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.StringUtils;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static com.workingbit.security.SecurityApplication.secureUserDao;
import static com.workingbit.share.util.Utils.getRandomString;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
public class SecureUserService {

  public Optional<AuthUser> register(RegisterUser registerUser, Optional<AuthUser> token) {
    System.out.println("REGISTER USER " + registerUser);
    System.out.println("TOKEN " + token.get());

    return token.map(t -> {
      try {
        SecureUser secureUser = new SecureUser();
        Utils.setRandomIdAndCreatedAt(secureUser);
        secureUser.setUsername(registerUser.getUsername());

        int tokenLengthInt = getTokenLength();
        secureUser.setTokenLength(tokenLengthInt);

        // hash credentials
        hashCredentials(registerUser, tokenLengthInt, secureUser);

        // encrypt random token
        String accessToken = getAccessToken(secureUser, tokenLengthInt);

        // save encrypted token and userSession
        secureUser.setAccessToken(accessToken);
        secureUser.setUserSession(t.getUserSession());
        secureUserDao.save(secureUser);
        System.out.println("REGISTERED SEC USER " + secureUser);

        // send access token and userSession
        return new AuthUser(accessToken, t.getUserSession());
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    });
  }

  public Optional<AuthUser> authorize(RegisterUser registerUser, Optional<AuthUser> token) {
    System.out.println("AUTHORIZE USER " + registerUser);
    System.out.println("TOKEN " + token.get());

    return token.map(t ->
        secureUserDao.findByUsername(registerUser.getUsername())
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
                  secureUser.setAccessToken(accessToken);
                  secureUser.setUserSession(t.getUserSession());
                  secureUserDao.save(secureUser);

                  // send access token and userSession
                  return new AuthUser(accessToken, t.getUserSession());
                }
              } catch (Exception e) {
                e.printStackTrace();
              }
              return null;
            })
            .orElse(null));
  }

  public Optional<AuthUser> authenticate(AuthUser authUser) {
    System.out.println("AUTHENTICATE USER " + authUser);
    String session = authUser.getUserSession();
    String accessToken = authUser.getAccessToken();
    Optional<SecureUser> secureUserOptional = secureUserDao.findBySession(session);
    return secureUserOptional.map((secureUser) -> {
      System.out.println("FOUND SEC USER " + secureUser);
      String key = secureUser.getKey();
      String initVector = secureUser.getInitVector();
      String tokenDecrypted = SecureUtils.decrypt(key, initVector, accessToken);
      boolean isAuth = secureUser.getToken().equals(tokenDecrypted);
      if (isAuth) {
        return authUser;
      }
      return null;
    });
  }

  private int getTokenLength() {
    String tokenLength = System.getenv("TOKEN_LENGTH");
    int tokenLengthInt = 100;
    if (StringUtils.isNotBlank(tokenLength)) {
      tokenLengthInt = Integer.parseInt(tokenLength);
    }
    return tokenLengthInt;
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
