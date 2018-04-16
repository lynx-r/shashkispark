package com.workingbit.security.controller;

import com.despegar.http.client.*;
import com.despegar.sparkjava.test.SparkServer;
import com.workingbit.security.SecurityApplication;
import com.workingbit.security.config.Path;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.*;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.ClassRule;
import org.junit.Test;
import spark.servlet.SparkApplication;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.workingbit.share.common.RequestConstants.ACCESS_TOKEN;
import static com.workingbit.share.common.RequestConstants.JSESSIONID;
import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;
import static com.workingbit.share.util.Utils.getRandomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
public class SecurityControllerTest {

  private static String secureUrl = "/api/secure";
  private static String articleUrl = "/api/v1/article";
  private static Integer randomPort = RandomUtils.nextInt(1000, 65000);

  public static class BoardBoxControllerTestSparkApplication implements SparkApplication {

    @Override
    public void init() {
      SecurityApplication.start();
    }
  }

  @ClassRule
  public static SparkServer<BoardBoxControllerTestSparkApplication> testServer = new SparkServer<>(BoardBoxControllerTestSparkApplication.class, randomPort);

  @Test
  public void create_article() throws HttpClientException {
    String username = Utils.getRandomString();
    String password = Utils.getRandomString();
    RegisterUser registerUser = new RegisterUser(username, password);
    AuthUser registered = (AuthUser) post(secureUrl + Path.REGISTER, registerUser).getBody();
    assertNotNull(registered);

    Map<String, String> headers = new HashMap<String, String>() {{
      put(ACCESS_TOKEN, registered.getAccessToken());
      put(JSESSIONID, registered.getSession());
    }};

    CreateArticlePayload createArticlePayload = getCreateArticlePayload();
    CreateArticleResponse articleResponse = (CreateArticleResponse) post(articleUrl, createArticlePayload, headers).getBody();

    Article article = articleResponse.getArticle();
    BoardBox board = articleResponse.getBoard();
    assertNotNull(article.getId());
    assertNotNull(article.getBoardBoxId());
    assertNotNull(board.getId());
  }

  @Test
  public void save_article() throws HttpClientException {
    CreateArticlePayload createArticlePayload = getCreateArticlePayload();
    CreateArticleResponse articleResponse = (CreateArticleResponse) post("", createArticlePayload).getBody();

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
  public void find_article_by_id() throws HttpClientException {
    CreateArticlePayload createArticlePayload = getCreateArticlePayload();

    CreateArticleResponse articleResponse = (CreateArticleResponse) post("", createArticlePayload).getBody();
    Article article = articleResponse.getArticle();
    BoardBox board = articleResponse.getBoard();
    assertNotNull(article.getId());
    assertNotNull(article.getBoardBoxId());
    assertNotNull(board.getId());

    article = (Article) get("/" + article.getId()).getBody();
    assertNotNull(article);
  }

  @Test
  public void find_all() throws HttpClientException {
    CreateArticlePayload createArticlePayload = getCreateArticlePayload();

    CreateArticleResponse articleResponse = (CreateArticleResponse) post("", createArticlePayload).getBody();
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

  private Answer post(String path, Object payloed) throws HttpClientException {
    return post(path, payloed, null);
  }

  private Answer post(String path, Object payload, Map<String, String> headers) throws HttpClientException {
    PostMethod resp = testServer.post(path, dataToJson(payload), false);
    if (headers != null) {
      headers.forEach(resp::addHeader);
    }
    HttpResponse execute = testServer.execute(resp);
    return jsonToData(new String(execute.body()), Answer.class);
  }

  private Answer put(String path, Object payload) throws HttpClientException {
    PutMethod resp = testServer.put(path, dataToJson(payload), false);
    HttpResponse execute = testServer.execute(resp);
    return jsonToData(new String(execute.body()), Answer.class);
  }

  private Answer get(String params) throws HttpClientException {
    GetMethod resp = testServer.get(params, false);
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