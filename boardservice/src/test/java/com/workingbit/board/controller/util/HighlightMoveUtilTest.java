package com.workingbit.board.controller.util;

import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.share.domain.ICoordinates;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.Draught;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.EnumRules;
import com.workingbit.share.model.MovesList;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Aleksey Popryaduhin on 20:01 10/08/2017.
 */
public class HighlightMoveUtilTest {
  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void draught_simple_moves() throws BoardServiceException, ExecutionException, InterruptedException, TimeoutException {
    Board board = getBoard();
    Board updatedBoard = getSquareByVHWithDraught(board, "c3"); // c3
    MovesList highlight = HighlightMoveUtil.highlightedAssignedMoves(getSquare(updatedBoard, "c3"));
    testCollection("d4,b4", highlight.getAllowed());
  }

  @Test
  public void draught_simple_moves_black() throws BoardServiceException, ExecutionException, InterruptedException, TimeoutException {
    Board board = getBoard();
    Board updatedBoard = getSquareByNotationWithBlackDraught(board, "c3"); // c3
    MovesList highlight = HighlightMoveUtil.highlightedAssignedMoves(getSquare(updatedBoard, "c3"));
    testCollection("b2,d2", highlight.getAllowed());
  }

  @Test
  public void draught_became_queen() throws BoardServiceException, ExecutionException, InterruptedException, TimeoutException {
    Board board = getBoard();
    Board updatedBoard = getSquareByVHWithDraught(board, "c7"); // c3
    MovesList highlight = HighlightMoveUtil.highlightedAssignedMoves(getSquare(updatedBoard, "c7"));
    testCollection("b8,d8", highlight.getAllowed());
  }

  @Test
  public void draught_one_beat() throws BoardServiceException, ExecutionException, InterruptedException, TimeoutException {
    Board board = getBoard();
    Board updatedBoard = getSquareByVHWithDraught(board, "c3"); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "d4"); // c3
    MovesList highlight = HighlightMoveUtil.highlightedAssignedMoves(getSquare(updatedBoard, "c3"));
    testCollection("d4", highlight.getCaptured());
    testCollection("e5", highlight.getAllowed());
  }

  @Test
  public void draught_one_beat_back() throws BoardServiceException, ExecutionException, InterruptedException, TimeoutException {
    Board board = getBoard();
    Board updatedBoard = getSquareByVHWithDraught(board, "c3"); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "d2"); // c3
    MovesList highlight = HighlightMoveUtil.highlightedAssignedMoves(getSquare(updatedBoard, "c3"));
    testCollection("d2", highlight.getCaptured());
    testCollection("e1", highlight.getAllowed());
  }

  @Test
  public void draught_beat_sequence() throws BoardServiceException, ExecutionException, InterruptedException, TimeoutException {
    Board board = getBoard();
    Board updatedBoard = getSquareByVHWithDraught(board, "c3"); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "d4"); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "d6"); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "b6"); // c3
    MovesList highlight = HighlightMoveUtil.highlightedAssignedMoves(getSquare(updatedBoard, "c3"));
    testCollection("d4,d6,b6", highlight.getCaptured());
    testCollection("c7,e5,a5", highlight.getAllowed());
  }

  @Test
  public void queen_turk_stroke() throws BoardServiceException, ExecutionException, InterruptedException, TimeoutException {
    Board board = getBoard();
    Board updatedBoard = getSquareByNotationWithDraughtQueen(board, "e1", false); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "c3"); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "b6"); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "e7"); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "e5"); // c3
    MovesList highlight = HighlightMoveUtil.highlightedAssignedMoves(getSquare(updatedBoard, "e1"));
    testCollection("c3,b6,e7,e5", highlight.getCaptured());
    testCollection("b4,f8,a5,c7,d8,d4,f4,g3,h2,f6", highlight.getAllowed());
  }

  @Test
  public void draught_turk_stroke() throws BoardServiceException, ExecutionException, InterruptedException, TimeoutException {
    Board board = getBoard();
    Board updatedBoard = getSquareByVHWithDraught(board, "c1"); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "b2"); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "b4"); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "d4"); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "d6"); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "f6"); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "f4"); // c3
    MovesList highlight = HighlightMoveUtil.highlightedAssignedMoves(getSquare(updatedBoard, "c1"));
    testCollection("b2,b4,d4,d6,f6,f4", highlight.getCaptured());
    testCollection("a3,c5,e3,g5,e7", highlight.getAllowed());
  }

  @Test
  public void queen_beats_sequence() throws BoardServiceException, ExecutionException, InterruptedException, TimeoutException {
    Board board = getBoard();
    Board updatedBoard = getSquareByNotationWithDraughtQueen(board, "e1", false); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "c3"); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "b6"); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "e5"); // c3
    Square e1 = getSquare(updatedBoard, "e1");
    MovesList highlight = HighlightMoveUtil.highlightedAssignedMoves(e1);
    testCollection("c3,b6,e5", highlight.getCaptured());
    testCollection("h2,g3,c7,f4,a5", highlight.getAllowed());
  }


  @Test
  public void queen_beats_sequence2() throws BoardServiceException, ExecutionException, InterruptedException, TimeoutException {
    Board board = getBoard();
    Board updatedBoard = getSquareByNotationWithDraughtQueen(board, "e1", false); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "d2"); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "b6"); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "e7"); // c3
    MovesList highlight = HighlightMoveUtil.highlightedAssignedMoves(getSquare(updatedBoard, "e1"));
    testCollection("d2,b6,e7", highlight.getCaptured());
    testCollection("b4,a5,d8,f8,f6,g5,h4", highlight.getAllowed());
  }

  @Test
  public void draught_two_captured() throws BoardServiceException, ExecutionException, InterruptedException {
    Board board = getBoard();
    Board updatedBoard = getSquareByVHWithDraught(board, "c3"); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "d4"); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "d6"); // c3
    MovesList highlight = HighlightMoveUtil.highlightedAssignedMoves(getSquare(updatedBoard, "c3"));
    testCollection("d4,d6", highlight.getCaptured());
    testCollection("c7,e5", highlight.getAllowed());
  }

  @Test
  public void draught_captured_over_many_squares() throws BoardServiceException, ExecutionException, InterruptedException {
    Board board = getBoard();
    Board updatedBoard = getSquareByVHWithDraught(board, "h2"); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "g3"); // c3
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "c7"); // c3
    MovesList highlight = HighlightMoveUtil.highlightedAssignedMoves(getSquare(updatedBoard, "h2"));
    testCollection("g3", highlight.getCaptured());
    testCollection("f4", highlight.getAllowed());
  }


  @Test
  public void queen_moves_on_empty_desk() throws BoardServiceException, ExecutionException, InterruptedException {
    Board board = getBoard();
    Board updatedBoard = getSquareByNotationWithDraughtQueen(board, "c3", false);
    MovesList highlight = HighlightMoveUtil.highlightedAssignedMoves(getSquare(updatedBoard, "c3"));
    testCollection("d4,e5,f6,g7,h8,b2,a1,b4,a5,d2,e1", highlight.getAllowed());
  }

  @Test
  public void queen_moves_with_beat() throws BoardServiceException, ExecutionException, InterruptedException {
    Board board = getBoard();
    Board updatedBoard = getSquareByNotationWithDraughtQueen(board, "c3", false);
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "e5");
    MovesList highlight = HighlightMoveUtil.highlightedAssignedMoves(getSquare(updatedBoard, "c3"));
    testCollection("e5", highlight.getCaptured());
    testCollection("f6,g7,h8", highlight.getAllowed());
  }

  @Test
  public void queen_moves_with_beat_and_in_one_square() throws BoardServiceException, ExecutionException, InterruptedException {
    Board board = getBoard();
    Board updatedBoard = getSquareByNotationWithDraughtQueen(board, "e1", false);
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "f2");
    updatedBoard = getSquareByNotationWithBlackDraught(updatedBoard, "h4");
    MovesList highlight = HighlightMoveUtil.highlightedAssignedMoves(getSquare(updatedBoard, "e1"));
    testCollection("f2", highlight.getCaptured());
    testCollection("g3", highlight.getAllowed());
  }

  private Board getSquareByVHWithDraught(Board currentBoard, String notation) throws BoardServiceException {
    BoardUtils.addDraught(currentBoard, notation, new Draught(0, 0, 0, false));
    return currentBoard;
  }

  private Board getSquareByNotationWithBlackDraught(Board currentBoard, String notation) throws BoardServiceException {
    BoardUtils.addDraught(currentBoard, notation, new Draught(0, 0, 0, true));
    return currentBoard;
  }

  private Board getSquareByNotationWithDraughtQueen(Board board, String notation, boolean black) throws BoardServiceException {
    BoardUtils.addDraught(board, notation, new Draught(0, 0, 0, black, true));
    return board;
  }

  private void testCollection(String notations, List<Square> items) {
    List<String> collection = items.stream().map(ICoordinates::getNotation).collect(Collectors.toList());
    String[] notation = notations.split(",");
    Arrays.stream(notation).forEach(n -> {
      assertTrue(collection.toString(), collection.contains(n));
    });
    assertEquals(collection.toString(), notation.length, collection.size());
  }

  private Square getSquare(Board board, String notation) {
    Square square = BoardUtils.findSquareByNotation(notation, board);
    return square;
  }

  Board getBoard() {
    Board board = BoardUtils.initBoard(false, false, EnumRules.RUSSIAN);
//    Board board = new Board(boardBox, false, EnumRules.RUSSIAN, 60);
//    Board currentBoard = board;
//    Optional<Square> squareByVH = BoardUtils.findSquareByNotation(currentBoard, "c3"); // 5,2
//    Square selectedSquare = squareByVH;
//    Draught draught = new Draught(5, 2, getRules().getDimension());
//    selectedSquare.setDraught(draught);
//    currentBoard.setSelectedSquare(selectedSquare);
    return board;
  }
}