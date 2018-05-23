package com.workingbit.board.controller;

import com.despegar.http.client.*;
import com.despegar.sparkjava.test.SparkServer;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.workingbit.board.BoardEmbedded;
import com.workingbit.board.config.Authority;
import com.workingbit.share.domain.DeepClone;
import com.workingbit.share.domain.ICoordinates;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Draught;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumEditBoardBoxMode;
import com.workingbit.share.model.enumarable.EnumRules;
import com.workingbit.share.util.UnirestUtil;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import spark.servlet.SparkApplication;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.workingbit.orchestrate.OrchestrateModule.orchestralService;
import static com.workingbit.share.common.RequestConstants.ACCESS_TOKEN_HEADER;
import static com.workingbit.share.common.RequestConstants.USER_SESSION_HEADER;
import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Aleksey Popryaduhin on 17:56 30/09/2017.
 */
public class BoardBoxControllerTest {

  private static String boardUrl = "/api/v1";
  private static Integer randomPort = RandomUtils.nextInt(1000, 65000);

  public static class BoardBoxControllerTestSparkApplication implements SparkApplication {

    @Override
    public void init() {
      BoardEmbedded.start();
    }
  }

  @ClassRule
  public static SparkServer<BoardBoxControllerTestSparkApplication> testServer = new SparkServer<>(BoardBoxControllerTestSparkApplication.class, randomPort);

  private AuthUser register() throws Exception {
    String username = Utils.getRandomString20();
    String password = Utils.getRandomString20();
    UserCredentials userCredentials = new UserCredentials(username, password);
    AuthUser registered = orchestralService.register(userCredentials).get();
    assertNotNull(registered);

    return registered;
  }

  @Test
  public void add_draught() throws Exception {

    AuthUser authUser = register();
    DomainId boardBoxId = DomainId.getRandomID();
    DomainId articleId = DomainId.getRandomID();

    List bb = getBoardBox(boardBoxId, articleId, authUser, false);

    BoardBox boardBox = (BoardBox) bb.get(0);
    authUser = (AuthUser) bb.get(1);

    Answer post = post(Authority.BOARD_ADD_DRAUGHT_PROTECTED.getPath(), boardBox, authUser);
    boardBox = (BoardBox) post.getBody();
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
  public void anonym_find_board() throws Exception {
    AuthUser authUser = register();
    DomainId boardBoxId = DomainId.getRandomID();
    DomainId articleId = DomainId.getRandomID();

    List bb = getBoardBox(boardBoxId, articleId, authUser, false);

    BoardBox boardBox = (BoardBox) bb.get(0);
    authUser = (AuthUser) bb.get(1);

    GetMethod resp = testServer.get(boardUrl + "/board/" + boardBoxId, false);
    HttpResponse execute = testServer.execute(resp);
    List<String> setCookie = execute.headers().get("Set-Cookie");
    String serverSession1 = setCookie.get(0);

    resp = testServer.get(boardUrl + "/board/" + boardBoxId, false);
    resp.addHeader("Set-Cookie", setCookie.toString());
    execute = testServer.execute(resp);
    setCookie = execute.headers().get("Set-Cookie");
    String serverSession2 = setCookie.get(0);

    assertEquals(serverSession1, serverSession2);
  }

  @Test
  public void highlight() throws UnirestException, HttpClientException {
    DomainId boardBoxId = DomainId.getRandomID();
    DomainId articleId = DomainId.getRandomID();

//    List bb = getBoardBox(boardBoxId, articleId, authUser, false);
//
//    BoardBox boardBox = (BoardBox) bb.get(0);
//    authUser = (AuthUser) bb.get(1);

//    boardBox = (BoardBox) post("/highlight", boardBox, null).getBody();
//    Board board = boardBox.getBoard();
//    List<Square> highlighted = board.getSquares()
//        .stream()
//        .filter(Objects::nonNull)
//        .filter(Square::isHighlight)
//        .collect(Collectors.toList());
//    assertEquals(highlighted.size(), 2);
//    highlighted.forEach(square -> {
//      square.setDim(8);
//    });
//    testCollection("b4,d4", highlighted);
  }

  @Test
  public void move() throws Exception {
    DomainId boardBoxId = DomainId.getRandomID();
    DomainId articleId = DomainId.getRandomID();

    AuthUser authUser = register();

    List bb = getBoardBox(boardBoxId, articleId, authUser, false);

    BoardBox boardBox = (BoardBox) bb.get(0);
    authUser = (AuthUser) bb.get(1);

    Square nextSquare = findSquare(boardBox, "b4");
    nextSquare.setHighlight(true);
    boardBox.getBoard().setNextSquare(nextSquare);


    boardBox = (BoardBox) post("/move", boardBox, authUser).getBody();
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

  @Test
  public void switchTo() throws Exception {
    DomainId boardBoxId = DomainId.getRandomID();
    DomainId articleId = DomainId.getRandomID();

    AuthUser authUser = register();
    List bb = getBoardBox(boardBoxId, articleId, authUser, true);
    BoardBox boardBox = (BoardBox) bb.get(0);
    authUser = (AuthUser) bb.get(1);

    List<? extends DeepClone> move = move(boardBox, authUser, "g3", "h4");
    move = move(((BoardBox) move.get(0)), ((AuthUser) move.get(1)), "b6", "a5");
    move = move(((BoardBox) move.get(0)), ((AuthUser) move.get(1)), "e3", "f4");
    move = move(((BoardBox) move.get(0)), ((AuthUser) move.get(1)), "d6", "c5");

    BoardBox bbox = (BoardBox)move.get(0);
    NotationDrive notationDrive = bbox.getNotation().getNotationHistory().get(2);
    bbox.getNotation().getNotationHistory().setCurrentNotationDrive(notationDrive);
    Answer post = post(Authority.BOARD_FORK_PROTECTED.getPath(), move.get(0), (AuthUser) move.get(1));
    BoardBoxes bboxes = (BoardBoxes) post.getBody();
    BoardBox finalBbox = bbox;
    bbox = bboxes.getBoardBoxes().valueList().stream().filter(b->b.getId().equals(finalBbox.getId())).findFirst().get();
    authUser = post.getAuthUser();
    assertEquals(2, bbox.getNotation().getNotationHistory().getLast().getVariants().size());
  }

  private List<? extends DeepClone> move(BoardBox boardBox, AuthUser authUser, String sel, String nex) throws HttpClientException {
    Square selectedSquare = findSquare(boardBox, sel);
    boardBox.getBoard().setSelectedSquare(selectedSquare);

    Square nextSquare = findSquare(boardBox, nex);
    nextSquare.setHighlight(true);
    boardBox.getBoard().setNextSquare(nextSquare);

    Answer post = post(Authority.BOARD_MOVE_PROTECTED.getPath(), boardBox, authUser);
    boardBox = (BoardBox) post.getBody();
    authUser = post.getAuthUser();
    Board board = boardBox.getBoard();
    Square moved = board.getSquares()
        .stream()
        .filter(Objects::nonNull)
        .peek(square -> square.setDim(8))
        .filter(square -> square.getNotation().equals(nex))
        .findFirst()
        .get();
    assertTrue(moved.isOccupied());
    return List.of(boardBox, authUser);
  }

  public Square findSquare(BoardBox boardBox, String notation) {
    return boardBox.getBoard().getSquares().stream()
        .filter(Objects::nonNull)
        .peek(square -> square.setDim(8))
        .filter(square -> square.getNotation().equals(notation))
        .findFirst()
        .get();
  }

  private List<DeepClone> getBoardBox(DomainId boardBoxId, DomainId articleId, AuthUser authUser, boolean fillBoard) throws HttpClientException {
    CreateBoardPayload createBoardPayload = CreateBoardPayload.createBoardPayload();
    createBoardPayload.setArticleId(articleId);
    createBoardPayload.setBoardBoxId(boardBoxId);
    createBoardPayload.setRules(EnumRules.RUSSIAN);
    createBoardPayload.setFillBoard(fillBoard);
    createBoardPayload.setEditMode(EnumEditBoardBoxMode.EDIT);
    createBoardPayload.setBlack(false);
    UnirestUtil.configureSerialization();
    BoardBox body = (BoardBox) post(Authority.BOARD_PROTECTED.getPath(), createBoardPayload, authUser).getBody();
    assertNotNull(body);

//    body.setEditMode(EnumEditBoardBoxMode.PLACE);
//    Answer put = put(Authority.BOARD_PROTECTED.getPath(), body, authUser);
//    body = (BoardBox) put.getBody();
//    authUser = put.getAuthUser();

    Answer post = post(Authority.BOARD_BY_ID.getPath().replace(":id", body.getId()), body.getDomainId(), authUser);
    authUser = post.getAuthUser();
    BoardBox boardBox = (BoardBox) post.getBody();
    return List.of(boardBox, authUser);
  }

  private Answer post(String path, Object payload, AuthUser authUser) throws HttpClientException {
    PostMethod resp = testServer.post(boardUrl + path, dataToJson(payload), false);
    if (authUser != null) {
      resp.addHeader(ACCESS_TOKEN_HEADER, authUser.getAccessToken());
      resp.addHeader(USER_SESSION_HEADER, authUser.getUserSession());
    }
    HttpResponse execute = testServer.execute(resp);
    return jsonToData(new String(execute.body()), Answer.class);
  }

  private Answer put(String path, Object payload, AuthUser authUser) throws HttpClientException {
    PutMethod resp = testServer.put(boardUrl + path, dataToJson(payload), false);
    if (authUser != null) {
      resp.addHeader(ACCESS_TOKEN_HEADER, authUser.getAccessToken());
      resp.addHeader(USER_SESSION_HEADER, authUser.getUserSession());
    }
    HttpResponse execute = testServer.execute(resp);
    return jsonToData(new String(execute.body()), Answer.class);
  }

  private Answer get(String params) throws HttpClientException {
    GetMethod resp = testServer.get(boardUrl + params, false);
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