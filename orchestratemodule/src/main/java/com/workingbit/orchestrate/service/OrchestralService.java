package com.workingbit.orchestrate.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.workingbit.orchestrate.config.ShareProperties;
import com.workingbit.orchestrate.util.RedisUtil;
import com.workingbit.share.common.RequestConstants;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumAuthority;
import com.workingbit.share.util.UnirestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.workingbit.orchestrate.util.RedisUtil.putInternalRequest;
import static com.workingbit.share.util.Utils.getRandomString20;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;
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
  private String userInfo;
  private String saveUserInfo;
  private String logout;

  public OrchestralService(ShareProperties shareProperties) {
    register = shareProperties.registerResource();
    authorize = shareProperties.authorizeResource();
    authenticate = shareProperties.authenticateResource();
    article = shareProperties.articleResource();
    boardbox = shareProperties.boardboxResource();
    userInfo = shareProperties.userInfoResource();
    saveUserInfo = shareProperties.saveUserInfoResource();
    logout = shareProperties.logoutResource();
    UnirestUtil.configureSerialization();
  }

  public Optional<AuthUser> register(UserCredentials userCredentials) throws RequestException {
    return registerAnswer(userCredentials).map(answer -> ((AuthUser) answer.getBody()));
  }

  public Optional<Answer> registerAnswer(UserCredentials userCredentials) throws RequestException {
    return post(register, userCredentials, emptyMap());
  }

  public Optional<AuthUser> authorize(UserCredentials userCredentials) throws RequestException {
    return authorizeAnswer(userCredentials).map(answer -> ((AuthUser) answer.getBody()));
  }

  public Optional<Answer> authorizeAnswer(UserCredentials userCredentials) throws RequestException {
    return post(authorize, userCredentials, emptyMap());
  }

  public Optional<AuthUser> authenticate(AuthUser authUser) throws RequestException {
    return authenticateAnswer(authUser).map(answer -> ((AuthUser) answer.getBody()));
  }

  public Optional<Answer> authenticateAnswer(AuthUser authUser) throws RequestException {
    Map<String, String> headers = getAuthHeaders(authUser);
    return get(authenticate, headers);
  }

  public Optional<Article> createArticle(CreateArticlePayload articlePayload, AuthUser authUser) throws RequestException {
    return createArticleAnswer(articlePayload, authUser).map(answer -> ((Article) answer.getBody()));
  }

  public Optional<Answer> createArticleAnswer(CreateArticlePayload articlePayload, AuthUser authUser) throws RequestException {
    Map<String, String> headers = getAuthHeaders(authUser);
    return post(article, articlePayload, headers);
  }

  public Optional<BoardBox> createBoardBox(CreateBoardPayload boardRequest, AuthUser authUser) throws RequestException {
    return createBoardBoxAnswer(boardRequest, authUser).map(answer -> ((BoardBox) answer.getBody()));
  }

  public Optional<Answer> createBoardBoxAnswer(CreateBoardPayload boardRequest, AuthUser authUser) throws RequestException {
    Map<String, String> headers = getAuthHeaders(authUser);
    return post(boardbox, boardRequest, headers);
  }

  public Optional<UserInfo> userInfo(AuthUser authUser) throws RequestException {
    return userInfoAnswer(authUser).map(answer -> ((UserInfo) answer.getBody()));
  }

  public Optional<Answer> userInfoAnswer(AuthUser authUser) throws RequestException {
    Map<String, String> authHeaders = getAuthHeaders(authUser);
    return post(userInfo, authUser, authHeaders);
  }

  public Optional<UserInfo> saveUserInfo(UserInfo userInfo, AuthUser authUser) throws RequestException {
    return saveUserInfoAnswer(userInfo, authUser).map(answer -> ((UserInfo) answer.getBody()));
  }

  public Optional<Answer> saveUserInfoAnswer(UserInfo userInfo, AuthUser authUser) throws RequestException {
    Map<String, String> authHeaders = getAuthHeaders(authUser);
    return post(saveUserInfo, userInfo, authHeaders);
  }

  public Optional<AuthUser> logout(AuthUser authUser) throws RequestException {
    return logoutAnswer(authUser).map(answer -> ((AuthUser) answer.getBody()));
  }

  public Optional<Answer> logoutAnswer(AuthUser authUser) throws RequestException {
    Map<String, String> headers = getAuthHeaders(authUser);
    return get(logout, headers);
  }

  public Optional<Answer> internal(AuthUser authUser, BiFunction<AuthUser, String, Optional<Answer>> function) {
    AuthUser authUserInternal = authUser.deepClone();
    String internalKey = getRandomString20();
    authUser.setInternalKey(internalKey);
    authUserInternal.setInternalKey(internalKey);
    authUserInternal.addAuthorities(EnumAuthority.INTERNAL);
    putInternalRequest(internalKey, authUserInternal);
    return function.apply(authUserInternal, internalKey);
  }

  private Map<String, String> getAuthHeaders(AuthUser authUser) {
    return new HashMap<>() {{
      put(RequestConstants.ACCESS_TOKEN_HEADER, authUser.getAccessToken());
      put(RequestConstants.USER_SESSION_HEADER, authUser.getUserSession());
      put(RequestConstants.INTERNAL_KEY_HEADER, authUser.getInternalKey());
    }};
  }

  @SuppressWarnings("unchecked")
  public Optional<Answer> post(String resource, Payload payload, Map<String, String> headers) throws RequestException {
    try {
      HttpResponse<Answer> response = Unirest.post(resource)
          .headers(headers)
          .body(payload)
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

  @SuppressWarnings("unchecked")
  public Optional<Answer> get(String resource, Map<String, String> headers) throws RequestException {
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

  public void cacheRequest(Request request, Answer answer, AuthUser authUser) {
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

  public String checkTokenCache(AuthUser authUser) {
    return RedisUtil.checkTokenCache(authUser);
  }
}
