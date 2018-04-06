package com.workingbit.board.controller.util;

import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.MovesList;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.workingbit.board.controller.util.BoardUtils.*;
import static com.workingbit.share.common.ErrorMessages.UNABLE_TO_MOVE;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by Aleksey Popryaduhin on 21:13 11/08/2017.
 */
public class MoveUtilTest extends BaseServiceTest {

  @Test
  public void should_white_move() {
    BoardBox boardBox = getBoardBox(false);
    Board board = boardBox.getBoard();
    String c3 = "c3";
    BoardUtils.addDraught(board, c3, getDraught(0, 0));
    Square squareC3 = BoardUtils.findSquareByNotation(c3, board);
    String d4 = "d4";
    Square squareD4 = BoardUtils.findSquareByNotation(d4, board);
    squareD4.setHighlight(true);
    board.setSelectedSquare(squareC3);
    board.setNextSquare(squareD4);

    board = move(board, squareC3);
    assertFalse(squareC3.isOccupied());
    assertTrue(squareD4.isOccupied());
  }

  @Test
  public void should_move_2() {
    BoardBox boardBox = getBoardBox(true);
    Board board = boardBox.getBoard();
    board = move(board, "c3", "d4", false);
    board = move(board, "d6", "e5", true);
    System.out.println(printBoardNotation(board.getNotationHistory()));
  }

  @Test
  public void should_move_4() {
    BoardBox boardBox = getBoardBox(true);
    Board board = boardBox.getBoard();
    board = move(board, "c3", "d4", false);
    board = move(board, "h6", "g5", true);
    board = move(board, "a3", "b4", false);
    board = move(board, "b6", "a5", true);

    System.out.println(printBoardNotation(board.getNotationHistory()));
//    assertEquals("1. c3-d4 h6-g5 2. a3-b4 b6-a5", board.getNotation());
  }

  @Test
  public void should_capture() {
    BoardBox boardBox = getBoardBox(true);
    Board board = boardBox.getBoard();
    board = move(board, "c3", "d4", false);
    board = move(board, "f6", "e5", true);
    board = move(board, "d4", "f6", false);
    board = move(board, "g7", "e5", true);
    System.out.println(printBoardNotation(board.getNotationHistory()));
//    assertEquals("1. c3-d4 f6-e5 2. d4:f6 g7:e5", board.getNotation());
  }

  @Test
  public void should_not_capture_not_allowed() {
    BoardBox boardBox = getBoardBox(true);
    Board board = boardBox.getBoard();
    board = move(board, "c3", "d4", false);
    board = move(board, "f6", "e5", true);
    Square d4 = findSquareByNotation("d4", board);
    MovesList movesList = highlightedBoard(board.isBlackTurn(), d4, board);
    assertTrue(!movesList.getCaptured().isEmpty());
    Square from = BoardUtils.findSquareByNotation("a1", board);
    Square to = BoardUtils.findSquareByNotation("f6", board);
    to.setHighlight(true);
    board.setSelectedSquare(from);
    board.setNextSquare(to);
    board.setBlackTurn(false);

    try {
      move(board, from);
    } catch (Exception e) {
      assertEquals(UNABLE_TO_MOVE, e.getMessage());
    }
  }

  @Test
  public void should_capture_2() {
    BoardBox boardBox = getBoardBox(true);
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
    System.out.println(printBoardNotation(board.getNotationHistory()));
//    assertEquals("1. c3-d4 f6-e5 2. d4:f6 g7:e5", board.getNotation());
  }

  @Test
  public void should_capture_on_cross_diagonal() {
    BoardBox boardBox = getBoardBox(false);
    Board board = boardBox.getBoard();
    board = addWhiteDraught(board, "b2");
    board = addBlackDraught(board, "c3");
    board = addBlackDraught(board, "e5");
    board = addBlackDraught(board, "e7");
    board = addBlackDraught(board, "c7");
    board = addBlackDraught(board, "c5");
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
    System.out.println(printBoardNotation(board.getNotationHistory()));
  }

  @Test
  public void should_capture_turk_stroke() {
    BoardBox boardBox = getBoardBox(false);
    Board board = boardBox.getBoard();
    board = addWhiteQueen(board, "e1");
    board = addBlackDraught(board, "c3");
    board = addBlackDraught(board, "b6");
    board = addBlackDraught(board, "e7");
    board = addBlackDraught(board, "e5");
    Square e1 = findSquareByNotation("e1", board);
    board.setSelectedSquare(e1);
    board = move(board, "e1", "a5", false);
    MovesList highlight = HighlightMoveUtil.highlightedAssignedMoves(getSquare(board, "a5"));
    assertTrue(testSameHighlight(board, highlight));
    board = move(board, "a5", "d8", false);
    highlight = HighlightMoveUtil.highlightedAssignedMoves(getSquare(board, "d8"));
    assertTrue(testSameHighlight(board, highlight));
//    highlight = HighlightMoveUtil.highlightedAssignedMoves(getSquare(board, "f6"));
//    testCollection("d8,b6,d4", highlight.getAllowed());
//    testCollection("e7,c7,c5", highlight.getCaptured());
//    board = move(board, "f6", "d8", false);
//    assertTrue(board.getSelectedSquare().getDraught().isQueen());
//    highlight = HighlightMoveUtil.highlightedAssignedMoves(getSquare(board, "d8"));
//    testCollection("d4,e3,f2,g1,b6", highlight.getAllowed());
//    testCollection("c5,c7", highlight.getCaptured());
    System.out.println(printBoardNotation(board.getNotationHistory()));
  }

  @Test
  public void should_move_white_on_edge() {
    BoardBox boardBox = getBoardBox(false);
    Board board = boardBox.getBoard();
    String c7 = "c7";
    BoardUtils.addDraught(board, c7, getDraught(0, 0));
    Square squareC3 = BoardUtils.findSquareByNotation(c7, board);
    String d8 = "d8";
    Square squareD4 = BoardUtils.findSquareByNotation(d8, board);
    squareD4.setHighlight(true);
    board.setSelectedSquare(squareC3);
    board.setNextSquare(squareD4);

    board = move(board, squareC3);
    assertFalse(squareC3.isOccupied());
    assertTrue(squareD4.isOccupied());
  }

  @Test
  public void should_move_black() {
    BoardBox boardBox = getBoardBox(false);
    Board board = boardBox.getBoard();
    String c7 = "c7";
    BoardUtils.addDraught(board, c7, getDraught(0, 0));
    Square squareC7 = BoardUtils.findSquareByNotation(c7, board);
    String d6 = "d6";
    Square squareD6 = (Square) BoardUtils.findSquareByNotation(d6, board).deepClone();
    squareD6.setHighlight(true);
    board.setSelectedSquare(squareC7);
    board.setNextSquare(squareD6);

    board = move(board, squareC7);
    squareC7 = BoardUtils.findSquareByNotation(c7, board);
    assertFalse(squareC7.isOccupied());
    squareD6 = BoardUtils.findSquareByNotation(d6, board);
    assertTrue(squareD6.isOccupied());
  }

  private boolean testSameHighlight(Board board, MovesList highlight) {
    List<Square> highlighted = board.getAssignedSquares()
        .stream()
        .filter(Square::isHighlight)
        .sorted()
        .collect(Collectors.toList());
    List<Square> allowed = highlight.getAllowed();
    if (allowed.size() != highlighted.size()) {
      return false;
    }

    List<Square> allowedSorted = allowed.stream()
        .sorted()
        .collect(Collectors.toList());
    return highlighted.equals(allowedSorted);
  }
}