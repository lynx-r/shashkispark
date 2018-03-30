package com.workingbit.board.controller.util;

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
import com.workingbit.share.model.CreateBoardPayload;
import com.workingbit.share.model.EnumRules;
import com.workingbit.share.model.MovesList;
import com.workingbit.share.util.Utils;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.workingbit.board.controller.util.BoardUtils.findSquareByVH;
import static com.workingbit.board.controller.util.BoardUtils.highlightedBoard;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by Aleksey Popryaduhin on 21:15 11/08/2017.
 */
public class BaseServiceTest {

  protected BoardService boardService = new BoardService();
  protected BoardBoxService boardBoxService = new BoardBoxService();
  protected NotationParserService notationParserService = new NotationParserService();

  BoardDao boardDao;

  protected BoardBox getBoardBox(boolean fillBoard) {
    Board board = BoardUtils.initBoard(fillBoard, false, EnumRules.RUSSIAN);
    Utils.setRandomIdAndCreatedAt(board);
    return new BoardBox(board);
  }

  protected Board getBoard() {
    return BoardUtils.initBoard(false, false, EnumRules.RUSSIAN);
  }

  protected BoardService boardService() {
    return boardService;
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
//    AppProperties appProperties = mock(AppProperties.class);
//    when(appProperties.getRegion()).thenReturn("eu-central-1");
//    BoardDao boardDao = new BoardDao(appProperties);
//    return new BoardService(boardDao, objectMapper, boardHistoryService);
//  }

  protected CreateBoardPayload getCreateBoardRequest(boolean black, boolean fillBoard, EnumRules rules) {
    CreateBoardPayload createBoardPayload = CreateBoardPayload.createBoardPayload();
    createBoardPayload.setBlack(black);
    createBoardPayload.setFillBoard(fillBoard);
    createBoardPayload.setRules(rules);
    createBoardPayload.setBoardBoxId(Utils.getRandomUUID());
    return createBoardPayload;
  }

  protected Board getSquareByNotationWithDraught(Board currentBoard, String notation) throws BoardServiceException {
    BoardUtils.addDraught(currentBoard, notation, new Draught(0, 0, 0, false));
    return currentBoard;
  }

  protected Board getSquareByNotationWithBlackDraught(Board currentBoard, String notation) throws BoardServiceException {
    BoardUtils.addDraught(currentBoard, notation, new Draught(0, 0, 0, true));
    return currentBoard;
  }

  protected Board getSquareByNotationWithDraughtQueen(Board board, String notation, boolean black) throws BoardServiceException {
    BoardUtils.addDraught(board, notation, new Draught(0, 0, 0, black, true));
    return board;
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
    Square square = BoardUtils.findSquareByNotation(notation, board);
    return square;
  }

  protected Board move(Board board, Square selectedSquare) {
    boolean blackTurn = board.isBlackTurn();
    MovesList capturedSquares = highlightedBoard(blackTurn, selectedSquare, board);
    return BoardUtils.moveDraught(board, capturedSquares.getCaptured());
  }

  protected Board move(Board board, String fromNotation, String toNotation, boolean blackTurn) {
    Square from = BoardUtils.findSquareByNotation(fromNotation, board);
    Square to = BoardUtils.findSquareByNotation(toNotation, board);
    to.setHighlighted(true);
    board.setSelectedSquare(from);
    board.setNextSquare(to);
    board.setBlackTurn(blackTurn);

    board = move(board, from);

    from = BoardUtils.findSquareByNotation(fromNotation, board);
    to = BoardUtils.findSquareByNotation(toNotation, board);

    assertFalse(from.isOccupied());
    TestCase.assertTrue(to.isOccupied());
    return board;
  }
}
