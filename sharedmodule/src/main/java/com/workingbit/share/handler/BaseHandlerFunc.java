package com.workingbit.share.handler;

import com.workingbit.share.client.ShareRemoteClient;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.model.*;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.StringUtils;
import spark.Request;
import spark.Response;

import java.time.LocalDateTime;
import java.util.*;

import static com.workingbit.share.common.RequestConstants.*;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;

/**
 * Created by Aleksey Popryaduhin on 16:37 01/10/2017.
 */
public interface BaseHandlerFunc<T extends Payload> {

  Answer process(T data, Optional<AuthUser> token);

  default void logRequest(Request request) {
    System.out.println(String.format("REQUEST: %s %s %s %s %s",
        LocalDateTime.now(),
        request.requestMethod(), request.url(), request.host(), request.userAgent()));
  }

  default void logResponse(String url, Response response, AuthUser token) {
    System.out.println(String.format("RESPONSE: %s %s %s %s",
        url,
        LocalDateTime.now(),
        response.status(), token));
  }

  default Answer getAnswer(Request request, Response response, IPath path, T data) {
    String userSession = getOrCreateSession(request, response);
    String accessToken = request.headers(ACCESS_TOKEN_HEADER);
    String roleStr = request.headers(USER_ROLE_HEADER);
    int counter = 0;
    try {
      counter = Integer.parseInt(request.headers(AUTH_COUNTER_HEADER));
    } catch (NumberFormatException ignore) {
    }
    Set<EnumSecureRole> roles = new HashSet<>(Collections.singletonList(EnumSecureRole.ANONYMOUS));
    if (StringUtils.isNotBlank(roleStr)) {
      roles = EnumSecureRole.parseRoles(roleStr);
    }
    AuthUser internalUserRole = getInternalUserRole(accessToken, userSession);
    internalUserRole.setCounter(counter);
    if (path.isSecure() || EnumSecureRole.isSecure(roles)) {
      Optional<AuthUser> authenticated = isAuthenticated(internalUserRole);
      if (!authenticated.isPresent()) {
        return getForbiddenAnswer(response);
      }
      AuthUser securedUser = authenticated.get();
      if (!hasRights(securedUser.getRoles(), path)) {
        return getForbiddenAnswer(response);
      }
      Answer securedAnswer = getSecureAnswer(data, securedUser);
      if (securedAnswer == null) {
        return getForbiddenAnswer(response);
      }
      return securedAnswer;
    } else {
      return getInsecureAnswer(data, internalUserRole);
    }
  }

  default boolean hasRights(Set<EnumSecureRole> roles, IPath path) {
    if (roles.contains(EnumSecureRole.BAN)) {
      return false;
    }
    return path.getRoles().isEmpty() || path.getRoles().containsAll(roles);
  }

  default Answer getSecureAnswer(T data, AuthUser authenticated) {
    Answer processed = process(data, Optional.of(authenticated));
    processed.setAuthUser(authenticated);
    return processed;
  }

  default Answer getForbiddenAnswer(Response response) {
    String anonymousSession = getSessionAndSetCookieInResponse(response);
    AuthUser authUser = new AuthUser(anonymousSession)
        .setRole(EnumSecureRole.ANONYMOUS);
    return Answer.created(authUser)
        .statusCode(HTTP_FORBIDDEN)
        .message(HTTP_FORBIDDEN, ErrorMessages.UNABLE_TO_AUTHENTICATE);
  }

  default Answer getInsecureAnswer(T data, AuthUser authUser) {
    Answer process = process(data, Optional.ofNullable(authUser));
    if (process.getBody() instanceof AuthUser) {
      process.setAuthUser((AuthUser) process.getBody());
    }
    return process;
  }

  default String getOrCreateSession(Request request, Response response) {
    // take user session which user got after login
    String accessToken = request.headers(ACCESS_TOKEN_HEADER);
    if (StringUtils.isBlank(accessToken)) {
      // if anonymous user
      String anonymousSession = request.cookie(ANONYMOUS_SESSION_HEADER);
      if (StringUtils.isBlank(anonymousSession)) {
        anonymousSession = getSessionAndSetCookieInResponse(response);
      }
      // return anonym session
      return anonymousSession;
    }
    // return transfer session
    return request.headers(USER_SESSION_HEADER);
  }

  /**
   * if does not have session give it him
   *
   * @return generate session and set cookie
   */
  default String getSessionAndSetCookieInResponse(Response response) {
    String anonymousSession = Utils.getRandomString(SESSION_LENGTH);
    response.cookie(ANONYMOUS_SESSION_HEADER, anonymousSession, COOKIE_AGE, false, true);
    System.out.println("Set-Cookie " + ANONYMOUS_SESSION_HEADER + ": " + anonymousSession);
    return anonymousSession;
  }

  default Optional<AuthUser> isAuthenticated(AuthUser authUser) {
    return ShareRemoteClient.Singleton.getInstance().authenticate(authUser);
  }

  default AuthUser getInternalUserRole(String accessToken, String userSession) {
    if (StringUtils.isBlank(accessToken) || StringUtils.isBlank(userSession)) {
      return new AuthUser(userSession).setRole(EnumSecureRole.ANONYMOUS);
    }
    return new AuthUser(accessToken, userSession).setRole(EnumSecureRole.INTERNAL);
  }
}
