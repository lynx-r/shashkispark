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
import com.workingbit.share.model.NotationHistory;
import com.workingbit.share.model.enumarable.EnumEditBoardBoxMode;
import com.workingbit.share.model.enumarable.EnumRules;
import com.workingbit.share.util.Utils;
import junit.framework.TestCase;
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

  protected BoardService boardService = new BoardService();
  protected BoardBoxService boardBoxService = new BoardBoxService();
  protected BoardBoxDao boardBoxDao = new BoardBoxDao(appProperties);
  protected BoardDao boardDao = new BoardDao(appProperties);

  protected NotationParserService notationParserService = new NotationParserService();
  private AuthUser token;

  @Before
  public void setUp() throws Exception {
    token = new AuthUser(Utils.getRandomString20(), Utils.getRandomString20());
  }

  protected BoardBox getBoardBox(boolean fillBoard) {
    Board board = BoardUtils.initBoard(fillBoard, false, EnumRules.RUSSIAN);
    Utils.setRandomIdAndCreatedAt(board);
    BoardBox boardBox = new BoardBox(board);
    boardBox.setNotation(new Notation());
    return boardBox;
  }

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

  protected BoardBoxService boardBoxService() {
    return boardBoxService;
  }

  protected Draught getDraught(int v, int h) {
    return new Draught(v, h, getRules().getDimension());
  }

  Square getSquare(Draught draught, int v, int h) {
    return new Square(v, h, getRules().getDimension(), true, draught);
  }

  Draught getDraughtBlack(int v, int h) {
    return new Draught(v, h, getRules().getDimension(), true);
  }

  Square getSquareByVH(BoardBox board, int v, int h) {
    return findSquareByVH(board.getBoard(), v, h);
  }

  protected EnumRules getRules() {
    return EnumRules.RUSSIAN;
  }

//  BoardService getBoardServiceMock() {
//    ShareProperties moduleProperties = mock(ShareProperties.class);
//    when(moduleProperties.getRegion()).thenReturn("eu-central-1");
//    BoardDao boardDao = new BoardDao(moduleProperties);
//    return new BoardService(boardDao, objectMapper, boardHistoryService);
//  }

  protected CreateBoardPayload getCreateBoardRequest(boolean black, boolean fillBoard, EnumRules rules,
                                                     EnumEditBoardBoxMode editMode) {
    DomainId boardBoxId = DomainId.getRandomID();
    DomainId articleId = DomainId.getRandomID();

    CreateBoardPayload createBoardPayload = CreateBoardPayload.createBoardPayload();
    createBoardPayload.setBlack(black);
    createBoardPayload.setFillBoard(fillBoard);
    createBoardPayload.setRules(rules);
    createBoardPayload.setArticleId(articleId);
    createBoardPayload.setBoardBoxId(boardBoxId);
    createBoardPayload.setEditMode(editMode);
    return createBoardPayload;
  }

  protected Board addWhiteDraught(Board board, String notation) throws BoardServiceException {
    addSquare(board, notation, false, false);
    return board;
  }

  protected Board addBlackDraught(Board board, String notation) throws BoardServiceException {
    addSquare(board, notation, true, false);
    return board;
  }

  protected Board addWhiteQueen(Board board, String notation) throws BoardServiceException {
    addSquare(board, notation, false, true);
    return board;
  }

  protected Board addBlackQueen(Board board, String notation) throws BoardServiceException {
    addSquare(board, notation, true, true);
    return board;
  }

  private void addSquare(Board board, String notation, boolean black, boolean queen) {
    BoardUtils.addDraught(board, notation, new Draught(0, 0, 0, black, queen));
    Square added = getSquare(board, notation);
    board.setSelectedSquare(added);
  }

  protected void testCollection(String notations, Set<Square> items) {
    List<String> collection = items.stream().map(ICoordinates::getNotation).collect(Collectors.toList());
    String[] notation = notations.split(",");
    Arrays.stream(notation).forEach(n -> {
      assertTrue(collection.toString(), collection.contains(n));
    });
    assertEquals(collection.toString(), notation.length, collection.size());
  }

  protected Square getSquare(Board board, String notation) {
    return BoardUtils.findSquareByNotation(notation, board);
  }

//  protected Board move(Board board, Square selectedSquare, NotationHistory notationHistory) {
//    boolean blackTurn = board.isBlackTurn();
//    MovesList capturedSquares = getHighlightedBoard(blackTurn, board);
//    return boardService.move(board, capturedSquares.getCaptured(), board.getDomainId(), notationHistory);
//  }

  protected Board move(Board serverBoard, String fromNotation, String toNotation, boolean blackTurn, NotationHistory notationHistory) {
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

  protected Square getSquareWithWhiteDraught(Board board, String notation) {
    return getSquareWithDraught(board, notation, false, false);
  }

  protected Square getSquareWithBlackDraught(Board board, String notation) {
    return getSquareWithDraught(board, notation, true, false);
  }

  protected Square getSquareWithDraught(Board board, String notation, boolean black) {
    return getSquareWithDraught(board, notation, black, false);
  }

  protected Square getSquareWithQueen(Board board, String notation, boolean black) {
    return getSquareWithDraught(board, notation, black, true);
  }

  private Square getSquareWithDraught(Board board, String notation, boolean black, boolean queen) {
    Square square = findSquareByNotation(notation, board);
    Draught draught = new Draught(square.getV(), square.getH(), square.getDim(), black, queen);
    square.setDraught(draught);
    return square;
  }

}
