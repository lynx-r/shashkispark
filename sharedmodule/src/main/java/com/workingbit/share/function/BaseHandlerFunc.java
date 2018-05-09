//package com.workingbit.share.function;
//
//import com.amazonaws.services.dynamodbv2.model.AttributeValue;
//import com.workingbit.share.common.ErrorMessages;
//import com.workingbit.share.exception.RequestException;
//import com.workingbit.share.model.Answer;
//import com.workingbit.share.model.AuthUser;
//import com.workingbit.share.model.Payload;
//import com.workingbit.share.model.enumarable.EnumAuthority;
//import com.workingbit.share.model.enumarable.IAuthority;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import spark.Request;
//import spark.Response;
//
//import java.util.Map;
//import java.util.Optional;
//import java.util.Set;
//
//import static com.workingbit.share.common.RequestConstants.FILTERS_HEADER;
//import static com.workingbit.share.common.RequestConstants.FILTER_VALUES_HEADER;
//import static com.workingbit.share.util.AuthRequestUtils.*;
//import static com.workingbit.share.util.JsonUtils.jsonToDataTypeRef;
//import static java.net.HttpURLConnection.*;
//
///**
// * Created by Aleksey Popryaduhin on 16:37 01/10/2017.
// */
//public interface BaseHandlerFunc<T extends Payload> {
//
//  Logger logger = LoggerFactory.getLogger(BaseHandlerFunc.class);
//
//  Answer process(T data, AuthUser token) throws RequestException;
//
//  default Answer getAnswer(Request request, Response response, IAuthority path, T data) throws RequestException {
//    AuthUser authUser = getAuthUser(request, response);
////    if (!hasAuthorities(authUser.getAuthorities(), Set.of(EnumAuthority.INTERNAL))) {
//    boolean cannotBeAnonymous = !(path.getAuthorities().isEmpty()
//        || path.getAuthorities().contains(EnumAuthority.ANONYMOUS));
//    Optional<AuthUser> authenticated = Optional.empty();
//    try {
//      authenticated = isAuthenticated(authUser);
//    } catch (RequestException e) {
//      if (cannotBeAnonymous) {
//        throw RequestException.forbidden();
//      }
//    }
//    if (cannotBeAnonymous) {
//      if (!authenticated.isPresent()) {
//        return getForbiddenAnswer(response);
//      }
//      if (!hasAuthorities(authUser.getAuthorities(), path.getAuthorities())) {
//        return getForbiddenAnswer(response);
//      }
//      return getAnswerForChecked(request, response, data, authenticated.get());
//    }
//    return getAnswerForChecked(request, response, data, authUser /* unauthorized */);
////    }
////    if (hasAuthorities(authUser.getAuthorities(), Collections.singleton(EnumAuthority.ANONYMOUS))) {
////      return getForbiddenAnswer(response);
////    }
////    // internal can everything
////    return getAnswerForChecked(request, response, data, authUser);
//  }
//
//  default Answer getAnswerForAuth(Request request, Response response, T data) throws RequestException {
//    AuthUser authUser = getAuthUser(request, response);
//    Answer answerForAuth = getAnswerForChecked(request, response, data, authUser);
//    answerForAuth.setAuthUser((AuthUser) answerForAuth.getBody());
//    return answerForAuth;
//  }
//
//  private Answer getAnswerForChecked(Request request, Response response, T data, AuthUser authUser) throws RequestException {
//    String filters = request.headers(FILTERS_HEADER);
//    String filterValues = request.headers(FILTER_VALUES_HEADER);
//    if (StringUtils.isNotBlank(filters)) {
//      authUser.setFilters(filters);
//      @SuppressWarnings("unchecked")
//      Map<String, AttributeValue> filterValuesMap = jsonToDataTypeRef(filterValues);
//      authUser.setFilterValues(filterValuesMap);
//    }
//    Answer securedAnswer = getSecureAnswer(data, authUser);
//    if (securedAnswer == null) {
//      return getForbiddenAnswer(response);
//    }
//    return securedAnswer;
//  }
//
//  private Answer getSecureAnswer(T data, AuthUser authenticated) throws RequestException {
//    Set<String> violations = violations(data);
//    Answer processed;
//    if (violations.isEmpty()) {
//      processed = process(data, authenticated);
//      processed.setAuthUser(authenticated);
//    } else {
//      return Answer.error(HTTP_BAD_REQUEST, violations.toArray(new String[0]));
//    }
//    boolean isResponseSuccess = processed.getStatusCode() != HTTP_OK || processed.getStatusCode() != HTTP_CREATED;
//    if (!isResponseSuccess) {
//      return Answer.error(HTTP_BAD_REQUEST, violations.toArray(new String[0]));
//    }
//    return processed;
//  }
//
//  private Answer getForbiddenAnswer(Response response) {
//    String anonymousSession = getSessionAndSetCookieInResponse(response);
//    AuthUser authUser = new AuthUser(anonymousSession)
//        .setAuthority(EnumAuthority.ANONYMOUS);
//    return Answer.created(authUser)
//        .statusCode(HTTP_FORBIDDEN)
//        .message(HTTP_FORBIDDEN, ErrorMessages.UNABLE_TO_AUTHENTICATE);
//  }
//}
