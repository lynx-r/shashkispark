package com.workingbit.board.controller.util;

import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.MovesList;
import org.junit.Test;

import static com.workingbit.board.controller.util.BoardUtils.findSquareByNotation;
import static com.workingbit.board.controller.util.BoardUtils.printBoardNotation;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by Aleksey Popryaduhin on 21:13 11/08/2017.
 */
public class MoveUtilTest extends BaseServiceTest {

  @Test
  public void should_white_move() {
    BoardBox boardBox = getBoard(false);
    Board board = boardBox.getBoard();
    String c3 = "c3";
    BoardUtils.addDraught(board, c3, getDraught(0, 0));
    Square squareC3 = BoardUtils.findSquareByNotation(c3, board);
    String d4 = "d4";
    Square squareD4 = BoardUtils.findSquareByNotation(d4, board);
    squareD4.setHighlighted(true);
    board.setSelectedSquare(squareC3);
    board.setNextSquare(squareD4);

    board = BoardUtils.moveDraught(squareC3, board);
    assertFalse(squareC3.isOccupied());
    assertTrue(squareD4.isOccupied());
  }

  @Test
  public void should_move_2() {
    BoardBox boardBox = getBoard(true);
    Board board = boardBox.getBoard();
    board = move(board, "c3", "d4", false);
    board = move(board, "d6", "e5", true);
    System.out.println(printBoardNotation(board.getNotationStrokes()));
  }

  @Test
  public void should_move_4() {
    BoardBox boardBox = getBoard(true);
    Board board = boardBox.getBoard();
    board = move(board, "c3", "d4", false);
    board = move(board, "h6", "g5", true);
    board = move(board, "a3", "b4", false);
    board = move(board, "b6", "a5", true);

    System.out.println(printBoardNotation(board.getNotationStrokes()));
//    assertEquals("1. c3-d4 h6-g5 2. a3-b4 b6-a5", board.getNotation());
  }

  @Test
  public void should_capture() {
    BoardBox boardBox = getBoard(true);
    Board board = boardBox.getBoard();
    board = move(board, "c3", "d4", false);
    board = move(board, "f6", "e5", true);
    board = move(board, "d4", "f6", false);
    board = move(board, "g7", "e5", true);
    System.out.println(printBoardNotation(board.getNotationStrokes()));
//    assertEquals("1. c3-d4 f6-e5 2. d4:f6 g7:e5", board.getNotation());
  }

  @Test
  public void should_capture_2() {
    BoardBox boardBox = getBoard(true);
    Board board = boardBox.getBoard();
    board = move(board, "c3", "d4", false);
    board = move(board, "f6", "e5", true);
    board = move(board, "d4", "f6", false);
    board = move(board, "g7", "e5", true);
    board = move(board, "a3", "b4", false);
    board = move(board, "h6", "g5", true);
    board = move(board, "e3", "d4", false);
    board = move(board, "e5", "c3", true);
    board = move(board, "c3", "a5", true);
    System.out.println(printBoardNotation(board.getNotationStrokes()));
//    assertEquals("1. c3-d4 f6-e5 2. d4:f6 g7:e5", board.getNotation());
  }

  @Test
  public void should_capture_on_cross_diagonal() {
    BoardBox boardBox = getBoard(false);
    Board board = boardBox.getBoard();
    board = getSquareByNotationWithDraught(board, "b2");
    board = getSquareByNotationWithBlackDraught(board, "c3");
    board = getSquareByNotationWithBlackDraught(board, "e5");
    board = getSquareByNotationWithBlackDraught(board, "e7");
    board = getSquareByNotationWithBlackDraught(board, "c7");
    board = getSquareByNotationWithBlackDraught(board, "c5");
    Square b2 = findSquareByNotation("b2", board);
    board.setSelectedSquare(b2);
    board = move(board, "b2", "d4", false);
    MovesList highlight = HighlightMoveUtil.highlightedAssignedMoves(getSquare(board, "d4"));
    testCollection("f6,d8,b6", highlight.getAllowed());
    testCollection("e5,e7,c7,c5", highlight.getCaptured());
    board = move(board, "d4", "f6", false);
    highlight = HighlightMoveUtil.highlightedAssignedMoves(getSquare(board, "f6"));
    testCollection("d8,b6,d4", highlight.getAllowed());
    testCollection("e7,c7,c5", highlight.getCaptured());
    board = move(board, "f6", "d8", false);
    assertTrue(board.getSelectedSquare().getDraught().isQueen());
    highlight = HighlightMoveUtil.highlightedAssignedMoves(getSquare(board, "d8"));
    testCollection("d4,e3,f2,g1,b6", highlight.getAllowed());
    testCollection("c5,c7", highlight.getCaptured());
    System.out.println(printBoardNotation(board.getNotationStrokes()));
  }

  @Test
  public void should_move_white_on_edge() {
    BoardBox boardBox = getBoard(false);
    Board board = boardBox.getBoard();
    String c7 = "c7";
    BoardUtils.addDraught(board, c7, getDraught(0, 0));
    Square squareC3 = BoardUtils.findSquareByNotation(c7, board);
    String d8 = "d8";
    Square squareD4 = BoardUtils.findSquareByNotation(d8, board);
    squareD4.setHighlighted(true);
    board.setSelectedSquare(squareC3);
    board.setNextSquare(squareD4);

    BoardUtils.moveDraught(squareC3, board);
    assertFalse(squareC3.isOccupied());
    assertTrue(squareD4.isOccupied());
  }

  @Test
  public void should_move_black() {
    BoardBox boardBox = getBoard(false);
    Board board = boardBox.getBoard();
    String c7 = "c7";
    BoardUtils.addDraught(board, c7, getDraught(0, 0));
    Square squareC7 = BoardUtils.findSquareByNotation(c7, board);
    String d6 = "d6";
    Square squareD6 = (Square) BoardUtils.findSquareByNotation(d6, board).deepClone();
    squareD6.setHighlighted(true);
    board.setSelectedSquare(squareC7);
    board.setNextSquare(squareD6);

    board = BoardUtils.moveDraught(squareC7, board);
    squareC7 = BoardUtils.findSquareByNotation(c7, board);
    assertFalse(squareC7.isOccupied());
    squareD6 = BoardUtils.findSquareByNotation(d6, board);
    assertTrue(squareD6.isOccupied());
  }

  private Board move(Board board, String fromNotation, String toNotation, boolean blackTurn) {
    Square from = BoardUtils.findSquareByNotation(fromNotation, board);
    Square to = BoardUtils.findSquareByNotation(toNotation, board);
    to.setHighlighted(true);
    board.setSelectedSquare(from);
    board.setNextSquare(to);
    board.setBlackTurn(blackTurn);

    board = BoardUtils.moveDraught(from, board);
    assertFalse(from.isOccupied());
    assertTrue(to.isOccupied());
    return board;
  }
}