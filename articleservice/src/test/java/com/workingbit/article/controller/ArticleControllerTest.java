package com.workingbit.article.controller;

import com.despegar.http.client.*;
import com.despegar.sparkjava.test.SparkServer;
import com.workingbit.article.ArticleEmbedded;
import com.workingbit.share.client.ShareRemoteClient;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.*;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import spark.servlet.SparkApplication;

import java.util.*;
import java.util.stream.Collectors;

import static com.workingbit.share.common.RequestConstants.ACCESS_TOKEN_HEADER;
import static com.workingbit.share.common.RequestConstants.USER_SESSION_HEADER;
import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;
import static com.workingbit.share.util.Utils.getRandomString;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static org.junit.Assert.*;

/**
 * Created by Aleksey Popryaduhin on 17:47 01/10/2017.
 */
public class ArticleControllerTest {

  private static String articleUrl = "/api/v1/article";
  private static Integer randomPort = RandomUtils.nextInt(1000, 65000);

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  public static class ArticleControllerTestSparkApplication implements SparkApplication {

    @Override
    public void init() {
      ArticleEmbedded.start();
    }
  }

  @ClassRule
  public static SparkServer<ArticleControllerTestSparkApplication> testServer = new SparkServer<>(ArticleControllerTestSparkApplication.class, randomPort);

  private AuthUser register() throws Exception {
    String username = Utils.getRandomString();
    String password = Utils.getRandomString();
    RegisterUser registerUser = new RegisterUser(username, password);
    AuthUser registered = ShareRemoteClient.Singleton.getInstance().register(registerUser).get();
    assertNotNull(registered);

    return registered;
  }

  /**
   * test for createArticleAndBoard
   *
   * @throws Exception
   */
  @Test
  public void create_article() throws Exception {
    try {
      CreateArticlePayload createArticlePayload = getCreateArticlePayload();
      // can't create
      Optional<CreateArticleResponse> articleResponseOpt = ShareRemoteClient.Singleton.getInstance().createArticle(createArticlePayload, new AuthUser());
      assertFalse(articleResponseOpt.isPresent());

      AuthUser headers = register();
      articleResponseOpt = ShareRemoteClient.Singleton.getInstance().createArticle(createArticlePayload, headers);

      assertTrue(articleResponseOpt.isPresent());
      CreateArticleResponse articleResponse = articleResponseOpt.get();
      Article article = articleResponse.getArticle();
      BoardBox board = articleResponse.getBoard();
      assertNotNull(article.getId());
      assertNotNull(article.getBoardBoxId());
      assertNotNull(board.getId());
    } catch (Exception e) {
      assertFalse(e.getMessage(), false);
      e.printStackTrace();
    }
  }

  /**
   * test for saveArticle
   *
   * @throws Exception
   */
  @Test
  public void save_article() throws Exception {
    CreateArticlePayload createArticlePayload = getCreateArticlePayload();
    AuthUser headers = register();
    CreateArticleResponse articleResponse = (CreateArticleResponse) post("", createArticlePayload, headers).getBody();

    Article article = articleResponse.getArticle();
    String title = getRandomString();
    article.setTitle(title);
    String content = getRandomString();
    article.setContent(content);
    AuthUser articleNotAuthorized = (AuthUser) put("", article).getBody();
    assertTrue(articleNotAuthorized.getRoles().contains(EnumSecureRole.ANONYMOUS));

    UserInfo userInfo = ShareRemoteClient.Singleton.getInstance().userInfo(headers).get();
    userInfo.addRole(EnumSecureRole.BAN);
    UserInfo savedUserInfo = ShareRemoteClient.Singleton.getInstance().saveUserInfo(userInfo, headers).get();
    assertTrue(savedUserInfo.getRoles().contains(EnumSecureRole.BAN));
    articleNotAuthorized = (AuthUser) put("", article, headers).getBody();
    assertTrue(articleNotAuthorized.getRoles().contains(EnumSecureRole.ANONYMOUS));

    userInfo.setRoles(headers.getRoles());
    boolean bannedUserInfo1 = ShareRemoteClient.Singleton.getInstance().saveUserInfo(userInfo, headers).isPresent();
    assertFalse(bannedUserInfo1);
    assertFalse(articleNotAuthorized.getRoles().contains(EnumSecureRole.BAN));

    articleNotAuthorized = (AuthUser) put("", article, headers).getBody();
    assertTrue(articleNotAuthorized.getRoles().contains(EnumSecureRole.ANONYMOUS));

    AuthUser admin = register();
    UserInfo adminInfo = ShareRemoteClient.Singleton.getInstance().userInfo(admin).get();
    adminInfo.addRole(EnumSecureRole.ADMIN);
    ShareRemoteClient.Singleton.getInstance().saveUserInfo(adminInfo, admin);

    // unban
    userInfo.setRoles(singleton(EnumSecureRole.AUTHOR));
    ShareRemoteClient.Singleton.getInstance().saveUserInfo(userInfo, admin);

    String newTitle = Utils.getRandomString();
    String newContent = Utils.getRandomString();
    article.setTitle(newTitle);
    article.setContent(newContent);
    article = (Article) put("", article, headers).getBody();

    article = (Article) put("", article, headers).getBody();

    assertEquals(newTitle, article.getTitle());

    article = (Article) get("/" + article.getId()).getBody();
    assertEquals(article.getTitle(), newTitle);
    assertNotNull(article.getContent(), newContent);
  }

  /**
   * test for findArticleById
   *
   * @throws Exception
   */
  @Test
  public void find_article_by_id() throws Exception {
    CreateArticlePayload createArticlePayload = getCreateArticlePayload();
    AuthUser headers = register();

    CreateArticleResponse articleResponse = (CreateArticleResponse) post("", createArticlePayload, headers).getBody();
    Article article = articleResponse.getArticle();
    BoardBox board = articleResponse.getBoard();
    assertNotNull(article.getId());
    assertNotNull(article.getBoardBoxId());
    assertNotNull(board.getId());

    article = (Article) get("/" + article.getId()).getBody();
    assertNotNull(article);

    article = (Article) get("/" + article.getId()).getBody();
    assertNotNull(article);
  }

  /**
   * test for findAllArticles
   *
   * @throws Exception
   */
  @Test
  public void find_all() throws Exception {
    CreateArticlePayload createArticlePayload = getCreateArticlePayload();
    String username = Utils.getRandomString();
    String password = Utils.getRandomString();
    RegisterUser registerUser = new RegisterUser(username, password);
    AuthUser registered = ShareRemoteClient.Singleton.getInstance().register(registerUser).get();
    assertNotNull(registered);

    CreateArticleResponse articleResponse = (CreateArticleResponse) post("", createArticlePayload, registered).getBody();
    Article article = articleResponse.getArticle();
    BoardBox board = articleResponse.getBoard();
    assertNotNull(article.getId());
    assertNotNull(article.getBoardBoxId());
    assertNotNull(board.getId());

    Articles articles = (Articles) get("s").getBody();
    String articleId = article.getId();
    article = articles.getArticles().stream().filter((article1 -> article1.getId().equals(articleId))).findFirst().get();
    assertNotNull(article);

    articles = (Articles) get("s").getBody();
    article = articles.getArticles().stream().filter((article1 -> article1.getId().equals(articleId))).findFirst().get();
    assertNotNull(article);

    AuthUser loggedout = ShareRemoteClient.Singleton.getInstance().logout(registered).get();
    assertEquals(EnumSecureRole.ANONYMOUS, loggedout.getRoles());

    createArticlePayload = getCreateArticlePayload();
    int statusCode = post("", createArticlePayload, registered).getStatusCode();
    assertEquals(403, statusCode);

    AuthUser loggedIn = ShareRemoteClient.Singleton.getInstance().authorize(registerUser).get();
    assertEquals(EnumSecureRole.AUTHOR, loggedIn.getRoles());

    loggedIn = ShareRemoteClient.Singleton.getInstance().authenticate(loggedIn).get();
    assertNotNull(loggedIn);
  }

  private CreateArticlePayload getCreateArticlePayload() {
    CreateArticlePayload createArticlePayload = CreateArticlePayload.createArticlePayload();
    Article article = new Article(getRandomString(), getRandomString(), getRandomString());
    createArticlePayload.setArticle(article);
    CreateBoardPayload createBoardPayload = CreateBoardPayload.createBoardPayload();
    createBoardPayload.setBlack(false);
    createBoardPayload.setRules(EnumRules.RUSSIAN);
    createBoardPayload.setFillBoard(false);
    createArticlePayload.setBoardRequest(createBoardPayload);
    return createArticlePayload;
  }

  private Answer post(String path, Object payload, AuthUser authUser) throws HttpClientException {
    Map<String, String> headers = new HashMap<String, String>() {{
      put(ACCESS_TOKEN_HEADER, authUser.getAccessToken());
      put(USER_SESSION_HEADER, authUser.getUserSession());
    }};
    return post(path, payload, headers);
  }

  private Answer post(String path, Object payload, Map<String, String> headers) throws HttpClientException {
    PostMethod resp = testServer.post(articleUrl + path, dataToJson(payload), false);
    headers.forEach(resp::addHeader);
    HttpResponse execute = testServer.execute(resp);
    return jsonToData(new String(execute.body()), Answer.class);
  }

  private Answer post(String path, Object payload) throws HttpClientException {
    return post(path, payload, emptyMap());
  }

  private Answer put(String path, Article payload, AuthUser authUser) throws HttpClientException {
    Map<String, String> headers = new HashMap<String, String>() {{
      put(ACCESS_TOKEN_HEADER, authUser.getAccessToken());
      put(USER_SESSION_HEADER, authUser.getUserSession());
    }};
    PutMethod resp = testServer.put(articleUrl + path, dataToJson(payload), false);
    headers.forEach(resp::addHeader);
    HttpResponse execute = testServer.execute(resp);
    return jsonToData(new String(execute.body()), Answer.class);
  }

  private Answer put(String path, Object payload) throws HttpClientException {
    PutMethod resp = testServer.put(articleUrl + path, dataToJson(payload), false);
    HttpResponse execute = testServer.execute(resp);
    return jsonToData(new String(execute.body()), Answer.class);
  }

  private Answer get(String params) throws HttpClientException {
    GetMethod resp = testServer.get(articleUrl + params, false);
    HttpResponse execute = testServer.execute(resp);
    return jsonToData(new String(execute.body()), Answer.class);
  }

  class MyClass {
    String field;

    MyClass(String field) {
      this.field = field;
    }
  }

  @Test
  public void testTypeCast() {
    List<Object> objectList = Arrays.asList(new MyClass("1"), new MyClass("2"));

    Class<MyClass> clazz = MyClass.class;
    List<MyClass> myClassList = objectList.stream()
        .map(clazz::cast)
        .collect(Collectors.toList());

    assertEquals(objectList.size(), myClassList.size());
    assertEquals(objectList, myClassList);
  }

}