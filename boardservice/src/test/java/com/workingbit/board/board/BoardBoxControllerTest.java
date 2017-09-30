package com.workingbit.board.board;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.workingbit.board.BoardApplication;
import com.workingbit.share.common.Utils;
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
import org.junit.Before;
import org.junit.Test;
import spark.Spark;

import java.util.Objects;

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

    resp = Unirest.post(boardUrl + "/add-draught").body(boardBox).asObject(Answer.class);
    boardBox = (BoardBox) resp.getBody().getBody();
    board = boardBox.getBoard();
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

}