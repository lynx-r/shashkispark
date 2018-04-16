package com.workingbit.share.util;

import com.workingbit.share.dao.SecureUserDao;
import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.RegisterUser;
import com.workingbit.share.model.SecureUser;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

import static com.workingbit.share.util.Utils.getRandomString;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
public class SecureUtils {

  public static Optional<AuthUser> register(RegisterUser registerUser, Optional<AuthUser> token) {
    return token.map(t -> {
      try {
        String tokenLength = System.getenv("TOKEN_LENGTH");
        int tokenLengthInt = 100;
        if (StringUtils.isNotBlank(tokenLength)) {
          tokenLengthInt = Integer.parseInt(tokenLength);
        }

        SecureUser secureUser = new SecureUser();
        Utils.setRandomIdAndCreatedAt(secureUser);

        // hash credentials
        String credentials = registerUser.getCredentials();
        String salt = ":" + Utils.getRandomString(tokenLengthInt);
        secureUser.setSalt(salt);
        String digest = Encryptor.digest(credentials + salt);
        secureUser.setDigest(digest);

        // encrypt random token
        secureUser.setTokenLength(tokenLengthInt);
        String randomToken = Utils.getRandomString(tokenLengthInt);
        secureUser.setToken(randomToken);
        int encLength = 16;
        String initVector = Utils.getRandomString(encLength);
        secureUser.setInitVector(initVector);
        String key = getRandomString(encLength);
        secureUser.setKey(key);
        String accessToken = Encryptor.encrypt(key, initVector, randomToken);

        // save encrypted token and userSession
        secureUser.setAccessToken(accessToken);
        secureUser.setUserSession(t.getSession());
        SecureUserDao.getInstance().save(secureUser);

        // send access token and userSession
        return new AuthUser(accessToken, t.getSession());
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    });
  }
  public static Optional<AuthUser> authorize(RegisterUser registerUser, Optional<AuthUser> token) {
    return token.map(t -> {
      try {
        String tokenLength = System.getenv("TOKEN_LENGTH");
        int tokenLengthInt = 100;
        if (StringUtils.isNotBlank(tokenLength)) {
          tokenLengthInt = Integer.parseInt(tokenLength);
        }

        SecureUser secureUser = new SecureUser();
        Utils.setRandomIdAndCreatedAt(secureUser);

        // hash credentials
        String credentials = registerUser.getCredentials();
        String salt = ":" + Utils.getRandomString(tokenLengthInt);
        secureUser.setSalt(salt);
        String digest = Encryptor.digest(credentials + salt);
        secureUser.setDigest(digest);

        // encrypt random token
        secureUser.setTokenLength(tokenLengthInt);
        String randomToken = Utils.getRandomString(tokenLengthInt);
        secureUser.setToken(randomToken);
        int encLength = 16;
        String initVector = Utils.getRandomString(encLength);
        secureUser.setInitVector(initVector);
        String key = getRandomString(encLength);
        secureUser.setKey(key);
        String accessToken = Encryptor.encrypt(key, initVector, randomToken);

        // save encrypted token and userSession
        secureUser.setAccessToken(accessToken);
        secureUser.setUserSession(t.getSession());
        SecureUserDao.getInstance().save(secureUser);

        // send access token and userSession
        return new AuthUser(accessToken, t.getSession());
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    });
  }

  public static Optional<AuthUser> authenticate(AuthUser authUser) {
    String session = authUser.getSession();
    String accessToken = authUser.getAccessToken();
    Optional<SecureUser> secureUserOptional = SecureUserDao.getInstance().findBySession(session);
    return secureUserOptional.map((secureUser) -> {
      String key = secureUser.getKey();
      String initVector = secureUser.getInitVector();
      String tokenDecrypted = Encryptor.decrypt(key, initVector, accessToken);
      boolean isAuth = secureUser.getToken().equals(tokenDecrypted);
      if (isAuth) {
        return authUser;
      }
      return null;
    });
  }

}
