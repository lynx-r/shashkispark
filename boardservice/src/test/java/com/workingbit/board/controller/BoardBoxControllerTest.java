package com.workingbit.board.controller;

import com.despegar.http.client.*;
import com.despegar.sparkjava.test.SparkServer;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.workingbit.board.BoardEmbedded;
import com.workingbit.board.config.Authority;
import com.workingbit.share.domain.DeepClone;
import com.workingbit.share.domain.ICoordinates;
import com.workingbit.share.domain.impl.*;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumEditBoardBoxMode;
import com.workingbit.share.model.enumarable.EnumRules;
import com.workingbit.share.util.UnirestUtil;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

  @NotNull
  private static String boardUrl = "/api/v1";
  private static Integer randomPort = RandomUtils.nextInt(1000, 65000);

  public static class BoardBoxControllerTestSparkApplication implements SparkApplication {

    @Override
    public void init() {
      BoardEmbedded.start();
    }
  }

  @NotNull
  @ClassRule
  public static SparkServer<BoardBoxControllerTestSparkApplication> testServer = new SparkServer<>(BoardBoxControllerTestSparkApplication.class, randomPort);

  @NotNull
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
//    BoardBox boardBox = (BoardBox) bb.getNotation(0);
//    authUser = (AuthUser) bb.getNotation(1);

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

    move = fork(move, 2);

    move = switchToVariant((BoardBox) move.get(0), (AuthUser) move.get(1), 2, 0);

    move = move(((BoardBox) move.get(0)), ((AuthUser) move.get(1)), "c3", "d4");

    move = switchToVariant((BoardBox) move.get(0), (AuthUser) move.get(1), 2, 1);
  }

  private List<? extends DeepClone> switchToVariant(BoardBox boardBox, AuthUser authUser, int numberNot, int numberVar) throws HttpClientException {
    NotationHistory notationHistory = boardBox.getNotation().getNotationHistory();
    NotationDrives history;
    NotationDrive toSwitch;
    NotationDrive toSwVar;
    notationHistory.setCurrentNotationDrive(numberNot);
    notationHistory.setVariantNotationDrive(numberVar);

    Answer post = post(Authority.BOARD_SWITCH.getPath(), boardBox, authUser);
    BoardBoxes body = (BoardBoxes) post.getBody();
    BoardBox finalBoardBox = boardBox;
    boardBox = body.values().stream().filter(b -> b.getId().equals(finalBoardBox.getId())).findFirst().get();
    authUser = post.getAuthUser();

    history = boardBox.getNotation().getNotationHistory().getNotation();
    toSwitch = history.get(numberNot);
    NotationDrive finalToSwitch = toSwitch;
    boolean b = toSwitch.getVariants().stream().filter(n -> n.getIdInVariants() != finalToSwitch.getIdInVariants()).noneMatch(NotationDrive::isCurrent);
    toSwVar = toSwitch.getVariants().get(numberVar);
    assertTrue(toSwVar.isCurrent());
    toSwitch = history.get(numberNot);
    assertTrue(b);
    NotationDrive finalToSwitch1 = toSwitch;

    return List.of(boardBox, authUser);
  }

  private List<? extends DeepClone> fork(List<? extends DeepClone> move, int numberToSwitch) throws HttpClientException {
    BoardBox bbox = (BoardBox) move.get(0);
    NotationDrive notationDrive = bbox.getNotation().getNotationHistory().get(numberToSwitch);
    bbox.getNotation().getNotationHistory().getNotationLine().setCurrentIndex(numberToSwitch);
    Answer post = post(Authority.BOARD_FORK_PROTECTED.getPath(), move.get(0), (AuthUser) move.get(1));
    BoardBoxes bboxes = (BoardBoxes) post.getBody();
    BoardBox finalBbox = bbox;
    bbox = bboxes.values().stream().filter(b -> b.getId().equals(finalBbox.getId())).findFirst().get();
    AuthUser authUser = post.getAuthUser();
    NotationDrives history = bbox.getNotation().getNotationHistory().getNotation();
    assertEquals(2, history.getLast().getVariants().size());
    return List.of(bbox, authUser);
  }

  private List<? extends DeepClone> move(@NotNull BoardBox boardBox, AuthUser authUser, String sel, String nex) throws HttpClientException {
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

  @NotNull
  public Square findSquare(@NotNull BoardBox boardBox, String notation) {
    return boardBox.getBoard().getSquares().stream()
        .filter(Objects::nonNull)
        .peek(square -> square.setDim(8))
        .filter(square -> square.getNotation().equals(notation))
        .findFirst()
        .get();
  }

  private List<DeepClone> getBoardBox(DomainId boardBoxId, DomainId articleId, AuthUser authUser, boolean fillBoard) throws HttpClientException {
    CreateBoardPayload createBoardPayload = new CreateBoardPayload();
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
//    Answer putNotation = putNotation(Authority.BOARD_PROTECTED.getPath(), body, authUser);
//    body = (BoardBox) putNotation.getBody();
//    authUser = putNotation.getAuthUser();

    Answer post = post(Authority.BOARD_BY_ID.getPath().replace(":id", body.getId()), body.getDomainId(), authUser);
    authUser = post.getAuthUser();
    BoardBox boardBox = (BoardBox) post.getBody();
    return List.of(boardBox, authUser);
  }

  private Answer post(String path, Object payload, @Nullable AuthUser authUser) throws HttpClientException {
    PostMethod resp = testServer.post(boardUrl + path, dataToJson(payload), false);
    if (authUser != null) {
      resp.addHeader(ACCESS_TOKEN_HEADER, authUser.getAccessToken());
      resp.addHeader(USER_SESSION_HEADER, authUser.getUserSession());
    }
    HttpResponse execute = testServer.execute(resp);
    return jsonToData(new String(execute.body()), Answer.class);
  }

  private Answer put(String path, Object payload, @Nullable AuthUser authUser) throws HttpClientException {
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