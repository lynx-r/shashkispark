package com.workingbit.share.service;

import com.workingbit.share.common.ShareProperties;
import com.workingbit.share.dao.SecureUserDao;
import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.RegisterUser;
import com.workingbit.share.model.SecureUser;
import com.workingbit.share.util.Encryptor;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

import static com.workingbit.share.common.Config4j.configurationProvider;
import static com.workingbit.share.util.Utils.getRandomString;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
public class SecureUserService {

  private static SecureUserDao secureUserDao;

  static {
    ShareProperties shareProperties = configurationProvider().bind("app", ShareProperties.class);
    secureUserDao = new SecureUserDao(shareProperties);
  }

  public static SecureUserService getInstance() {
    return new SecureUserService();
  }

  public Optional<AuthUser> register(RegisterUser registerUser, Optional<AuthUser> token) {
    return token.map(t -> {
      try {
        String tokenLength = System.getenv("TOKEN_LENGTH");
        int tokenLengthInt = 100;
        if (StringUtils.isNotBlank(tokenLength)) {
          tokenLengthInt = Integer.parseInt(tokenLength);
        }

        SecureUser secureUser = new SecureUser();
        Utils.setRandomIdAndCreatedAt(secureUser);
        secureUser.setUsername(registerUser.getUsername());

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
        secureUserDao.save(secureUser);

        // send access token and userSession
        return new AuthUser(accessToken, t.getSession());
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    });
  }

  public Optional<AuthUser> authorize(RegisterUser registerUser, Optional<AuthUser> token) {
    return token.map(t ->
        secureUserDao.findByUsername(registerUser.getUsername())
            .map(secureUser -> {
              try {
                int tokenLengthInt = secureUser.getTokenLength();
                // hash credentials
                String credentials = registerUser.getCredentials();
                String salt = secureUser.getSalt();
                String clientDigest = Encryptor.digest(credentials + salt);

                if (clientDigest.equals(secureUser.getDigest())) {
                  // encrypt random token
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
                  secureUserDao.save(secureUser);

                  // send access token and userSession
                  return new AuthUser(accessToken, t.getSession());
                }
              } catch (Exception e) {
                e.printStackTrace();
              }
              return null;
            })
            .orElse(null));
  }

  public Optional<AuthUser> authenticate(AuthUser authUser) {
    String session = authUser.getSession();
    String accessToken = authUser.getAccessToken();
    Optional<SecureUser> secureUserOptional = secureUserDao.findBySession(session);
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
