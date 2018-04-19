package com.workingbit.share.handler;

import com.workingbit.share.client.ShareRemoteClient;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.model.Answer;
import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.EnumSecureRole;
import com.workingbit.share.model.Payload;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.StringUtils;
import spark.Request;
import spark.Response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.workingbit.share.common.RequestConstants.*;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;

/**
 * Created by Aleksey Popryaduhin on 16:37 01/10/2017.
 */
public interface BaseHandlerFunc<T extends Payload> {

  Answer process(T data, Optional<AuthUser> token);

  default void logRequest(Request request) {
    System.out.println(String.format("%s %s %s %s %s",
        LocalDateTime.now().format(DateTimeFormatter.ISO_DATE),
        request.requestMethod(), request.url(), request.host(), request.userAgent()));
  }

  default Answer getAnswer(Request request, Response response, boolean secure, T data) {
    String userSession = getOrCreateSession(request, response);
    String token = request.headers(ACCESS_TOKEN);
    String roleStr = request.headers(USER_ROLE);
    EnumSecureRole role = EnumSecureRole.ANONYMOUS;
    if (StringUtils.isNotBlank(roleStr)) {
      role = EnumSecureRole.valueOf(roleStr.toUpperCase());
    }
    if (secure || EnumSecureRole.isSecure(role)) {
      Answer processedAnswer = getSecureAnswer(data, token, userSession);
      if (processedAnswer == null) {
        return getForbiddenAnswer(response);
      }
      return processedAnswer;
    } else {
      return getInsecureAnswer(data, role, userSession);
    }
  }

  default Answer getSecureAnswer(T data, String token, String session) {
    Optional<AuthUser> authUser = isAuthenticated(token, session);
    if (authUser.isPresent()) {
      Answer processed = process(data, authUser);
      processed.setAuthUser(authUser.get());
      return processed;
    }
    return null;
  }

  default Answer getForbiddenAnswer(Response response) {
    String anonymousSession = getSessionAndSetCookieInResponse(response);
    AuthUser authUser = new AuthUser(anonymousSession)
        .role(EnumSecureRole.ANONYMOUS);
    return Answer.created(authUser)
        .statusCode(HTTP_FORBIDDEN)
        .message(HTTP_FORBIDDEN, ErrorMessages.UNABLE_TO_AUTHENTICATE);
  }

  default Answer getInsecureAnswer(T data, EnumSecureRole role, String userSession) {
    AuthUser authUser = new AuthUser(userSession)
        .role(role);
    Optional<AuthUser> authUserOptional = Optional.of(authUser);
    return process(data, authUserOptional);
  }

  default String getOrCreateSession(Request request, Response response) {
    // take user session which user got after login
    String accessToken = request.headers(ACCESS_TOKEN);
    if (StringUtils.isBlank(accessToken)) {
      // if anonymous user
      String anonymousSession = request.cookie(ANONYMOUS_SESSION);
      if (StringUtils.isBlank(anonymousSession)) {
        anonymousSession = getSessionAndSetCookieInResponse(response);
      }
      // return anonym session
      return anonymousSession;
    }
    // return transfer session
    return request.headers(USER_SESSION);
  }

  /**
   * if does not have session give it him
   *
   * @return generate session and set cookie
   */
  default String getSessionAndSetCookieInResponse(Response response) {
    String anonymousSession;
    anonymousSession = Utils.getRandomString(SESSION_LENGTH);
    response.cookie(ANONYMOUS_SESSION, anonymousSession, COOKIE_AGE, false, true);
    return anonymousSession;
  }

  default Optional<AuthUser> isAuthenticated(String accessToken, String session) {
    if (StringUtils.isBlank(accessToken) || StringUtils.isBlank(session)) {
      return Optional.empty();
    }
    AuthUser authUser = new AuthUser(accessToken, session)
        .role(EnumSecureRole.INTERNAL);
    return ShareRemoteClient.Singleton.getInstance().authenticate(authUser);
  }
}
