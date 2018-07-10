//package com.workingbit.orchestrate.util;
//
//import com.workingbit.orchestrate.function.BaseHandlerFunc;
//import com.workingbit.share.exception.RequestException;
//import com.workingbit.share.model.Answer;
//import com.workingbit.share.model.AuthUser;
//import com.workingbit.share.model.DomainId;
//import com.workingbit.share.model.enumarable.EnumAuthority;
//import com.workingbit.share.model.enumarable.IAuthority;
//import com.workingbit.share.util.Utils;
//import org.apache.commons.lang3.StringUtils;
//import org.jetbrains.annotations.NotNull;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
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
//import static com.workingbit.orchestrate.OrchestrateModule.orchestralService;
//import static com.workingbit.share.common.DBConstants.DATE_TIME_FORMATTER;
//import static com.workingbit.share.common.RequestConstants.*;
//
///**
// * Created by Aleksey Popryaduhin on 16:37 01/10/2017.
// */
//public class AuthRequestUtil {
//
//  private static Logger logger = LoggerFactory.getLogger(BaseHandlerFunc.class);
//
//  private static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
//  private static Validator validator;
//
//  static {
//    validator = factory.getValidator();
//  }
//
//  public static Optional<AuthUser> isAuthenticated(@NotNull AuthUser authUser) throws RequestException {
//    return orchestralService
//        .internal(authUser, "authenticateAnswer", authUser)
//        .map(Answer::getAuthUser);
//  }
//
//  public static boolean hasAuthorities(Set<EnumAuthority> clientAuthorities, @NotNull Set<EnumAuthority> allowedAuthorities) {
//    if (clientAuthorities.contains(EnumAuthority.REMOVED)) {
//      return false;
//    }
//    return !allowedAuthorities.isEmpty() && EnumAuthority.hasAuthorities(clientAuthorities, allowedAuthorities);
//  }
//
//  private static String getOrCreateSession(Request request, @NotNull Response response) {
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
//  @NotNull
//  public static String getSessionAndSetCookieInResponse(Response response) {
//    String anonymousSession = Utils.getRandomString(SESSION_LENGTH);
//    response.cookie(ANONYMOUS_SESSION_HEADER, anonymousSession, COOKIE_AGE, false, true);
//    return anonymousSession;
//  }
//
//  public static void logRequest(Request request) {
//    logger.info(String.format("REQUEST: %s %s %s %s %s",
//        request.requestMethod(), LocalDateTime.now(),
//        request.url(), request.host(), request.userAgent()));
//  }
//
//  public static void logResponse(String url, Response response, AuthUser token) {
//    logger.info(String.format("RESPONSE: %s %s %s %s",
//        url,
//        LocalDateTime.now(),
//        response.status(), token == null ? "" : token.getAccessToken()));
//  }
//
//  @NotNull
//  public static AuthUser getAuthUser(@NotNull Request request, @NotNull Response response) {
//    String userSession = getOrCreateSession(request, response);
//    String userId = request.headers(USER_ID_HEADER);
//    String username = request.headers(USERNAME_HEADER);
//    String userCreatedAt = request.headers(USER_CREATED_AT_HEADER);
//    String accessToken = request.headers(ACCESS_TOKEN_HEADER);
//    String internalKey = request.headers(INTERNAL_KEY_HEADER);
//    String superHash = request.headers(SUPER_HASH_HEADER);
//
//    if (StringUtils.isBlank(accessToken) || StringUtils.isBlank(userSession)) {
//      return new AuthUser(userSession).addAuthorities(EnumAuthority.ANONYMOUS);
//    }
//    AuthUser authUser = new AuthUser(accessToken, userSession);
//    authUser.setEmail(username);
//    authUser.setSuperHash(superHash);
//    if (StringUtils.isNotBlank(internalKey)) {
//      authUser.setInternalKey(internalKey);
//      authUser.addAuthorities(EnumAuthority.INTERNAL);
//      if (StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(userCreatedAt)) {
//        authUser.setUserId(new DomainId(userId, LocalDateTime.parse(userCreatedAt, DATE_TIME_FORMATTER)));
//      }
//    }
//    return authUser;
//  }
//
//  public static <T> Set<String> violations(T bean) {
//    Set<ConstraintViolation<T>> violations = validator.validate(bean);
//    return violations
//        .stream()
//        .map(ConstraintViolation::getMessage)
//        .collect(Collectors.toSet());
//  }
//
//  public static boolean canBeAnonymous(IAuthority path) {
//    return path.getAuthorities().isEmpty()
//        || path.getAuthorities().contains(EnumAuthority.ANONYMOUS);
//  }
//}
