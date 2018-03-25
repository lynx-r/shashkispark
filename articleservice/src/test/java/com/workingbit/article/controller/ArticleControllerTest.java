package com.workingbit.article.controller;

import com.despegar.http.client.*;
import com.despegar.sparkjava.test.SparkServer;
import com.workingbit.article.ArticleApplication;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.*;
import org.apache.commons.lang3.RandomUtils;
import org.junit.ClassRule;
import org.junit.Test;
import spark.servlet.SparkApplication;

import static com.workingbit.share.util.Utils.getRandomString;
import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

  @Test
  public void create_article() throws HttpClientException {
    CreateArticlePayload createArticlePayload = getCreateArticlePayload();
    CreateArticleResponse articleResponse = (CreateArticleResponse) post("", createArticlePayload).getBody();

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
    article = articles.stream().filter((article1 -> article1.getId().equals(finalArticle.getId()))).findFirst().get();
    assertNotNull(article);
  }

  private CreateArticlePayload getCreateArticlePayload() {
    CreateArticlePayload createArticlePayload = new CreateArticlePayload();
    Article article = new Article(getRandomString(), getRandomString(), getRandomString());
    createArticlePayload.setArticle(article);
    CreateBoardPayload createBoardPayload = new CreateBoardPayload();
    createBoardPayload.setBlack(false);
    createBoardPayload.setRules(EnumRules.RUSSIAN);
    createBoardPayload.setFillBoard(false);
    createArticlePayload.setBoardRequest(createBoardPayload);
    return createArticlePayload;
  }

  private Answer post(String path, Object payload) throws HttpClientException {
    PostMethod resp = testServer.post(boardUrl + path, dataToJson(payload), false);
    HttpResponse execute = testServer.execute(resp);
    return jsonToData(new String(execute.body()), Answer.class);
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
}