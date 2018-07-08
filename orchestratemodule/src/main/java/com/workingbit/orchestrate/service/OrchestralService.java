package com.workingbit.orchestrate.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.workingbit.orchestrate.config.ModuleProperties;
import com.workingbit.orchestrate.util.RedisUtil;
import com.workingbit.share.common.RequestConstants;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumAuthority;
import com.workingbit.share.util.UnirestUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.workingbit.orchestrate.util.RedisUtil.putInternalRequest;
import static com.workingbit.share.common.DBConstants.DATE_TIME_FORMATTER;
import static com.workingbit.share.util.Utils.getRandomString20;
import static java.net.HttpURLConnection.*;
import static java.util.Collections.emptyMap;

/**
 * Created by Aleksey Popryaduhin on 23:59 27/09/2017.
 */
public class OrchestralService {

  private static final Logger logger = LoggerFactory.getLogger(OrchestralService.class);

  private String register;
  private String authorize;
  private String authenticate;
  private String article;
  private String boardbox;
  private String boardboxDeleteByArticleId;
  private String parsePdn;
  private String userInfo;
  private String saveUserInfo;
  private String logout;

  public OrchestralService(ModuleProperties moduleProperties) {
    register = moduleProperties.registerResource();
    authorize = moduleProperties.authorizeResource();
    authenticate = moduleProperties.authenticateResource();
    article = moduleProperties.articleResource();
    boardbox = moduleProperties.boardboxResource();
    boardboxDeleteByArticleId = moduleProperties.boardboxDeleteByArticleIdResource();
    parsePdn = moduleProperties.parsePdnResource();
    userInfo = moduleProperties.userInfoResource();
    saveUserInfo = moduleProperties.saveUserInfoResource();
    logout = moduleProperties.logoutResource();
    UnirestUtil.configureSerialization();
  }

  public Optional<AuthUser> register(RegisteredUser userCredentials) {
    return registerAnswer(userCredentials).map(answer -> ((AuthUser) answer.getBody()));
  }

  public Optional<Answer> registerAnswer(RegisteredUser userCredentials) {
    return post(register, userCredentials, emptyMap());
  }

  public Optional<AuthUser> authorize(UserCredentials userCredentials) {
    return authorizeAnswer(userCredentials).map(answer -> ((AuthUser) answer.getBody()));
  }

  public Optional<Answer> authorizeAnswer(UserCredentials userCredentials) {
    return post(authorize, userCredentials, emptyMap());
  }

  public Optional<AuthUser> authenticate(@NotNull AuthUser authUser) {
    return authenticateAnswer(authUser).map(answer -> ((AuthUser) answer.getBody()));
  }

  public Optional<Answer> authenticateAnswer(@NotNull AuthUser authUser) {
    Map<String, String> headers = getAuthHeaders(authUser);
    return get(authenticate, headers);
  }

  public Optional<CreateArticleResponse> createArticle(CreateArticlePayload articlePayload, @NotNull AuthUser authUser) {
    return createArticleAnswer(articlePayload, authUser).map(answer -> ((CreateArticleResponse) answer.getBody()));
  }

  public Optional<Answer> createArticleAnswer(CreateArticlePayload articlePayload, @NotNull AuthUser authUser) {
    Map<String, String> headers = getAuthHeaders(authUser);
    return post(article, articlePayload, headers);
  }

  public Optional<Article> saveArticle(Article articlePayload, @NotNull AuthUser authUser) {
    return saveArticleAnswer(articlePayload, authUser).map(answer -> ((Article) answer.getBody()));
  }

  public Optional<Answer> saveArticleAnswer(Article articlePayload, AuthUser authUser) {
    Map<String, String> headers = getAuthHeaders(authUser);
    return put(article, articlePayload, headers);
  }

  public Optional<BoardBox> createBoardBox(CreateBoardPayload boardRequest, @NotNull AuthUser authUser) {
    return createBoardBoxAnswer(boardRequest, authUser).map(answer -> ((BoardBox) answer.getBody()));
  }

  public Optional<Answer> createBoardBoxAnswer(CreateBoardPayload boardRequest, @NotNull AuthUser authUser) {
    Map<String, String> headers = getAuthHeaders(authUser);
    return post(boardbox, boardRequest, headers);
  }

  public Optional<ResultPayload> deleteBoardBoxesByArticleId(DomainId articleId, AuthUser authUser) {
    return deleteBoardBoxesByArticleIdAnswer(articleId, authUser).map(answer -> ((ResultPayload) answer.getBody()));
  }

  public Optional<Answer> deleteBoardBoxesByArticleIdAnswer(DomainId articleId, AuthUser authUser) {
    Map<String, String> headers = getAuthHeaders(authUser);
    return post(boardboxDeleteByArticleId, articleId, headers);
  }

  public Optional<BoardBox> parsePdn(ImportPdnPayload boardRequest, @NotNull AuthUser authUser) {
    return parsePdnAnswer(boardRequest, authUser).map(answer -> ((BoardBox) answer.getBody()));
  }

  public Optional<Answer> parsePdnAnswer(ImportPdnPayload boardRequest, @NotNull AuthUser authUser) {
    Map<String, String> headers = getAuthHeaders(authUser);
    return post(parsePdn, boardRequest, headers);
  }

  public Optional<UserInfo> userInfo(@NotNull AuthUser authUser) {
    return userInfoAnswer(authUser).map(answer -> ((UserInfo) answer.getBody()));
  }

  public Optional<Answer> userInfoAnswer(@NotNull AuthUser authUser) {
    Map<String, String> authHeaders = getAuthHeaders(authUser);
    return post(userInfo, authUser, authHeaders);
  }

  public Optional<UserInfo> saveUserInfo(UserInfo userInfo, @NotNull AuthUser authUser) {
    return saveUserInfoAnswer(userInfo, authUser).map(answer -> ((UserInfo) answer.getBody()));
  }

  public Optional<Answer> saveUserInfoAnswer(UserInfo userInfo, @NotNull AuthUser authUser) {
    Map<String, String> authHeaders = getAuthHeaders(authUser);
    return post(saveUserInfo, userInfo, authHeaders);
  }

  public Optional<AuthUser> logout(@NotNull AuthUser authUser) {
    return logoutAnswer(authUser).map(answer -> ((AuthUser) answer.getBody()));
  }

  public Optional<Answer> logoutAnswer(@NotNull AuthUser authUser) {
    Map<String, String> headers = getAuthHeaders(authUser);
    return get(logout, headers);
  }

  @NotNull
  @SuppressWarnings("unchecked")
  public Optional<Answer> internal(@NotNull AuthUser authUser, @NotNull String methodName, @NotNull Object... params) {
    AuthUser authUserInternal = authUser.deepClone();
    String internalKey = getRandomString20();
    authUser.setInternalKey(internalKey);
    authUserInternal.setInternalKey(internalKey);
    authUserInternal.addAuthorities(EnumAuthority.INTERNAL);
    putInternalRequest(internalKey, authUserInternal);
    try {
      Class[] classParams = Arrays.stream(params).map(Object::getClass).toArray(Class[]::new);
      Method method = getClass().getMethod(methodName, classParams);
      return (Optional<Answer>) method.invoke(this, params);
    } catch (InvocationTargetException te) {
      Throwable targetException = te.getTargetException();
      if (targetException instanceof RequestException) {
        if (((RequestException) targetException).getCode() == HTTP_FORBIDDEN) {
          logger.warn(targetException.getMessage());
        } else {
          logger.error("INTERNAL REQUEST EXCEPTION: " + te.getMessage());
        }
        throw ((RequestException) targetException);
      }
      throw RequestException.forbidden();
    } catch (@NotNull NoSuchMethodException | IllegalAccessException e) {
      logger.error("INTERNAL CALL FAIL: " + e.getMessage());
      throw RequestException.forbidden();
    }
  }

  private Optional<Answer> internalCall(AuthUser authUser, String internalKey, BiFunction<AuthUser, String, Optional<Answer>> function) {
    return function.apply(authUser, internalKey);
  }

  private Map<String, String> getAuthHeaders(AuthUser authUser) {
    return new HashMap<>() {{
      DomainId userId = authUser.getUserId();
      if (userId != null) {
        put(RequestConstants.USER_ID_HEADER, userId.getId());
        put(RequestConstants.USER_CREATED_AT_HEADER, userId.getCreatedAt().format(DATE_TIME_FORMATTER));
      }
      String username = authUser.getEmail();
      if (StringUtils.isNotBlank(username)) {
        put(RequestConstants.USERNAME_HEADER, username);
      }
      put(RequestConstants.ACCESS_TOKEN_HEADER, authUser.getAccessToken());
      put(RequestConstants.USER_SESSION_HEADER, authUser.getUserSession());
      put(RequestConstants.INTERNAL_KEY_HEADER, authUser.getInternalKey());
    }};
  }

  @SuppressWarnings("unchecked")
  public Optional<Answer> post(String resource, Payload payload, Map<String, String> headers) {
    try {
      HttpResponse<Answer> response = Unirest.post(resource)
          .headers(headers)
          .body(payload)
          .asObject(Answer.class);
      if (response.getStatus() == HTTP_OK || response.getStatus() == HTTP_CREATED) {
        return Optional.of(response.getBody());
      }
      throw RequestException.requestException(response.getBody().getMessage());
    } catch (UnirestException e) {
      logger.error("Unirest exception", e);
    }
    return Optional.empty();
  }

  @SuppressWarnings("unchecked")
  public Optional<Answer> put(String resource, Payload payload, Map<String, String> headers) {
    try {
      HttpResponse<Answer> response = Unirest.put(resource)
          .headers(headers)
          .body(payload)
          .asObject(Answer.class);
      if (response.getStatus() == HTTP_OK || response.getStatus() == HTTP_CREATED) {
        return Optional.of(response.getBody());
      }
      throw RequestException.requestException(response.getBody().getMessage());
    } catch (UnirestException e) {
      logger.error("Unirest exception", e);
    }
    return Optional.empty();
  }

  @SuppressWarnings("unchecked")
  public Optional<Answer> get(String resource, Map<String, String> headers) {
    try {
      HttpResponse<Answer> response = Unirest.get(resource)
          .headers(headers)
          .asObject(Answer.class);
      if (response.getStatus() == HTTP_OK || response.getStatus() == HTTP_CREATED) {
        return Optional.of(response.getBody());
      }
      throw RequestException.forbidden();
    } catch (UnirestException e) {
      logger.error("Unirest exception", e);
    }
    return Optional.empty();
  }

  public void cacheRequest(@NotNull Request request, @NotNull Answer answer, @NotNull AuthUser authUser) {
    String key = getRequestKey(request);
    if (answer.getBody() != null) {
//      RedisUtil.cacheRequest(key, answer.getBody());
    }
    RedisUtil.cacheToken(authUser);
  }

  private String getRequestKey(Request request) {
    String url = request.url();
    String queryParams = request.queryParams().stream().collect(Collectors.joining());
    return url + queryParams;
  }

  @Nullable
  public String checkTokenCache(@NotNull AuthUser authUser) {
    return RedisUtil.getTokenCache(authUser);
  }

  public void cacheSecureAuth(@NotNull SecureAuth auth) {
    RedisUtil.cacheSecureAuth(auth.getUserSession(), auth);
    RedisUtil.cacheSecureAuthUsername(auth.getEmail(), auth.getUserSession());
  }

  public SecureAuth getSecureAuth(String userSession) {
    if (StringUtils.isBlank(userSession)) {
      return null;
    }
    return RedisUtil.getSecureAuthByUserSession(userSession);
  }

  @Nullable
  public SecureAuth getSecureAuthByUsername(String username) {
    return RedisUtil.getSecureAuthByUsername(username);
  }

  public void removeSecureAuth(@NotNull AuthUser authUser) {
    RedisUtil.removeSecureAuthByUserSession(authUser.getUserSession());
    RedisUtil.removeSecureAuthByUserUsername(authUser.getEmail());
  }
}
