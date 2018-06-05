package com.workingbit.board.controller.util;

import com.workingbit.board.config.AppProperties;
import com.workingbit.board.dao.BoardBoxDao;
import com.workingbit.board.dao.BoardDao;
import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.board.service.BoardBoxService;
import com.workingbit.board.service.BoardService;
import com.workingbit.board.service.NotationParserService;
import com.workingbit.share.domain.ICoordinates;
import com.workingbit.share.domain.impl.*;
import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.CreateBoardPayload;
import com.workingbit.share.model.DomainId;
import com.workingbit.share.domain.impl.NotationHistory;
import com.workingbit.share.model.enumarable.EnumEditBoardBoxMode;
import com.workingbit.share.model.enumarable.EnumRules;
import com.workingbit.share.util.Utils;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.workingbit.board.controller.util.BoardUtils.findSquareByNotation;
import static com.workingbit.board.controller.util.BoardUtils.findSquareByVH;
import static com.workingbit.share.common.Config4j.configurationProvider;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by Aleksey Popryaduhin on 21:15 11/08/2017.
 */
public class BaseServiceTest {

  private static AppProperties appProperties = configurationProvider("application.yaml").bind("app", AppProperties.class);

  @NotNull
  protected BoardService boardService = new BoardService();
  @NotNull
  protected BoardBoxService boardBoxService = new BoardBoxService();
  @NotNull
  protected BoardBoxDao boardBoxDao = new BoardBoxDao(appProperties);
  @NotNull
  protected BoardDao boardDao = new BoardDao(appProperties);

  @NotNull
  protected NotationParserService notationParserService = new NotationParserService();
  private AuthUser token;

  @Before
  public void setUp() throws Exception {
    token = new AuthUser(Utils.getRandomString20(), Utils.getRandomString20());
  }

  @NotNull
  protected BoardBox getBoardBox(boolean fillBoard) {
    Board board = BoardUtils.initBoard(fillBoard, false, EnumRules.RUSSIAN);
    Utils.setRandomIdAndCreatedAt(board);
    BoardBox boardBox = new BoardBox(board);
    boardBox.setNotation(new Notation());
    return boardBox;
  }

  @NotNull
  protected BoardBox getSavedBoardBoxEmpty() {
    BoardBox boardBox = new BoardBox();
    boardBox.setId(Utils.getRandomString20());
    boardBox.setCreatedAt(LocalDateTime.now());
    boardBox.setArticleId(DomainId.getRandomID());
    return boardBoxService.save(boardBox, token);
  }

  protected Board getBoard() {
    return BoardUtils.initBoard(false, false, EnumRules.RUSSIAN);
  }

  @NotNull
  protected BoardBoxService boardBoxService() {
    return boardBoxService;
  }

  @NotNull
  protected Draught getDraught(int v, int h) {
    return new Draught(v, h, getRules().getDimensionAbs());
  }

  @NotNull Square getSquare(Draught draught, int v, int h) {
    return new Square(v, h, getRules().getDimensionAbs(), true, draught);
  }

  @NotNull Draught getDraughtBlack(int v, int h) {
    return new Draught(v, h, getRules().getDimensionAbs(), true);
  }

  @NotNull Square getSquareByVH(@NotNull BoardBox board, int v, int h) {
    return findSquareByVH(board.getBoard(), v, h);
  }

  @NotNull
  protected EnumRules getRules() {
    return EnumRules.RUSSIAN;
  }

//  BoardService getBoardServiceMock() {
//    ShareProperties moduleProperties = mock(ShareProperties.class);
//    when(moduleProperties.getRegion()).thenReturn("eu-central-1");
//    BoardDao boardDao = new BoardDao(moduleProperties);
//    return new BoardService(boardDao, objectMapper, boardHistoryService);
//  }

  @NotNull
  protected CreateBoardPayload getCreateBoardRequest(boolean black, boolean fillBoard, EnumRules rules,
                                                     EnumEditBoardBoxMode editMode) {
    DomainId boardBoxId = DomainId.getRandomID();
    DomainId articleId = DomainId.getRandomID();

    CreateBoardPayload createBoardPayload = new CreateBoardPayload();
    createBoardPayload.setBlack(black);
    createBoardPayload.setFillBoard(fillBoard);
    createBoardPayload.setRules(rules);
    createBoardPayload.setArticleId(articleId);
    createBoardPayload.setBoardBoxId(boardBoxId);
    createBoardPayload.setEditMode(editMode);
    return createBoardPayload;
  }

  @NotNull
  protected Board addWhiteDraught(@NotNull Board board, String notation) throws BoardServiceException {
    addSquare(board, notation, false, false);
    return board;
  }

  @NotNull
  protected Board addBlackDraught(@NotNull Board board, String notation) throws BoardServiceException {
    addSquare(board, notation, true, false);
    return board;
  }

  @NotNull
  protected Board addWhiteQueen(@NotNull Board board, String notation) throws BoardServiceException {
    addSquare(board, notation, false, true);
    return board;
  }

  @NotNull
  protected Board addBlackQueen(@NotNull Board board, String notation) throws BoardServiceException {
    addSquare(board, notation, true, true);
    return board;
  }

  private void addSquare(@NotNull Board board, String notation, boolean black, boolean queen) {
    BoardUtils.addDraught(board, notation, new Draught(0, 0, 0, black, queen));
    Square added = getSquare(board, notation);
    board.setSelectedSquare(added);
  }

  protected void testCollection(@NotNull String notations, @NotNull Set<Square> items) {
    List<String> collection = items.stream().map(ICoordinates::getNotation).collect(Collectors.toList());
    String[] notation = notations.split(",");
    Arrays.stream(notation).forEach(n -> {
      assertTrue(collection.toString(), collection.contains(n));
    });
    assertEquals(collection.toString(), notation.length, collection.size());
  }

  protected void testCollectionTree(@NotNull String notations, @NotNull TreeSquare items) {
    List<String> collection = items.flatTree().stream().map(ICoordinates::getNotation).collect(Collectors.toList());
    String[] notation = notations.split(",");
    Arrays.stream(notation).forEach(n -> {
      assertTrue(collection.toString(), collection.contains(n));
    });
    assertEquals(collection.toString(), notation.length, collection.size());
  }

  @NotNull
  protected Square getSquare(@NotNull Board board, String notation) {
    return BoardUtils.findSquareByNotation(notation, board);
  }

//  protected Board move(Board board, Square selectedSquare, NotationHistory notationHistory) {
//    boolean blackTurn = board.isBlackTurn();
//    MovesList capturedSquares = getHighlightedBoard(blackTurn, board);
//    return boardService.move(board, capturedSquares.getCaptured(), board.getDomainId(), notationHistory);
//  }

  @NotNull
  protected Board move(@NotNull Board serverBoard, String fromNotation, String toNotation, boolean blackTurn, @NotNull NotationHistory notationHistory) {
    Square from = BoardUtils.findSquareByNotation(fromNotation, serverBoard);
    Square to = BoardUtils.findSquareByNotation(toNotation, serverBoard);
    to.setHighlight(true);
    serverBoard.setSelectedSquare(from);
    serverBoard.setNextSquare(to);
    serverBoard.setBlackTurn(blackTurn);

    Board clientBoard = serverBoard.deepClone();
    serverBoard = boardService.move(serverBoard, clientBoard, notationHistory);

    from = BoardUtils.findSquareByNotation(fromNotation, serverBoard);
    to = BoardUtils.findSquareByNotation(toNotation, serverBoard);

    assertFalse(from.isOccupied());
    TestCase.assertTrue(to.isOccupied());
    return serverBoard;
  }

  @NotNull
  protected Square getSquareWithWhiteDraught(@NotNull Board board, String notation) {
    return getSquareWithDraught(board, notation, false, false);
  }

  @NotNull
  protected Square getSquareWithBlackDraught(@NotNull Board board, String notation) {
    return getSquareWithDraught(board, notation, true, false);
  }

  @NotNull
  protected Square getSquareWithDraught(@NotNull Board board, String notation, boolean black) {
    return getSquareWithDraught(board, notation, black, false);
  }

  @NotNull
  protected Square getSquareWithQueen(@NotNull Board board, String notation, boolean black) {
    return getSquareWithDraught(board, notation, black, true);
  }

  @NotNull
  private Square getSquareWithDraught(@NotNull Board board, String notation, boolean black, boolean queen) {
    Square square = findSquareByNotation(notation, board);
    Draught draught = new Draught(square.getV(), square.getH(), square.getDim(), black, queen);
    square.setDraught(draught);
    return square;
  }

}
