package com.workingbit.orchestrate.function;

import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumAuthority;
import com.workingbit.share.model.enumarable.IAuthority;
import org.apache.commons.lang3.StringUtils;
import spark.Request;
import spark.Response;

import java.util.Optional;
import java.util.Set;

import static com.workingbit.orchestrate.OrchestrateModule.orchestralService;
import static com.workingbit.orchestrate.util.AuthRequestUtil.*;
import static com.workingbit.orchestrate.util.RedisUtil.checkInternalRequest;
import static com.workingbit.share.common.RequestConstants.FILTERS_HEADER;
import static java.net.HttpURLConnection.*;

/**
 * Created by Aleksey Popryaduhin on 16:37 01/10/2017.
 */
public interface BaseHandlerFunc<T extends Payload> {

  Answer process(T data, AuthUser token, QueryPayload query) throws RequestException;

  default Answer getAnswer(Request request, Response response, IAuthority path, T data, QueryPayload query) {
    AuthUser authUser = getAuthUser(request, response);
    if (authUser.getInternalKey() != null) {
      boolean isAuthUserValid = checkInternalRequest(authUser.getInternalKey(), authUser);
      if (!isAuthUserValid) {
        throw RequestException.badRequest();
      }
    }
    if (!hasAuthorities(authUser.getAuthorities(), Set.of(EnumAuthority.INTERNAL))) {
      boolean canBeAnonymous = canBeAnonymous(path);
      Optional<AuthUser> authenticated = Optional.empty();
      try {
        authenticated = isAuthenticated(authUser);
      } catch (RequestException e) {
        String lastToken = orchestralService.checkTokenCache(authUser);
        if (lastToken != null) {
          authUser.setAccessToken(lastToken);
          try {
            authenticated = isAuthenticated(authUser);
          } catch (RequestException ignore) {
            if (!canBeAnonymous) {
              throw RequestException.forbidden();
            }
          }
        } else if (!canBeAnonymous) {
          throw RequestException.forbidden();
        }
      }
      if (canBeAnonymous || authenticated.isPresent()) {
        AuthUser authed = authUser;
        if (authenticated.isPresent()) {
          authed = authenticated.get();
          authed.setSuperHash(authUser.getSuperHash());
        }
        if (!path.getAuthorities().isEmpty()
            && !hasAuthorities(authed.getAuthorities(), path.getAuthorities())) {
          return getForbiddenAnswer(response);
        }
        String filterExpression = request.headers(FILTERS_HEADER);
        if (StringUtils.isNotBlank(filterExpression)) {
          authed.parseFilters(filterExpression);
        }
        return getAnswerForChecked(request, data, authed, query);
      }
      return getForbiddenAnswer(response);
    }
    if (hasAuthorities(authUser.getAuthorities(), Set.of(EnumAuthority.ANONYMOUS))) {
      return getForbiddenAnswer(response);
    }
    // internal can everything
    return getAnswerForChecked(request, data, authUser);
  }

  default Answer getAnswer(Request request, Response response, IAuthority path, T data) throws RequestException {
    return getAnswer(request, response, path, data, null);
  }

  default Answer getAnswerForAuth(Request request, Response response, T data) throws RequestException {
    AuthUser authUser = getAuthUser(request, response);
    Answer answerForAuth = getAnswerForChecked(request, data, authUser);
    answerForAuth.setAuthUser((AuthUser) answerForAuth.getBody());
    return answerForAuth;
  }

  private Answer getAnswerForChecked(Request request, T data, AuthUser authed, QueryPayload query) {
    Answer securedAnswer = getSecureAnswer(data, authed, query);
    orchestralService.cacheRequest(request, securedAnswer, authed);
    return securedAnswer;
  }

  private Answer getAnswerForChecked(Request request, T data, AuthUser authUser) throws RequestException {
    return getAnswerForChecked(request, data, authUser, null);
  }

  private Answer getSecureAnswer(T data, AuthUser authenticated, QueryPayload query) {
    Set<String> violations = violations(data);
    Answer processed;
    if (violations.isEmpty()) {
      processed = process(data, authenticated, query);
      processed.setAuthUser(authenticated);
    } else {
      return Answer.error(HTTP_BAD_REQUEST, violations.toArray(new String[0]));
    }
    return processed;
  }

  private Answer getForbiddenAnswer(Response response) {
    String anonymousSession = getSessionAndSetCookieInResponse(response);
    AuthUser authUser = new AuthUser(anonymousSession)
        .addAuthorities(EnumAuthority.ANONYMOUS);
    return Answer.created(authUser)
        .statusCode(HTTP_FORBIDDEN)
        .message(HTTP_FORBIDDEN, ErrorMessages.UNABLE_TO_AUTHENTICATE);
  }
}
