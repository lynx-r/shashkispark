package com.workingbit.article.controller;

import com.despegar.http.client.*;
import com.despegar.sparkjava.test.SparkServer;
import com.workingbit.article.ArticleEmbedded;
import com.workingbit.share.dao.DaoFilters;
import com.workingbit.share.dao.Unary;
import com.workingbit.share.dao.ValueFilter;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumArticleStatus;
import com.workingbit.share.model.enumarable.EnumAuthority;
import com.workingbit.share.model.enumarable.EnumRules;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import spark.servlet.SparkApplication;
import spark.utils.IOUtils;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import static com.workingbit.article.config.Authority.*;
import static com.workingbit.orchestrate.OrchestrateModule.orchestralService;
import static com.workingbit.orchestrate.util.AuthRequestUtil.hasAuthorities;
import static com.workingbit.share.common.RequestConstants.*;
import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;
import static java.net.HttpURLConnection.*;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static org.junit.Assert.*;

/**
 * Created by Aleksey Popryaduhin on 17:47 01/10/2017.
 */
public class ArticleControllerTest {

  @NotNull
  private static String articleUrl = "/api/v1";
  private static Integer randomPort = RandomUtils.nextInt(1000, 65000);

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  public static class ArticleControllerTestSparkApplication implements SparkApplication {

    @Override
    public void init() {
      ArticleEmbedded.start();
    }
  }

  @NotNull
  @ClassRule
  public static SparkServer<ArticleControllerTestSparkApplication> testServer = new SparkServer<>(ArticleControllerTestSparkApplication.class, randomPort);

  @NotNull
  private AuthUser register() {
    String username = Utils.getRandomString20();
    String password = Utils.getRandomString20();
    RegisteredUser userCredentials = new RegisteredUser(Utils.getRandomString20(), Utils.getRandomString20(),
        Utils.getRandomString20(), EnumRank.MS, username, password);
    AuthUser registered = orchestralService
        .register(userCredentials)
        .get();
    assertNotNull(registered);

    return registered;
  }

  /**
   * local for createArticleAndBoard
   *
   * @throws Exception
   */
  @Test
  public void create_article() throws Exception {
    CreateArticlePayload createArticlePayload = getCreateArticlePayload();
    // can't createWithoutRoot
    post(ARTICLE_PROTECTED.getPath(), createArticlePayload, AuthUser.anonymous(), HTTP_FORBIDDEN);

    AuthUser headers = register();
    CreateArticleResponse articleAnswer = (CreateArticleResponse) post(ARTICLE_PROTECTED.getPath(), createArticlePayload, headers, HTTP_CREATED).getBody();

    assertNotNull(articleAnswer);
    Article article = articleAnswer.getArticle();
    BoardBox board = articleAnswer.getBoard();
    assertNotNull(article.getId());
    assertNotNull(article.getSelectedBoardBoxId());
    assertNotNull(board.getId());
  }

  /**
   * local for saveArticle
   *
   * @throws Exception
   */
  @Test
  public void save_article() throws Exception {
    CreateArticlePayload createArticlePayload = getCreateArticlePayload();
    AuthUser headers = register();
    Answer answer = post(ARTICLE_PROTECTED.getPath(), createArticlePayload, headers);
    assertEquals(HTTP_CREATED, answer.getStatusCode());

    Article article = ((CreateArticleResponse) answer.getBody()).getArticle();
    String title = Utils.getRandomString20();
    article.setTitle(title);
    String content = Utils.getRandomString20();
    article.setContent(content);
    MessageResponse articleNotAuthorized = put(ARTICLE_PROTECTED.getPath(), article).getMessage();
    assertEquals(HTTP_FORBIDDEN, articleNotAuthorized.getCode());

    UserInfo userInfo = orchestralService
        .userInfo(headers)
        .get();
    userInfo.addAuthority(EnumAuthority.REMOVED);
    answer = orchestralService.saveUserInfoAnswer(userInfo, headers).get();
    UserInfo savedUserInfo = (UserInfo) answer.getBody();
    headers = answer.getAuthUser();
    assertTrue(savedUserInfo.getAuthorities().contains(EnumAuthority.REMOVED));
    articleNotAuthorized = put(ARTICLE_PROTECTED.getPath(), article, headers).getMessage();
    assertEquals(HTTP_FORBIDDEN, articleNotAuthorized.getCode());

    headers = answer.getAuthUser();
    answer = orchestralService.authenticateAnswer(headers).get();
    assertEquals(HTTP_OK, answer.getStatusCode());
    headers = answer.getAuthUser();
    assertTrue(headers.getAuthorities().contains(EnumAuthority.REMOVED));

    answer = put(ARTICLE_PROTECTED.getPath(), article, headers);
    assertEquals(HTTP_FORBIDDEN, answer.getStatusCode());

    AuthUser admin = register();
    String superHash = System.getenv("SHASHKI_SUPER_USER");
    assertNotNull("Супер пароль не может быть пустым", superHash);
    admin.setSuperHash(superHash);
    UserInfo adminInfo = orchestralService.userInfo(admin).get();
    adminInfo.addAuthority(EnumAuthority.ADMIN);
    orchestralService.saveUserInfo(adminInfo, admin);

    // unban
    userInfo.setAuthorities(new HashSet<>(Set.of(EnumAuthority.AUTHOR)));
    answer = orchestralService.saveUserInfoAnswer(userInfo, admin).get();
    assertEquals(HTTP_OK, answer.getStatusCode());
    headers = answer.getAuthUser();

    String newTitle = Utils.getRandomString20();
    String newContent = Utils.getRandomString20();
    article.setTitle(newTitle);
    article.setContent(newContent);
    article = (Article) put(ARTICLE_PROTECTED.getPath(), article, headers).getBody();

    article = (Article) put(ARTICLE_PROTECTED.getPath(), article, headers).getBody();

    assertEquals(newTitle, article.getTitle());

    article = (Article) get(ARTICLE_PROTECTED.getPath() + "/" + article.getHumanReadableUrl()).getBody();
    assertEquals(article.getTitle(), newTitle);
    assertNotNull(article.getContent(), newContent);
  }

  /**
   * local for findArticleByHru
   *
   * @throws Exception
   */
  @Test
  public void find_article_by_id() throws Exception {
    CreateArticlePayload createArticlePayload = getCreateArticlePayload();
    AuthUser headers = register();

    CreateArticleResponse articleResponse = (CreateArticleResponse) post(ARTICLE_PROTECTED.getPath(), createArticlePayload, headers).getBody();
    Article article = articleResponse.getArticle();
    BoardBox board = articleResponse.getBoard();
    assertNotNull(article.getId());
    assertNotNull(article.getSelectedBoardBoxId());
    assertNotNull(board.getId());

    article = (Article) get(ARTICLE_PROTECTED.getPath() + "/" + article.getHumanReadableUrl()).getBody();
    assertNotNull(article);

    article = (Article) get(ARTICLE_PROTECTED.getPath() + "/" + article.getHumanReadableUrl()).getBody();
    assertNotNull(article);
  }

  @Test
  public void find_public_board_ids() throws Exception {
    CreateArticlePayload createArticlePayload = getCreateArticlePayload();
    AuthUser headers = register();

    Answer articleResponseAnswer = post(ARTICLE_PROTECTED.getPath(), createArticlePayload, headers);
    CreateArticleResponse articleResponse = (CreateArticleResponse) articleResponseAnswer.getBody();
    Article article = articleResponse.getArticle();
    BoardBox board = articleResponse.getBoard();
    assertNotNull(article.getId());
    assertNotNull(article.getSelectedBoardBoxId());
    assertNotNull(board.getId());
    headers = articleResponseAnswer.getAuthUser();

    var parsePdn = ImportPdnPayload.createBoardPayload();
    parsePdn.setArticleId(article.getDomainId());
    parsePdn.setRules(EnumRules.RUSSIAN);
    InputStream resourceAsStream = getClass().getResourceAsStream("/pdn/parse_simple.pdn");
    StringWriter writer = new StringWriter();
    IOUtils.copy(resourceAsStream, writer);
    parsePdn.setPdn(writer.toString());

    Answer answer = post(ARTICLE_IMPORT_PDN_PROTECTED.getPath(), parsePdn, headers, HTTP_CREATED);
    headers = answer.getAuthUser();
    articleResponse = (CreateArticleResponse) answer.getBody();
//    assertEquals(2, articleResponse.getArticle().getBoardBoxIds().size());
  }

  /**
   * local for findAllArticles
   *
   * @throws Exception
   */
  @Test
  public void find_all() throws Exception {
    CreateArticlePayload createArticlePayload = getCreateArticlePayload();
    String username = Utils.getRandomString20();
    String password = Utils.getRandomString20();
    RegisteredUser registeredUser = new RegisteredUser(Utils.getRandomString20(), Utils.getRandomString20(),
        Utils.getRandomString20(), EnumRank.MS, username, password);
    AuthUser registered = orchestralService.register(registeredUser).get();
    assertNotNull(registered);

    CreateArticleResponse articleResponse = (CreateArticleResponse) post(ARTICLE_PROTECTED.getPath(), createArticlePayload, registered).getBody();
    Article article = articleResponse.getArticle();
    BoardBox board = articleResponse.getBoard();
    assertNotNull(article.getId());
    assertNotNull(article.getSelectedBoardBoxId());
    assertNotNull(board.getId());

    article.setArticleStatus(EnumArticleStatus.PUBLISHED);
    article = (Article) put(ARTICLE_PROTECTED.getPath(), article, registered).getBody();

    Articles articles = (Articles) get(ARTICLES.getPath()).getBody();
    String articleId = article.getId();
    article = articles.getArticles().stream().filter((article1 -> article1.getId().equals(articleId))).findFirst().get();
    assertNotNull(article);

    articles = (Articles) get(ARTICLES.getPath()).getBody();
    article = articles.getArticles().stream().filter((article1 -> article1.getId().equals(articleId))).findFirst().get();
    assertNotNull(article);

    AuthUser loggedout = orchestralService
        .logout(registered)
        .get();
    assertTrue(hasAuthorities(singleton(EnumAuthority.ANONYMOUS), loggedout.getAuthorities()));

    createArticlePayload = getCreateArticlePayload();
    int statusCode = post(ARTICLE_PROTECTED.getPath(), createArticlePayload, registered).getStatusCode();
    assertEquals(HTTP_FORBIDDEN, statusCode);

    UserCredentials userCredentials = new UserCredentials(registered.getEmail(), password);
    AuthUser loggedIn = orchestralService.authorize(userCredentials).get();
    assertTrue(!hasAuthorities(singleton(EnumAuthority.AUTHOR), loggedout.getAuthorities()));

    orchestralService.authenticate(loggedIn).get();
    assertTrue(hasAuthorities(singleton(EnumAuthority.ANONYMOUS), loggedout.getAuthorities()));
  }

  @Test
  public void find_with_filter() throws Exception {
    CreateArticlePayload createArticlePayload = getCreateArticlePayload();
    String username = Utils.getRandomString20();
    String password = Utils.getRandomString20();
    RegisteredUser userCredentials = new RegisteredUser(Utils.getRandomString20(), Utils.getRandomString20(),
        Utils.getRandomString20(), EnumRank.MS, username, password);
    AuthUser registered = orchestralService.register(userCredentials).get();
    assertNotNull(registered);

    post(ARTICLE_PROTECTED.getPath(), createArticlePayload, registered).getBody();
    DaoFilters filters = new DaoFilters();
    filters.add(new ValueFilter("articleStatus", "DRAFT", " = ", "S"));
    registered.parseFilters(dataToJson(filters));
    Answer answer = get(ARTICLES.getPath(), registered, HTTP_OK);
    Articles articles = (Articles) answer.getBody();
    assertEquals(1, articles.getArticles().size());
    registered = answer.getAuthUser();

    createArticlePayload = getCreateArticlePayload();
    CreateArticleResponse articleResponse = (CreateArticleResponse) post(ARTICLE_PROTECTED.getPath(), createArticlePayload, registered).getBody();
    Article article = articleResponse.getArticle();

    article.setArticleStatus(EnumArticleStatus.PUBLISHED);
    article = (Article) put(ARTICLE_PROTECTED.getPath(), article, registered).getBody();

    filters = new DaoFilters();
    filters.add(new ValueFilter("articleStatus", "PUBLISHED", " = ", "S"));
    registered.parseFilters(dataToJson(filters));
    answer = get(ARTICLES.getPath(), registered, HTTP_OK);
    articles = (Articles) answer.getBody();
    assertEquals(1, articles.getArticles().size());
    registered = answer.getAuthUser();

    filters = new DaoFilters();
    filters.add(new Unary("("));
    filters.add(new ValueFilter("articleStatus", "DRAFT", " = ", "S"));
    filters.add(new Unary("or"));
    filters.add(new ValueFilter("articleStatus", "PUBLISHED", " = ", "S"));
    filters.add(new Unary(")"));
    registered.parseFilters(dataToJson(filters));
    articles = (Articles) get(ARTICLES.getPath(), registered, HTTP_OK).getBody();
    assertEquals(2, articles.getArticles().size());
  }

  @NotNull
  private CreateArticlePayload getCreateArticlePayload() {
    CreateArticlePayload createArticlePayload = CreateArticlePayload.createArticlePayload();
    Article article = new Article(Utils.getRandomString20(), Utils.getRandomString20(),  Utils.getRandomString20(), Utils.getRandomString20());
    Utils.setArticleUrlAndIdAndCreatedAt(article, false);
    createArticlePayload.setArticle(article);
    CreateBoardPayload createBoardPayload = new CreateBoardPayload();
    createBoardPayload.setBlack(false);
    createBoardPayload.setRules(EnumRules.RUSSIAN);
    createBoardPayload.setFillBoard(false);
    createArticlePayload.setBoardRequest(createBoardPayload);
    return createArticlePayload;
  }

  private Answer post(String path, Object payload, AuthUser authUser) throws HttpClientException {
    Map<String, String> headers = new HashMap<>() {{
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
    Map<String, String> headers = new HashMap<>() {{
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

  private Answer post(String path, Object payload, @Nullable AuthUser authUser, int expectCode) throws HttpClientException {
    PostMethod resp = testServer.post(articleUrl + path, dataToJson(payload), false);
    if (authUser != null) {
      if (StringUtils.isNotBlank(authUser.getAccessToken())) {
        resp.addHeader(ACCESS_TOKEN_HEADER, authUser.getAccessToken());
      }
      if (StringUtils.isNotBlank(authUser.getUserSession())) {
        resp.addHeader(USER_SESSION_HEADER, authUser.getUserSession());
      }
      resp.addHeader(FILTERS_HEADER, dataToJson(authUser.getFilters()));
    }
    HttpResponse execute = testServer.execute(resp);
    assertEquals(expectCode, execute.code());
    Answer answer = jsonToData(new String(execute.body()), Answer.class);
    assertEquals(expectCode, answer.getStatusCode());
    return answer;
  }

  private Answer post(String path, Object payload, @Nullable AuthUser authUser, int expectCode, @NotNull String[] errors) throws HttpClientException {
    PostMethod resp = testServer.post(articleUrl + path, dataToJson(payload), false);
    if (authUser != null) {
      if (StringUtils.isNotBlank(authUser.getAccessToken())) {
        resp.addHeader(ACCESS_TOKEN_HEADER, authUser.getAccessToken());
      }
      if (StringUtils.isNotBlank(authUser.getUserSession())) {
        resp.addHeader(USER_SESSION_HEADER, authUser.getUserSession());
      }
      resp.addHeader(FILTERS_HEADER, dataToJson(authUser.getFilters()));
    }
    HttpResponse execute = testServer.execute(resp);
    assertEquals(expectCode, execute.code());
    Answer answer = jsonToData(new String(execute.body()), Answer.class);
    assertEquals(expectCode, answer.getStatusCode());
    Arrays.sort(errors);
    String[] actualErrors = answer.getMessage().getMessages();
    Arrays.sort(actualErrors);
    assertArrayEquals(errors, actualErrors);
    return answer;
  }

  private Answer get(String path, @Nullable AuthUser authUser, int expectCode) throws HttpClientException {
    var resp = testServer.get(articleUrl + path, false);
    if (authUser != null) {
      if (StringUtils.isNotBlank(authUser.getAccessToken())) {
        resp.addHeader(ACCESS_TOKEN_HEADER, authUser.getAccessToken());
      }
      if (StringUtils.isNotBlank(authUser.getUserSession())) {
        resp.addHeader(USER_SESSION_HEADER, authUser.getUserSession());
      }
      resp.addHeader(FILTERS_HEADER, dataToJson(authUser.getFilters()));
    }
    HttpResponse execute = testServer.execute(resp);
    assertEquals(expectCode, execute.code());
    Answer answer = jsonToData(new String(execute.body()), Answer.class);
    assertEquals(expectCode, answer.getStatusCode());
    return answer;
  }
}