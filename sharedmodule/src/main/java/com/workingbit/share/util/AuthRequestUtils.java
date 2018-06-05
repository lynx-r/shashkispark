//package com.workingbit.share.util;
//
//import com.workingbit.orchestrate.service.OrchestralService;
//import com.workingbit.share.exception.RequestException;
//import com.workingbit.share.model.AuthUser;
//import com.workingbit.share.model.enumarable.EnumAuthority;
//import com.workingbit.share.util.Utils;
//import org.apache.commons.lang3.StringUtils;
//import spark.Request;
//import spark.Response;
//
//import javax.validation.ConstraintViolation;
//import javax.validation.Validation;
//import javax.validation.Validator;
//import javax.validation.ValidatorFactory;
//import java.time.LocalDateTime;
//import java.util.Optional;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import static com.workingbit.share.common.RequestConstants.*;
//
///**
// * Created by Aleksey Popryaduhin on 16:37 01/10/2017.
// */
//public class AuthRequestUtils {
//  private static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
//  private static Validator validator;
//  static {
//    validator = factory.getValidator();
//  }
//
////  public static Optional<AuthUser> getTrustedAuthUser(AuthUser authUser) throws RequestException {
////    return ShareRemoteClient.Singleton.getInstance()
////        .userInfo(authUser)
////        .map(Answer::getBody)
////        .map((UserInfo userInfo) -> AuthUser.simpleUser(userInfo.getUserId(), userInfo.getUsername(),
////            authUser.getAccessToken(), authUser.getUserSession(), authUser.getAuthorities()));
////  }
//
//  public static Optional<AuthUser> isAuthenticated(AuthUser authUser) throws RequestException {
//    return OrchestralService.Singleton.getInstance()
//        .authenticate(authUser);
//  }
//
//  public static boolean hasAuthorities(Set<EnumAuthority> clientAuthorities, Set<EnumAuthority> allowedAuthorities) {
//    if (clientAuthorities.contains(EnumAuthority.REMOVED)) {
//      return false;
//    }
//    return allowedAuthorities.isEmpty() || EnumAuthority.hasAuthorities(clientAuthorities, allowedAuthorities);
//  }
//
//  private static String getOrCreateSession(Request request, Response response) {
//    // take user session which user got after login
//    String accessToken = request.headers(ACCESS_TOKEN_HEADER);
//    if (StringUtils.isBlank(accessToken)) {
//      // if anonymous user
//      String anonymousSession = request.cookie(ANONYMOUS_SESSION_HEADER);
//      if (StringUtils.isBlank(anonymousSession)) {
//        anonymousSession = getSessionAndSetCookieInResponse(response);
//      }
//      // return anonym session
//      return anonymousSession;
//    }
//    // return transfer session
//    return request.headers(USER_SESSION_HEADER);
//  }
//
//  public static String getSessionAndSetCookieInResponse(Response response) {
//    String anonymousSession = Utils.getRandomString(SESSION_LENGTH);
//    response.cookie(ANONYMOUS_SESSION_HEADER, anonymousSession, COOKIE_AGE, false, true);
//    return anonymousSession;
//  }
//
//  public static void logRequest(Request request) {
//    System.out.println(String.format("REQUEST: %s %s %s %s %s",
//        LocalDateTime.now(),
//        request.requestMethod(), request.url(), request.host(), request.userAgent()));
//  }
//
//  public static void logResponse(String url, Response response, AuthUser token) {
//    System.out.println(String.format("RESPONSE: %s %s %s %s",
//        url,
//        LocalDateTime.now(),
//        response.status(), token));
//  }
//
//  public static AuthUser getAuthUser(Request request, Response response) {
//    String userSession = getOrCreateSession(request, response);
//    String accessToken = request.headers(ACCESS_TOKEN_HEADER);
//
//    if (StringUtils.isBlank(accessToken) || StringUtils.isBlank(userSession)) {
//      return new AuthUser(userSession).setAuthority(EnumAuthority.ANONYMOUS);
//    }
//    return new AuthUser(accessToken, userSession);
//  }
//
//  public static <T> Set<String> violations(T bean) {
//    Set<ConstraintViolation<T>> violations = validator.validate(bean);
//    return violations
//        .stream()
//        .map(ConstraintViolation::getMessage)
//        .collect(Collectors.toSet());
//  }
//}
