package com.workingbit.board.controller;

import com.despegar.http.client.*;
import com.despegar.sparkjava.test.SparkServer;
import com.workingbit.board.BoardEmbedded;
import com.workingbit.board.config.Authority;
import com.workingbit.board.service.BoardService;
import com.workingbit.board.service.NotationParserService;
import com.workingbit.orchestrate.config.ModuleProperties;
import com.workingbit.orchestrate.service.OrchestralService;
import com.workingbit.share.common.Config4j;
import com.workingbit.share.domain.DeepClone;
import com.workingbit.share.domain.ICoordinates;
import com.workingbit.share.domain.impl.*;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumArticleStatus;
import com.workingbit.share.model.enumarable.EnumEditBoardBoxMode;
import com.workingbit.share.model.enumarable.EnumRules;
import com.workingbit.share.util.UnirestUtil;
import com.workingbit.share.util.Utils;
import net.percederberg.grammatica.parser.ParserCreationException;
import net.percederberg.grammatica.parser.ParserLogException;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import spark.servlet.SparkApplication;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.workingbit.board.controller.util.BoardUtils.findSquareByNotation;
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
  private BoardService boardService = new BoardService();
  private ModuleProperties moduleProperties = Config4j.configurationProvider("moduleconfig.yaml").bind("app", ModuleProperties.class);
  private OrchestralService orchestralService = new OrchestralService(moduleProperties);

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
  private AuthUser register() {
    String username = Utils.getRandomString7();
    String password = Utils.getRandomString7();
    RegisteredUser userCredentials = new RegisteredUser(Utils.getRandomString7(), Utils.getRandomString7(),
        Utils.getRandomString7(), EnumRank.MS, username, password);
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
  public void highlight() {
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

  /**
   * actual test
   *
   * @throws Exception
   */
  @Test
  public void fork() throws Exception {
    DomainId boardBoxId = DomainId.getRandomID();
    DomainId articleId = DomainId.getRandomID();

    AuthUser authUser = register();

    Article article = new Article();
    article.setDomainId(articleId);
    article.setTitle(Utils.getRandomString7());
    article.setHumanReadableUrl(article.getTitle());
    article.setIntro(Utils.getRandomString(101));
    article.setContent(Utils.getRandomString(200));

    CreateBoardPayload createBoardPayload = new CreateBoardPayload();
    createBoardPayload.setArticleId(articleId);
    createBoardPayload.setBlack(false);
    createBoardPayload.setFillBoard(true);
    createBoardPayload.setUserId(authUser.getUserId());
    createBoardPayload.setIdInArticle(1);
    createBoardPayload.setBlack(false);
    createBoardPayload.setRules(EnumRules.RUSSIAN);
    CreateArticlePayload createArticlePayload = new CreateArticlePayload(article, createBoardPayload);

    CreateArticleResponse response = orchestralService.createArticle(createArticlePayload, authUser).get();
    assertNotNull(article);

    article = response.getArticle();
    article.setArticleStatus(EnumArticleStatus.PUBLISHED);
    article = orchestralService.saveArticle(article, authUser).get();
    BoardBox boardBox = response.getBoard();

    NotationParserService parserService = new NotationParserService();
    Notation notation = parserService.parseResource("/pdn/fork_1.pdn");
    NotationHistory notationHistory = notation.getNotationHistory();
    NotationDrives drives = notationHistory.getNotation();
    Board board = boardBox.getBoard().deepClone();
    for (NotationDrive drive : drives) {
      for (NotationMove notationMove : drive.getMoves()) {
        List<String> moves = notationMove.getMoveNotations();
        String move = moves.get(0);
        for (int i = 1; i < moves.size(); i++) {
          boardService.updateBoard(board);
          Square selected = findSquareByNotation(move, board);
          board.setSelectedSquare(selected);
          move = moves.get(i);
          Square next = findSquareByNotation(move, board);
          next.setHighlight(true);
          board.setNextSquare(next);
          boardBox.setBoard(board);
          boardBox = (BoardBox) post(Authority.BOARD_MOVE_PROTECTED.getPath(), boardBox, authUser).getBody();
          move = moves.get(i);
          board = boardBox.getBoard();
          Square moved = board.getSquares()
              .stream()
              .filter(Objects::nonNull)
              .peek(square -> square.setDim(8))
              .filter(square -> square.getNotation().equals(next.getNotation()))
              .findFirst()
              .get();
          assertTrue(moved.isOccupied());
        }
      }
    }

    notationHistory = boardBox.getNotation().getNotationHistory();
    notationHistory.setCurrentIndex(1);
    BoardBoxes boardBoxes = (BoardBoxes) post(Authority.BOARD_FORK_PROTECTED.getPath(), boardBox, authUser).getBody();
    BoardBox finalBoardBox = boardBox;
    boardBox = boardBoxes.getBoardBoxes().valueList().stream().filter(cb -> cb.getId().equals(finalBoardBox.getId())).findFirst().get();
    notationHistory = boardBox.getNotation().getNotationHistory();
    NotationLine notationLine = notationHistory.getNotationLine();
    assertEquals(1, notationLine.getCurrentIndex().intValue());
    assertEquals(1, notationLine.getVariantIndex().intValue());

    notationHistory = boardBox.getNotation().getNotationHistory();
    notationHistory.setCurrentIndex(1);
    boardBoxes = (BoardBoxes) post(Authority.BOARD_FORK_PROTECTED.getPath(), boardBox, authUser).getBody();
    boardBox = boardBoxes.getBoardBoxes().valueList().stream().filter(cb -> cb.getId().equals(finalBoardBox.getId())).findFirst().get();
    notationHistory = boardBox.getNotation().getNotationHistory();
    notationLine = notationHistory.getNotationLine();
    assertEquals(1, notationLine.getCurrentIndex().intValue());
    assertEquals(2, notationLine.getVariantIndex().intValue());

    NotationDrive last = notationHistory.getLast();
    NotationDrives variants = last.getVariants();
    assertEquals(3, variants.size());
    assertTrue(variants.getLast().isCurrent());
    assertTrue(variants.get(1).isPrevious());

    parserService = new NotationParserService();
    notation = parserService.parseResource("/pdn/fork_1_3.pdn");
    notationHistory = notation.getNotationHistory();
    drives = notationHistory.getNotation();
    board = boardBox.getBoard().deepClone();
    for (NotationDrive drive : drives) {
      for (NotationMove notationMove : drive.getMoves()) {
        List<String> moves = notationMove.getMoveNotations();
        String move = moves.get(0);
        for (int i = 1; i < moves.size(); i++) {
          boardService.updateBoard(board);
          Square selected = findSquareByNotation(move, board);
          board.setSelectedSquare(selected);
          move = moves.get(i);
          Square next = findSquareByNotation(move, board);
          next.setHighlight(true);
          board.setNextSquare(next);
          boardBox.setBoard(board);
          boardBox = (BoardBox) post(Authority.BOARD_MOVE_PROTECTED.getPath(), boardBox, authUser).getBody();
          move = moves.get(i);
          board = boardBox.getBoard();
          Square moved = board.getSquares()
              .stream()
              .filter(Objects::nonNull)
              .peek(square -> square.setDim(8))
              .filter(square -> square.getNotation().equals(next.getNotation()))
              .findFirst()
              .get();
          assertTrue(moved.isOccupied());
        }
      }
    }

//    post(Authority.BOARD_DELETE_PROTECTED.getPath(), boardBox.getDomainId(), authUser);
  }

  /**
   * actual test
   *
   * @throws Exception
   */
  @Test
  public void fork_international() throws Exception {
    DomainId boardBoxId = DomainId.getRandomID();
    DomainId articleId = DomainId.getRandomID();

    AuthUser authUser = register();

    Article article = new Article();
    article.setDomainId(articleId);
    article.setTitle(Utils.getRandomString7());
    article.setHumanReadableUrl(article.getTitle());
    article.setIntro(Utils.getRandomString(101));
    article.setContent(Utils.getRandomString(200));

    CreateBoardPayload createBoardPayload = new CreateBoardPayload();
    createBoardPayload.setArticleId(articleId);
    createBoardPayload.setBlack(false);
    createBoardPayload.setFillBoard(true);
    createBoardPayload.setUserId(authUser.getUserId());
    createBoardPayload.setIdInArticle(1);
    createBoardPayload.setBlack(false);
    createBoardPayload.setRules(EnumRules.RUSSIAN);
    CreateArticlePayload createArticlePayload = new CreateArticlePayload(article, createBoardPayload);

    CreateArticleResponse response = orchestralService.createArticle(createArticlePayload, authUser).get();
    assertNotNull(article);

    article = response.getArticle();
    article.setArticleStatus(EnumArticleStatus.PUBLISHED);
    article = orchestralService.saveArticle(article, authUser).get();
    BoardBox boardBox = response.getBoard();

    URL uri = getClass().getResource("/pdn/fork_inter_1.pdn");
    Path path = Paths.get(uri.toURI());
    List<String> bufferedReader = Files.readAllLines(path);
    String collect = bufferedReader.stream().collect(Collectors.joining("\n"));
    ImportPdnPayload importPdnPayload = new ImportPdnPayload(articleId, collect, 0, EnumEditBoardBoxMode.EDIT);
    Answer post = post(Authority.PARSE_PDN_PROTECTED.getPath(), importPdnPayload, authUser);
    authUser = post.getAuthUser();
    boardBox = (BoardBox) post.getBody();

    NotationHistory notationHistory = boardBox.getNotation().getNotationHistory();
    notationHistory.setCurrentIndex(5);
    BoardBoxes boardBoxes = (BoardBoxes) post(Authority.BOARD_FORK_PROTECTED.getPath(), boardBox, authUser).getBody();
    BoardBox finalBoardBox = boardBox;
    boardBox = boardBoxes.getBoardBoxes().valueList().stream().filter(cb -> cb.getId().equals(finalBoardBox.getId())).findFirst().get();
    notationHistory = boardBox.getNotation().getNotationHistory();
    NotationLine notationLine = notationHistory.getNotationLine();
    assertEquals(5, notationLine.getCurrentIndex().intValue());
    assertEquals(1, notationLine.getVariantIndex().intValue());

    notationHistory = boardBox.getNotation().getNotationHistory();
    notationHistory.setCurrentIndex(5);
    boardBoxes = (BoardBoxes) post(Authority.BOARD_FORK_PROTECTED.getPath(), boardBox, authUser).getBody();
    boardBox = boardBoxes.getBoardBoxes().valueList().stream().filter(cb -> cb.getId().equals(finalBoardBox.getId())).findFirst().get();
    notationHistory = boardBox.getNotation().getNotationHistory();
    notationLine = notationHistory.getNotationLine();
    assertEquals(5, notationLine.getCurrentIndex().intValue());
    assertEquals(2, notationLine.getVariantIndex().intValue());

    NotationDrive last = notationHistory.getLast();
    NotationDrives variants = last.getVariants();
    assertEquals(3, variants.size());
    assertTrue(variants.getLast().isCurrent());
    assertTrue(variants.get(1).isPrevious());

    boardBox = additionalMoves(boardBox, authUser);

    boardBox.getNotation().getNotationHistory().setCurrentIndex(5);
    boardBox.getNotation().getNotationHistory().setVariantIndex(1);
    boardBox = (BoardBox) post(Authority.BOARD_REMOVE_VARIANT_PROTECTED.getPath(), boardBox, authUser).getBody();

    notationHistory = boardBox.getNotation().getNotationHistory();
    last = notationHistory.getLast();
    variants = last.getVariants();
    assertEquals(3, variants.size());
    assertTrue(variants.getLast().isCurrent());
    assertTrue(variants.get(1).isPrevious());

    post(Authority.BOARD_DELETE_PROTECTED.getPath(), boardBox.getDomainId(), authUser);
  }

  private BoardBox additionalMoves(BoardBox boardBox, AuthUser authUser) throws ParserLogException, ParserCreationException, URISyntaxException, IOException, HttpClientException {
    NotationHistory notationHistory;
    NotationParserService parserService = new NotationParserService();
    Notation notation = parserService.parseResource("/pdn/fork_inter_1_3.pdn");
    notation.syncFormatAndRules();
    notationHistory = notation.getNotationHistory();
    NotationDrives drives = notationHistory.getNotation();
    Board board = boardBox.getBoard().deepClone();
    for (NotationDrive drive : drives) {
      for (NotationMove notationMove : drive.getMoves()) {
        List<String> moves = notationMove.getMoveNotations();
        String move = moves.get(0);
        for (int i = 1; i < moves.size(); i++) {
          boardService.updateBoard(board);
          Square selected = findSquareByNotation(move, board);
          board.setSelectedSquare(selected);
          move = moves.get(i);
          Square next = findSquareByNotation(move, board);
          next.setHighlight(true);
          board.setNextSquare(next);
          boardBox.setBoard(board);
          boardBox = (BoardBox) post(Authority.BOARD_MOVE_PROTECTED.getPath(), boardBox, authUser).getBody();
          move = moves.get(i);
          board = boardBox.getBoard();
          Board finalBoard = board;
          Square moved = board.getSquares()
              .stream()
              .filter(Objects::nonNull)
              .peek(square -> square.setDim(finalBoard.getRules().getDimension()))
              .filter(square -> square.getNotation().equals(next.getNotation()))
              .findFirst()
              .get();
          assertTrue(moved.isOccupied());
        }
      }
    }
    boardBox.setBoard(board);
    return boardBox;
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
    notationHistory.setCurrentIndex(numberNot);
    notationHistory.setVariantIndex(numberVar);

    Answer post = post(Authority.BOARD_SWITCH.getPath(), boardBox, authUser);
    BoardBoxes body = (BoardBoxes) post.getBody();
    BoardBox finalBoardBox = boardBox;
    boardBox = body.valueList().stream().filter(b -> b.getId().equals(finalBoardBox.getId())).findFirst().get();
    authUser = post.getAuthUser();

    history = boardBox.getNotation().getNotationHistory().getNotation();
    toSwitch = history.get(numberNot);
    NotationDrive finalToSwitch = toSwitch;
    boolean b = toSwitch.getVariants().stream().filter(n -> n.getIdInVariants() != finalToSwitch.getIdInVariants()).noneMatch(NotationDrive::isCurrent);
    toSwVar = toSwitch.getVariants().get(numberVar);
    assertTrue(toSwVar.isCurrent());
    assertTrue(b);

    return List.of(boardBox, authUser);
  }

  private List<? extends DeepClone> fork(List<? extends DeepClone> move, int numberToSwitch) throws HttpClientException {
    BoardBox bbox = (BoardBox) move.get(0);
    NotationDrive notationDrive = bbox.getNotation().getNotationHistory().get(numberToSwitch);
    bbox.getNotation().getNotationHistory().getNotationLine().setCurrentIndex(numberToSwitch);
    Answer post = post(Authority.BOARD_FORK_PROTECTED.getPath(), move.get(0), (AuthUser) move.get(1));
    BoardBoxes bboxes = (BoardBoxes) post.getBody();
    BoardBox finalBbox = bbox;
    bbox = bboxes.valueList().stream().filter(b -> b.getId().equals(finalBbox.getId())).findFirst().get();
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