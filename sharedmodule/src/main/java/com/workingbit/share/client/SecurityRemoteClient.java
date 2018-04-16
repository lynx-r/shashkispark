package com.workingbit.share.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.workingbit.share.common.ShareProperties;
import com.workingbit.share.model.Answer;
import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.Payload;
import com.workingbit.share.model.RegisterUser;
import com.workingbit.share.util.UnirestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.workingbit.share.common.Config4j.configurationProvider;
import static com.workingbit.share.common.RequestConstants.ACCESS_TOKEN;
import static com.workingbit.share.common.RequestConstants.JSESSIONID;

/**
 * Created by Aleksey Popryaduhin on 23:59 27/09/2017.
 */
public class SecurityRemoteClient {

  private static final Logger logger = LoggerFactory.getLogger(SecurityRemoteClient.class);

  private static String register;
  private static String authorize;
  private static String authenticate;

  static {
    ShareProperties shareProperties = configurationProvider().bind("app", ShareProperties.class);
    register = shareProperties.registerResource();
    authorize = shareProperties.authorizeResource();
    authenticate = shareProperties.authenticateResource();
    UnirestUtil.configureSerialization();
  }

  public static SecurityRemoteClient getInstance() {
    return new SecurityRemoteClient();
  }

  public Optional<AuthUser> register(RegisterUser registerUser) {
    return register(registerUser, Collections.emptyMap());
  }

  public Optional<AuthUser> register(RegisterUser registerUser, Map<String, String> headers) {
    return post(register, headers, registerUser);
  }

  public Optional<AuthUser> authorize(RegisterUser registerUser) {
    return authorize(registerUser, Collections.emptyMap());
  }

  public Optional<AuthUser> authorize(RegisterUser registerUser, Map<String, String> headers) {
    return post(authorize, headers, registerUser);
  }

  public Optional<AuthUser> authenticate(AuthUser authUser) {
    Map<String, String> headers = new HashMap<String, String>() {{
      put(ACCESS_TOKEN, authUser.getAccessToken());
      put(JSESSIONID, authUser.getSession());
    }};
    return authenticate(authUser, headers);
  }

  public Optional<AuthUser> authenticate(AuthUser authUser, Map<String, String> headers) {
    return post(authenticate, headers, authUser);
  }

  private Optional<AuthUser> post(String resource, Map<String, String> headers, Payload registerUser) {
    try {
      HttpResponse<Answer> response = Unirest.post(resource)
          .headers(headers)
          .body(registerUser)
          .asObject(Answer.class);
      if (response.getStatus() == 200) {
        Answer body = response.getBody();
        return Optional.of((AuthUser) body.getBody());
      }
      logger.error("Invalid status " + response.getStatus());
    } catch (UnirestException e) {
      logger.error("Unirest exception", e);
    }
    return Optional.empty();
  }
}
