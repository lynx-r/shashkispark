package com.workingbit.board.board;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.workingbit.board.BoardApplication;
import com.workingbit.share.common.Utils;
import com.workingbit.share.domain.ICoordinates;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Draught;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.Answer;
import com.workingbit.share.model.CreateBoardRequest;
import com.workingbit.share.model.EnumRules;
import com.workingbit.share.util.UnirestUtil;
import org.apache.commons.lang3.RandomUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import spark.Spark;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by Aleksey Popryaduhin on 17:56 30/09/2017.
 */
public class BoardBoxControllerTest {

  private String boardUrl;

  @Before
  public void setUp() throws Exception {
    String randomPort = String.valueOf(RandomUtils.nextInt(1000, 65000));
    BoardApplication.main(new String[]{randomPort});

    boardUrl = "http://localhost:" + randomPort + "/api/v1/board";
  }

  @After
  public void tearDown() throws Exception {
    Spark.stop();
  }

  @Test
  public void add_draught() throws UnirestException {
    String boardBoxId = Utils.getRandomUUID();
    String articleId = Utils.getRandomUUID();

    BoardBox boardBox = getBoardBox(boardBoxId, articleId);

    HttpResponse<Answer> resp = Unirest.post(boardUrl + "/add-draught").body(boardBox).asObject(Answer.class);
    boardBox = (BoardBox) resp.getBody().getBody();
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
  public void highlight() throws UnirestException {
    String boardBoxId = Utils.getRandomUUID();
    String articleId = Utils.getRandomUUID();

    BoardBox boardBox = getBoardBox(boardBoxId, articleId);

    HttpResponse<Answer> resp = Unirest.post(boardUrl + "/highlight").body(boardBox).asObject(Answer.class);
    boardBox = (BoardBox) resp.getBody().getBody();
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
  public void move() throws UnirestException {
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

    HttpResponse<Answer> resp = Unirest.post(boardUrl + "/move").body(boardBox).asObject(Answer.class);
    boardBox = (BoardBox) resp.getBody().getBody();
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

  private BoardBox getBoardBox(String boardBoxId, String articleId) throws UnirestException {
    CreateBoardRequest createBoardRequest = new CreateBoardRequest();
    createBoardRequest.setArticleId(articleId);
    createBoardRequest.setBoardBoxId(boardBoxId);
    createBoardRequest.setRules(EnumRules.RUSSIAN);
    createBoardRequest.setFillBoard(false);
    createBoardRequest.setBlack(false);
    UnirestUtil.configureSerialization();
    HttpResponse<Answer> resp = Unirest.post(boardUrl).body(createBoardRequest).asObject(Answer.class);
    BoardBox body = (BoardBox) resp.getBody().getBody();
    assertNotNull(body);

    resp = Unirest.get(boardUrl + "/" + body.getId()).asObject(Answer.class);
    BoardBox boardBox = (BoardBox) resp.getBody().getBody();
    Board board = boardBox.getBoard();
    Square square = new Square(5, 2, 8, true, new Draught(5, 2, 8));
    board.setSelectedSquare(square);
    return boardBox;
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