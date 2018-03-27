package com.workingbit.board.controller;

import com.despegar.http.client.GetMethod;
import com.despegar.http.client.HttpClientException;
import com.despegar.http.client.HttpResponse;
import com.despegar.http.client.PostMethod;
import com.despegar.sparkjava.test.SparkServer;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.workingbit.board.BoardApplication;
import com.workingbit.share.util.Utils;
import com.workingbit.share.domain.ICoordinates;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Draught;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.Answer;
import com.workingbit.share.model.CreateBoardPayload;
import com.workingbit.share.model.EnumRules;
import com.workingbit.share.util.UnirestUtil;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import spark.servlet.SparkApplication;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;
import static junit.framework.TestCase.*;

/**
 * Created by Aleksey Popryaduhin on 17:56 30/09/2017.
 */
public class BoardBoxControllerTest {

  private static String boardUrl = "/api/v1/board";
  private static Integer randomPort = RandomUtils.nextInt(1000, 65000);

  public static class BoardBoxControllerTestSparkApplication implements SparkApplication {

    @Override
    public void init() {
      BoardApplication.start();
    }
  }

  @ClassRule
  public static SparkServer<BoardBoxControllerTestSparkApplication> testServer = new SparkServer<>(BoardBoxControllerTestSparkApplication.class, randomPort);

  @Test
  public void add_draught() throws HttpClientException {
    String boardBoxId = Utils.getRandomUUID();
    String articleId = Utils.getRandomUUID();

    BoardBox boardBox = getBoardBox(boardBoxId, articleId);

    boardBox = (BoardBox) post("/add-draught", boardBox).getBody();
    Board board = boardBox.getBoard();
    Draught draught = board.getWhiteDraughts().get("c3");
    assertTrue(draught != null);
    Square square2 = board.getSquares()
        .stream()
        .filter(Objects::nonNull)
        .filter(square1 -> square1.isOccupied() && square1.getDraught().equals(draught))
        .findFirst()
        .orElse(null);
    assertTrue(square2 != null);
  }

  @Test
  public void highlight() throws UnirestException, HttpClientException {
    String boardBoxId = Utils.getRandomUUID();
    String articleId = Utils.getRandomUUID();

    BoardBox boardBox = getBoardBox(boardBoxId, articleId);

    boardBox = (BoardBox) post("/highlight", boardBox).getBody();
    Board board = boardBox.getBoard();
    List<Square> highlighted = board.getSquares()
        .stream()
        .filter(Objects::nonNull)
        .filter(Square::isHighlighted)
        .collect(Collectors.toList());
    assertEquals(highlighted.size(), 2);
    highlighted.forEach(square -> {
      square.setDim(8);
    });
    testCollection("b4,d4", highlighted);
  }

  @Test
  public void move() throws UnirestException, HttpClientException {
    String boardBoxId = Utils.getRandomUUID();
    String articleId = Utils.getRandomUUID();

    BoardBox boardBox = getBoardBox(boardBoxId, articleId);

    Square nextSquare = boardBox.getBoard().getSquares().stream()
        .filter(Objects::nonNull)
        .peek(square -> square.setDim(8))
        .filter(square -> square.getNotation().equals("b4"))
        .findFirst()
        .get();
    nextSquare.setHighlighted(true);
    boardBox.getBoard().setNextSquare(nextSquare);

    boardBox = (BoardBox) post("/move", boardBox).getBody();
    Board board = boardBox.getBoard();
    Square moved = board.getSquares()
        .stream()
        .filter(Objects::nonNull)
        .peek(square -> square.setDim(8))
        .filter(square -> square.getNotation().equals("b4"))
        .findFirst()
        .get();
    assertTrue(moved.isOccupied());
  }

  private BoardBox getBoardBox(String boardBoxId, String articleId) throws HttpClientException {
    CreateBoardPayload createBoardPayload = CreateBoardPayload.createBoardPayload();
    createBoardPayload.setArticleId(articleId);
    createBoardPayload.setBoardBoxId(boardBoxId);
    createBoardPayload.setRules(EnumRules.RUSSIAN);
    createBoardPayload.setFillBoard(false);
    createBoardPayload.setBlack(false);
    UnirestUtil.configureSerialization();
    BoardBox body = (BoardBox) post("", createBoardPayload).getBody();
    assertNotNull(body);

    BoardBox boardBox = (BoardBox) get(body.getId()).getBody();
    Board board = boardBox.getBoard();
    Square square = new Square(5, 2, 8, true, new Draught(5, 2, 8));
    board.setSelectedSquare(square);
    return boardBox;
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

  private void testCollection(String notations, List<Square> items) {
    List<String> collection = items.stream().map(ICoordinates::getNotation).collect(Collectors.toList());
    String[] notation = notations.split(",");
    Arrays.stream(notation).forEach(n -> {
      Assert.assertTrue(collection.toString(), collection.contains(n));
    });
    Assert.assertEquals(collection.toString(), notation.length, collection.size());
  }
}