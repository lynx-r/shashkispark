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

  default String preprocess(Request request, Response response) {
    return null;
  }

  default Answer createAnswer(Request request, Response response, boolean secure, T data) {
    Answer answer;
    if (secure) {
      String session = request.headers(USER_SESSION);
      String token = request.headers(ACCESS_TOKEN);
      answer = secureCheck(data, token, session);
    } else {
      String userSession = getOrCreateSession(request, response);
      AuthUser role = new AuthUser(userSession).role(EnumSecureRole.ANONYMOUS);
      answer = process(data, Optional.of(role));
    }
    return answer;
  }

  default Answer secureCheck(T data, String token, String session) {
    Optional<AuthUser> authUser = isAuthenticated(token, session);
    if (authUser.isPresent()) {
      return process(data, authUser);
    } else {
      return Answer.error(HTTP_UNAUTHORIZED, "Вы не авторизованы");
    }
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
