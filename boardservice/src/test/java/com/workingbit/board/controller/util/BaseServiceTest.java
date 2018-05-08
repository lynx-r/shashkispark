package com.workingbit.board.controller.util;

import com.workingbit.board.config.AppProperties;
import com.workingbit.board.dao.BoardBoxDao;
import com.workingbit.board.dao.BoardDao;
import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.board.service.BoardBoxService;
import com.workingbit.board.service.BoardService;
import com.workingbit.board.service.NotationParserService;
import com.workingbit.share.domain.ICoordinates;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Draught;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumEditBoardBoxMode;
import com.workingbit.share.model.enumarable.EnumRules;
import com.workingbit.share.util.Utils;
import junit.framework.TestCase;
import org.junit.Before;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.workingbit.board.controller.util.BoardUtils.findSquareByVH;
import static com.workingbit.board.controller.util.BoardUtils.getHighlightedBoard;
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
    return new BoardBox(board);
  }

  protected BoardBox getSavedBoardBoxEmpty() {
    BoardBox boardBox = new BoardBox();
    boardBox.setId(Utils.getRandomString20());
    boardBox.setCreatedAt(LocalDateTime.now());
    boardBox.setArticleId(Utils.getRandomString20());
    return boardBoxService.save(boardBox, token).get();
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
    CreateBoardPayload createBoardPayload = CreateBoardPayload.createBoardPayload();
    createBoardPayload.setBlack(black);
    createBoardPayload.setFillBoard(fillBoard);
    createBoardPayload.setRules(rules);
    createBoardPayload.setArticleId(Utils.getRandomString20());
    createBoardPayload.setBoardBoxId(Utils.getRandomString20());
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

  protected void testCollection(String notations, List<Square> items) {
    List<String> collection = items.stream().map(ICoordinates::getAlphanumericNotation64).collect(Collectors.toList());
    String[] notation = notations.split(",");
    Arrays.stream(notation).forEach(n -> {
      assertTrue(collection.toString(), collection.contains(n));
    });
    assertEquals(collection.toString(), notation.length, collection.size());
  }

  protected Square getSquare(Board board, String notation) {
    return BoardUtils.findSquareByNotation(notation, board);
  }

  protected Board move(Board board, Square selectedSquare, NotationHistory notationHistory) {
    boolean blackTurn = board.isBlackTurn();
    MovesList capturedSquares = getHighlightedBoard(blackTurn, board);
    return BoardUtils.moveDraught(board, capturedSquares.getCaptured(), board.getId(), notationHistory);
  }

  protected Board move(Board board, String fromNotation, String toNotation, boolean blackTurn, NotationHistory notationHistory) {
    Square from = BoardUtils.findSquareByNotation(fromNotation, board);
    Square to = BoardUtils.findSquareByNotation(toNotation, board);
    to.setHighlight(true);
    board.setSelectedSquare(from);
    board.setNextSquare(to);
    board.setBlackTurn(blackTurn);

    board = move(board, from, notationHistory);

    from = BoardUtils.findSquareByNotation(fromNotation, board);
    to = BoardUtils.findSquareByNotation(toNotation, board);

    assertFalse(from.isOccupied());
    TestCase.assertTrue(to.isOccupied());
    return board;
  }
}
