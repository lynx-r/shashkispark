package com.workingbit.article.controller;

import com.despegar.http.client.*;
import com.despegar.sparkjava.test.SparkServer;
import com.workingbit.article.ArticleApplication;
import com.workingbit.share.client.ShareRemoteClient;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.*;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.ClassRule;
import org.junit.Test;
import spark.servlet.SparkApplication;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.workingbit.share.common.RequestConstants.ACCESS_TOKEN;
import static com.workingbit.share.common.RequestConstants.JSESSIONID;
import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;
import static com.workingbit.share.util.Utils.getRandomString;
import static org.junit.Assert.*;

/**
 * Created by Aleksey Popryaduhin on 17:47 01/10/2017.
 */
public class ArticleControllerTest {

  private static String boardUrl = "/api/v1/article";
  private static Integer randomPort = RandomUtils.nextInt(1000, 65000);

  public static class BoardBoxControllerTestSparkApplication implements SparkApplication {

    @Override
    public void init() {
      ArticleApplication.start();
    }
  }

  @ClassRule
  public static SparkServer<BoardBoxControllerTestSparkApplication> testServer = new SparkServer<>(BoardBoxControllerTestSparkApplication.class, randomPort);

  private AuthUser register() throws Exception {
    String username = Utils.getRandomString();
    String password = Utils.getRandomString();
    RegisterUser registerUser = new RegisterUser(username, password);
    AuthUser registered = ShareRemoteClient.getInstance().register(registerUser).get();
    assertNotNull(registered);

    return registered;
  }

  @Test
  public void create_article() throws Exception {
    Stream.iterate(0, i -> i + 1).limit(10).forEach((i) -> {
      try {
        System.out.println("Iteration " + i);
        CreateArticlePayload createArticlePayload = getCreateArticlePayload();
        AuthUser headers = register();
        Optional<CreateArticleResponse> articleResponseOpt = ShareRemoteClient.getInstance().createArticle(createArticlePayload, headers);
//        CreateArticleResponse articleResponse = (CreateArticleResponse) post("", createArticlePayload, headers).getBody();

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
    });
  }

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
    article = (Article) put("", article).getBody();

    article = (Article) get("/" + article.getId()).getBody();
    assertEquals(article.getTitle(), title);
    assertNotNull(article.getContent(), content);
  }

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
  }

  @Test
  public void find_all() throws Exception {
    CreateArticlePayload createArticlePayload = getCreateArticlePayload();
    AuthUser headers = register();

    CreateArticleResponse articleResponse = (CreateArticleResponse) post("", createArticlePayload, headers).getBody();
    Article article = articleResponse.getArticle();
    BoardBox board = articleResponse.getBoard();
    assertNotNull(article.getId());
    assertNotNull(article.getBoardBoxId());
    assertNotNull(board.getId());

    Articles articles = (Articles) get("s").getBody();
    Article finalArticle = article;
    article = articles.getArticles().stream().filter((article1 -> article1.getId().equals(finalArticle.getId()))).findFirst().get();
    assertNotNull(article);
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
      put(ACCESS_TOKEN, authUser.getAccessToken());
      put(JSESSIONID, authUser.getSession());
    }};
    return post(path, payload, headers);
  }

  private Answer post(String path, Object payload, Map<String, String> headers) throws HttpClientException {
    PostMethod resp = testServer.post(boardUrl + path, dataToJson(payload), false);
    headers.forEach(resp::addHeader);
    HttpResponse execute = testServer.execute(resp);
    return jsonToData(new String(execute.body()), Answer.class);
  }

  private Answer post(String path, Object payload) throws HttpClientException {
    return post(path, payload, Collections.emptyMap());
  }

  private Answer put(String path, Object payload) throws HttpClientException {
    PutMethod resp = testServer.put(boardUrl + path, dataToJson(payload), false);
    HttpResponse execute = testServer.execute(resp);
    return jsonToData(new String(execute.body()), Answer.class);
  }

  private Answer get(String params) throws HttpClientException {
    GetMethod resp = testServer.get(boardUrl + params, false);
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