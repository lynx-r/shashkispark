package com.workingbit.share.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.workingbit.share.common.ShareProperties;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.*;
import com.workingbit.share.util.UnirestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.workingbit.share.common.Config4j.configurationProvider;
import static com.workingbit.share.common.RequestConstants.ACCESS_TOKEN;
import static com.workingbit.share.common.RequestConstants.USER_SESSION;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Created by Aleksey Popryaduhin on 23:59 27/09/2017.
 */
public class ShareRemoteClient {

  private static final Logger logger = LoggerFactory.getLogger(ShareRemoteClient.class);

  private static String register;
  private static String authorize;
  private static String authenticate;
  private static String article;
  private static String articles;
  private static String boardbox;
  private static String userInfo;

  static {
    ShareProperties shareProperties = configurationProvider("shareconfig.yaml").bind("app", ShareProperties.class);
    register = shareProperties.registerResource();
    authorize = shareProperties.authorizeResource();
    authenticate = shareProperties.authenticateResource();
    article = shareProperties.articleResource();
    articles = shareProperties.articlesResource();
    boardbox = shareProperties.boardboxResource();
    userInfo = shareProperties.userInfoResource();
    UnirestUtil.configureSerialization();
  }

  public static ShareRemoteClient getInstance() {
    return new ShareRemoteClient();
  }

  public Optional<AuthUser> register(RegisterUser registerUser) {
    return register(registerUser, Collections.emptyMap());
  }

  public Optional<AuthUser> register(RegisterUser registerUser, Map<String, String> headers) {
    return post(register, registerUser, headers);
  }

  public Optional<AuthUser> authorize(RegisterUser registerUser) {
    return authorize(registerUser, Collections.emptyMap());
  }

  public Optional<AuthUser> authorize(RegisterUser registerUser, Map<String, String> headers) {
    return post(authorize, registerUser, headers);
  }

  public Optional<AuthUser> authenticate(AuthUser authUser) {
    Map<String, String> headers = createAuthHeaders(authUser);
    return authenticate(authUser, headers);
  }

  public Optional<AuthUser> authenticate(AuthUser authUser, Map<String, String> headers) {
    return post(authenticate, authUser, headers);
  }

  public Optional<CreateArticleResponse> createArticle(CreateArticlePayload articlePayload, AuthUser authUser) {
    Map<String, String> headers = createAuthHeaders(authUser);
    return post(article, articlePayload, headers);
  }

  public Optional<CreateArticleResponse> createArticle(CreateArticlePayload articlePayload, Map<String, String> headers) {
    return post(article, articlePayload, headers);
  }

  public Optional<BoardBox> createBoardBox(CreateBoardPayload boardRequest, AuthUser authUser) {
    Map<String, String> headers = createAuthHeaders(authUser);
    return post(boardbox, boardRequest, headers);
  }

  public Optional<BoardBox> createBoardBox(CreateBoardPayload boardRequest, Map<String, String> headers) {
    return post(boardbox, boardRequest, headers);
  }

  private Map<String, String> createAuthHeaders(AuthUser authUser) {
    return new HashMap<String, String>() {{
      put(ACCESS_TOKEN, authUser.getAccessToken());
      put(USER_SESSION, authUser.getUserSession());
    }};
  }

  @SuppressWarnings("unchecked")
  public  <T> Optional<T> post(String resource, Payload payload, Map<String, String> headers) {
    try {
      HttpResponse<Answer> response = Unirest.post(resource)
          .headers(headers)
          .body(payload)
          .asObject(Answer.class);
      if (response.getStatus() == HTTP_OK || response.getStatus() == HTTP_CREATED) {
        Answer body = response.getBody();
        return Optional.of((T) body.getBody());
      }
      logger.error("Invalid status " + response.getStatus());
    } catch (UnirestException e) {
      logger.error("Unirest exception", e);
    }
    return Optional.empty();
  }

  public Optional<UserInfo> userInfo(AuthUser authUser) {
    Map<String, String> authHeaders = createAuthHeaders(authUser);
    return get(userInfo, authHeaders);
  }

  @SuppressWarnings("unchecked")
  public  <T> Optional<T> get(String resource, Map<String, String> headers) {
    try {
      HttpResponse<Answer> response = Unirest.get(resource)
          .headers(headers)
          .asObject(Answer.class);
      if (response.getStatus() == HTTP_OK || response.getStatus() == HTTP_CREATED) {
        Answer body = response.getBody();
        return Optional.of((T) body.getBody());
      }
      logger.error("Invalid status " + response.getStatus());
    } catch (UnirestException e) {
      logger.error("Unirest exception", e);
    }
    return Optional.empty();
  }
}
