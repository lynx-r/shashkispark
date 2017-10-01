package com.workingbit.article.controller;

import com.despegar.http.client.GetMethod;
import com.despegar.http.client.HttpClientException;
import com.despegar.http.client.HttpResponse;
import com.despegar.http.client.PostMethod;
import com.despegar.sparkjava.test.SparkServer;
import com.workingbit.article.ArticleApplication;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.*;
import org.apache.commons.lang3.RandomUtils;
import org.junit.ClassRule;
import org.junit.Test;
import spark.servlet.SparkApplication;

import static com.workingbit.share.common.Utils.randomString;
import static com.workingbit.share.util.JsonUtil.dataToJson;
import static com.workingbit.share.util.JsonUtil.jsonToData;
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
    CreateArticlePayload createArticlePayload = new CreateArticlePayload();
    Article article = new Article(randomString(), randomString(), randomString());
    createArticlePayload.setArticle(article);
    CreateBoardPayload createBoardPayload = new CreateBoardPayload();
    createBoardPayload.setBlack(false);
    createBoardPayload.setRules(EnumRules.RUSSIAN);
    createBoardPayload.setFillBoard(false);
    createArticlePayload.setBoardRequest(createBoardPayload);

    CreateArticleResponse articleResponse = (CreateArticleResponse) post("", createArticlePayload).getBody();
    article = articleResponse.getArticle();
    BoardBox board = articleResponse.getBoard();
    assertNotNull(article.getId());
    assertNotNull(article.getBoardBoxId());
    assertNotNull(board.getId());
  }

  private Answer post(String path, Object payload) throws HttpClientException {
    PostMethod resp = testServer.post(boardUrl + path, dataToJson(payload), false);
    HttpResponse execute = testServer.execute(resp);
    return jsonToData(new String(execute.body()), Answer.class);
  }

  private Answer get(String params) throws HttpClientException {
    GetMethod resp = testServer.get(boardUrl + "/" + params, false);
    HttpResponse execute = testServer.execute(resp);
    return jsonToData(new String(execute.body()), Answer.class);
  }
}