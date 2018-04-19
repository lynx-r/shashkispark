package com.workingbit.share.handler;

import com.workingbit.share.client.ShareRemoteClient;
import com.workingbit.share.model.Answer;
import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.EnumSecureRole;
import com.workingbit.share.model.Payload;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.StringUtils;
import spark.Request;
import spark.Response;

import java.util.Optional;

import static com.workingbit.share.common.RequestConstants.*;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

/**
 * Created by Aleksey Popryaduhin on 16:37 01/10/2017.
 */
public interface BaseHandlerFunc<T extends Payload> {

  Answer process(T data, Optional<AuthUser> token);

  default void logRequest(Request request) {
    System.out.println(String.format("%s %s", request.requestMethod(), request.url()));
  }

  default Answer getAnswer(Request request, Response response, boolean secure, T data) {
    String session = getOrCreateSession(request, response);
    String token = request.headers(ACCESS_TOKEN);
    if (secure) {
      return getSecureAnswer(data, token, session);
    } else {
      return getInsecureAnswer(data, token, session);
    }
  }

  default Answer getSecureAnswer(T data, String token, String session) {
    Optional<AuthUser> authUser = isAuthenticated(token, session);
    if (authUser.isPresent()) {
      return process(data, authUser);
    } else {
      return Answer.error(HTTP_UNAUTHORIZED, "Вы не авторизованы");
    }
  }

  default Answer getInsecureAnswer(T data, String accessToken, String userSession) {
    Optional<AuthUser> authUser;
    if (StringUtils.isNotBlank(accessToken)) {
      authUser = isAuthenticated(accessToken, userSession);
    } else {
      authUser = Optional.of(new AuthUser(userSession).role(EnumSecureRole.ANONYMOUS));
    }
    return process(data, authUser);
  }

  default String getOrCreateSession(Request request, Response response) {
    // take user session which user got after login
    String accessToken = request.headers(ACCESS_TOKEN);
    if (StringUtils.isBlank(accessToken)) {
      // if anonymous user
      String anonymousSession = request.cookie(ANONYMOUS_SESSION);
      if (StringUtils.isBlank(anonymousSession)) {
        // if does not have session give it him
        anonymousSession = Utils.getRandomString(SESSION_LENGTH);
        response.cookie(ANONYMOUS_SESSION, anonymousSession, COOKIE_AGE, false, true);
      }
      // return anonym session
      return anonymousSession;
    }
    // return transfer session
    return request.headers(USER_SESSION);
  }

  default Optional<AuthUser> isAuthenticated(String accessToken, String session) {
    if (StringUtils.isBlank(accessToken) || StringUtils.isBlank(session)) {
      return Optional.empty();
    }
    AuthUser authUser = new AuthUser(accessToken, session);
    return ShareRemoteClient.getInstance().authenticate(authUser);
  }
}
