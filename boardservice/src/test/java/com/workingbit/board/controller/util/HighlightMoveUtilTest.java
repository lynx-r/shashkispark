package com.workingbit.board.controller.util;

import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.MovesList;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Created by Aleksey Popryaduhin on 20:01 10/08/2017.
 */
public class HighlightMoveUtilTest extends BaseServiceTest {
  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void draught_simple_moves() throws BoardServiceException, ExecutionException, InterruptedException, TimeoutException {
    Board board = getBoard();
    Board updatedBoard = addWhiteDraught(board, "c3"); // c3
    MovesList highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(updatedBoard, "c3"));
    testCollection("d4,b4", highlight.getAllowed());
  }

  @Test
  public void draught_simple_moves_black() throws BoardServiceException, ExecutionException, InterruptedException, TimeoutException {
    Board board = getBoard();
    Board updatedBoard = addBlackDraught(board, "c3"); // c3
    MovesList highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(updatedBoard, "c3"));
    testCollection("b2,d2", highlight.getAllowed());
  }

  @Test
  public void draught_became_queen() throws BoardServiceException, ExecutionException, InterruptedException, TimeoutException {
    Board board = getBoard();
    Board updatedBoard = addWhiteDraught(board, "c7"); // c3
    MovesList highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(updatedBoard, "c7"));
    testCollection("b8,d8", highlight.getAllowed());
  }

  @Test
  public void draught_one_beat() throws BoardServiceException, ExecutionException, InterruptedException, TimeoutException {
    Board board = getBoard();
    Board updatedBoard = addWhiteDraught(board, "c3"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "d4"); // c3
    MovesList highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(updatedBoard, "c3"));
    testCollectionTree("d4", highlight.getCaptured());
    testCollection("e5", highlight.getAllowed());
  }

  @Test
  public void draught_one_beat_back() throws BoardServiceException, ExecutionException, InterruptedException, TimeoutException {
    Board board = getBoard();
    Board updatedBoard = addWhiteDraught(board, "c3"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "d2"); // c3
    MovesList highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(updatedBoard, "c3"));
    testCollectionTree("d2", highlight.getCaptured());
    testCollection("e1", highlight.getAllowed());
  }

  @Test
  public void draught_beat_sequence() throws BoardServiceException, ExecutionException, InterruptedException, TimeoutException {
    Board board = getBoard();
    Board updatedBoard = addWhiteDraught(board, "c3"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "d4"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "d6"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "b6"); // c3
    MovesList highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(updatedBoard, "c3"));
    testCollectionTree("d4,d6,b6", highlight.getCaptured());
    testCollection("c7,e5,a5", highlight.getAllowed());
  }

  @Test
  public void queen_turk_stroke() throws BoardServiceException, ExecutionException, InterruptedException, TimeoutException {
    Board board = getBoard();
    Board updatedBoard = addWhiteQueen(board, "e1"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "c3"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "b6"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "e7"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "e5"); // c3
    MovesList highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(updatedBoard, "e1"));
    testCollectionTree("c3,b6,e7,e5", highlight.getCaptured());
    testCollection("c7,f4,g3,h2,d8,f8,b4,a5", highlight.getAllowed());
//    testCollection("b4,f8,a5,c7,d8,d4,f4,g3,h2,f6", highlight.getAllowed());
  }

  @Test
  public void draught_turk_stroke() throws BoardServiceException, ExecutionException, InterruptedException, TimeoutException {
    Board board = getBoard();
    Board updatedBoard = addWhiteDraught(board, "c1"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "b2"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "b4"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "d4"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "d6"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "f6"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "f4"); // c3
    MovesList highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(updatedBoard, "c1"));
    testCollectionTree("b2,b4,d4,d6,f6,f4", highlight.getCaptured());
    testCollection("a3,c5,e3,g5,e7", highlight.getAllowed());
  }

  @Test
  public void queen_beats_sequence() throws BoardServiceException, ExecutionException, InterruptedException, TimeoutException {
    Board board = getBoard();
    Board updatedBoard = addWhiteQueen(board, "e1"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "c3"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "b6"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "e5"); // c3
    Square e1 = getSquare(updatedBoard, "e1");
    MovesList highlight = HighlightMoveUtil.getHighlightedAssignedMoves(e1);
    testCollectionTree("c3,b6,e5", highlight.getCaptured());
    testCollection("c7,f4,g3,h2,d8,b4,a5", highlight.getAllowed());
  }


  @Test
  public void queen_beats_sequence2() throws BoardServiceException, ExecutionException, InterruptedException, TimeoutException {
    Board board = getBoard();
    Board updatedBoard = addWhiteQueen(board, "e1"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "d2"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "b6"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "e7"); // c3
    MovesList highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(updatedBoard, "e1"));
    testCollectionTree("d2,b6,e7", highlight.getCaptured());
    testCollection("c7,d8,f8,c3,b4,a5", highlight.getAllowed());
  }

  @Test
  public void draught_two_captured() throws BoardServiceException, ExecutionException, InterruptedException {
    Board board = getBoard();
    Board updatedBoard = addWhiteDraught(board, "c3"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "d4"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "d6"); // c3
    MovesList highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(updatedBoard, "c3"));
    testCollectionTree("d4,d6", highlight.getCaptured());
    testCollection("c7,e5", highlight.getAllowed());
  }

//  @Test
//  public void draught_three_captured() throws BoardServiceException, ExecutionException, InterruptedException {
//    Board board = getBoard();
//    Board updatedBoard = addWhiteDraught(board, "c3"); // c3
//    updatedBoard = addBlackDraught(updatedBoard, "d4"); // c3
//    updatedBoard = addBlackDraught(updatedBoard, "d6"); // c3
//    updatedBoard = addBlackDraught(updatedBoard, "f6"); // c3
//    MovesList highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(updatedBoard, "c3"));
//    testCollectionTree("d4,d6,f6", highlight.getCaptured());
//    testCollection("c7,e5,g7", highlight.getAllowed());
//  }

  @Test
  public void draught_captured_over_many_squares() throws BoardServiceException, ExecutionException, InterruptedException {
    Board board = getBoard();
    Board updatedBoard = addWhiteDraught(board, "h2"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "g3"); // c3
    updatedBoard = addBlackDraught(updatedBoard, "c7"); // c3
    MovesList highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(updatedBoard, "h2"));
    testCollectionTree("g3", highlight.getCaptured());
    testCollection("f4", highlight.getAllowed());
  }


  @Test
  public void queen_moves_on_empty_desk() throws BoardServiceException, ExecutionException, InterruptedException {
    Board board = getBoard();
    Board updatedBoard = addWhiteQueen(board, "c3");
    MovesList highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(updatedBoard, "c3"));
    testCollection("d4,e5,f6,g7,h8,b2,a1,b4,a5,d2,e1", highlight.getAllowed());
  }

  @Test
  public void queen_moves_with_beat() throws BoardServiceException, ExecutionException, InterruptedException {
    Board board = getBoard();
    Board updatedBoard = addWhiteQueen(board, "c3");
    updatedBoard = addBlackDraught(updatedBoard, "e5");
    MovesList highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(updatedBoard, "c3"));
    testCollectionTree("e5", highlight.getCaptured());
    testCollection("f6,g7,h8", highlight.getAllowed());
  }

  @Test
  public void queen_moves_with_beat_in_one_square() throws BoardServiceException, ExecutionException, InterruptedException {
    Board board = getBoard();
    Board updatedBoard = addWhiteQueen(board, "e1");
    updatedBoard = addBlackDraught(updatedBoard, "f2");
    updatedBoard = addBlackDraught(updatedBoard, "h4");
    MovesList highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(updatedBoard, "e1"));
    testCollectionTree("f2", highlight.getCaptured());
    testCollection("g3", highlight.getAllowed());
  }

  @Test
  public void beat_over_two_with_angel() {
    Board board = getBoard();
    Board updatedBoard = addWhiteDraught(board, "c7");
    updatedBoard = addBlackDraught(updatedBoard, "b6");
    updatedBoard = addBlackDraught(updatedBoard, "c3");
    updatedBoard = addBlackDraught(updatedBoard, "d2");
    MovesList highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(updatedBoard, "c7"));
    testCollection("a5", highlight.getAllowed());
    testCollectionTree("b6", highlight.getCaptured());
  }

  @Test
  public void beat_over_two_like_queen() {
    Board board = getBoard();
    Board updatedBoard = addWhiteDraught(board, "g7");
    updatedBoard = addBlackDraught(updatedBoard, "f6");
    updatedBoard = addBlackDraught(updatedBoard, "b2");
    MovesList highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(updatedBoard, "g7"));
    testCollection("e5", highlight.getAllowed());
    testCollectionTree("f6", highlight.getCaptured());
  }

  @Test
  public void jump_to_far_for_draught() {
    Board board = getBoard();
    Board updatedBoard = addBlackDraught(board, "d8");
    updatedBoard = addWhiteDraught(updatedBoard, "c7");
    updatedBoard = addWhiteDraught(updatedBoard, "d4");
    MovesList highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(updatedBoard, "d8"));
    testCollection("b6", highlight.getAllowed());
    testCollectionTree("c7", highlight.getCaptured());
  }

}