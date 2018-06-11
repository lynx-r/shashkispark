package com.workingbit.board.controller.util;

import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.MovesList;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.workingbit.board.controller.util.BoardUtils.*;
import static com.workingbit.share.common.ErrorMessages.UNABLE_TO_MOVE;
import static junit.framework.TestCase.*;

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

    board = move(board, "c3", "d4", true, boardBox.getNotation().getNotationHistory());
    assertTrue(findSquareByNotation("d4", board).isOccupied());
    assertFalse(findSquareByNotation("c3", board).isOccupied());
  }

  @Test
  public void should_move_2() {
    BoardBox boardBox = getBoardBox(true);
    Board board = boardBox.getBoard();
    board = move(board, "c3", "d4", false, boardBox.getNotation().getNotationHistory());
    board = move(board, "d6", "e5", true, boardBox.getNotation().getNotationHistory());
    System.out.println(printBoardNotation(boardBox.getNotation().getNotationHistory()));
  }

  @Test
  public void should_move_4() {
    BoardBox boardBox = getBoardBox(true);
    Board board = boardBox.getBoard();
    board = move(board, "c3", "d4", false, boardBox.getNotation().getNotationHistory());
    board = move(board, "h6", "g5", true, boardBox.getNotation().getNotationHistory());
    board = move(board, "a3", "b4", false, boardBox.getNotation().getNotationHistory());
    board = move(board, "b6", "a5", true, boardBox.getNotation().getNotationHistory());

    System.out.println(printBoardNotation(boardBox.getNotation().getNotationHistory()));
//    assertEquals("1. c3-d4 h6-g5 2. a3-b4 b6-a5", board.getNotation());
  }

  @Test
  public void should_capture() {
    BoardBox boardBox = getBoardBox(true);
    Board board = boardBox.getBoard();
    board = move(board, "c3", "d4", false, boardBox.getNotation().getNotationHistory());
    board = move(board, "f6", "e5", true, boardBox.getNotation().getNotationHistory());
    board = move(board, "d4", "f6", false, boardBox.getNotation().getNotationHistory());
    board = move(board, "g7", "e5", true, boardBox.getNotation().getNotationHistory());
    System.out.println(printBoardNotation(boardBox.getNotation().getNotationHistory()));
//    assertEquals("1. c3-d4 f6-e5 2. d4:f6 g7:e5", board.getNotation());
  }

  @Test
  public void should_not_capture_not_allowed() {
    BoardBox boardBox = getBoardBox(true);
    Board board = boardBox.getBoard();
    board = move(board, "c3", "d4", false, boardBox.getNotation().getNotationHistory());
    board = move(board, "f6", "e5", true, boardBox.getNotation().getNotationHistory());
    Square d4 = findSquareByNotation("d4", board);
    board.setSelectedSquare(d4);
    MovesList movesList = getHighlightedBoard(board.isBlackTurn(), board);
    assertTrue(!movesList.getCaptured().isEmpty());
    Square from = BoardUtils.findSquareByNotation("a1", board);
    Square to = BoardUtils.findSquareByNotation("f6", board);
    to.setHighlight(true);
    board.setSelectedSquare(from);
    board.setNextSquare(to);
    board.setBlackTurn(false);

    try {
      board = move(board, "a1", "f6", true, boardBox.getNotation().getNotationHistory());
    } catch (Exception e) {
      assertEquals(UNABLE_TO_MOVE, e.getMessage());
    }
  }

  @Test
  public void should_capture_2() {
    BoardBox boardBox = getBoardBox(true);
    Board board = boardBox.getBoard();
    board = move(board, "c3", "d4", false, boardBox.getNotation().getNotationHistory());
    board = move(board, "f6", "e5", true, boardBox.getNotation().getNotationHistory());
    board = move(board, "d4", "f6", false, boardBox.getNotation().getNotationHistory());
    board = move(board, "g7", "e5", true, boardBox.getNotation().getNotationHistory());
    board = move(board, "a3", "b4", false, boardBox.getNotation().getNotationHistory());
    board = move(board, "h6", "g5", true, boardBox.getNotation().getNotationHistory());
    board = move(board, "e3", "d4", false, boardBox.getNotation().getNotationHistory());
    board = move(board, "e5", "c3", true, boardBox.getNotation().getNotationHistory());
    board = move(board, "c3", "a5", true, boardBox.getNotation().getNotationHistory());
    System.out.println(printBoardNotation(boardBox.getNotation().getNotationHistory()));
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
    board = move(board, "b2", "d4", false, boardBox.getNotation().getNotationHistory());
    MovesList highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(board, "d4"));
    testCollection("f6,d8,b6", highlight.getAllowed());
    testCollectionTree("e5,e7,c7,c5", highlight.getCaptured());
    board = move(board, "d4", "f6", false, boardBox.getNotation().getNotationHistory());
    highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(board, "f6"));
    testCollection("b6,d4,d8", highlight.getAllowed());
    testCollectionTree("e7,c7,c5", highlight.getCaptured());
    board = move(board, "f6", "d8", false, boardBox.getNotation().getNotationHistory());
    assertTrue(findSquareByNotation("d8", board).getDraught().isQueen());
    highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(board, "d8"));
    testCollection("b6,d4,e3,f2,g1,a5", highlight.getAllowed());
    testCollectionTree("c5,c7", highlight.getCaptured());
    board = move(board, "d8", "b6", false, boardBox.getNotation().getNotationHistory());
    assertTrue(findSquareByNotation("b6", board).getDraught().isQueen());
    highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(board, "b6"));
    testCollection("d4,e3,f2,g1", highlight.getAllowed());
    testCollectionTree("c5", highlight.getCaptured());
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
    board = move(board, "e1", "a5", false, boardBox.getNotation().getNotationHistory());
    MovesList highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(board, "a5"));
    assertTrue(testSameHighlight(board, highlight));
    board = move(board, "a5", "d8", false, boardBox.getNotation().getNotationHistory());
    highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(board, "d8"));
    assertTrue(testSameHighlight(board, highlight));
//    highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(board, "f6"));
//    testCollection("d8,b6,d4", highlight.getAllowed());
//    testCollection("e7,c7,c5", highlight.getCaptured());
//    board = move(board, "f6", "d8", false);
//    assertTrue(board.getSelectedSquare().getDraught().isQueen());
//    highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(board, "d8"));
//    testCollection("d4,e3,f2,g1,b6", highlight.getAllowed());
//    testCollection("c5,c7", highlight.getCaptured());
    System.out.println(printBoardNotation(boardBox.getNotation().getNotationHistory()));
  }

  @Test
  public void should_capture_turk_stroke2() {
    BoardBox boardBox = getBoardBox(false);
    Board board = boardBox.getBoard();
    board = addWhiteDraught(board, "d4");
    board = addBlackDraught(board, "c5");
    board = addBlackDraught(board, "c7");
    board = addBlackDraught(board, "e7");
    board = addBlackDraught(board, "e3");
    board = addBlackDraught(board, "g5");
    board = addBlackDraught(board, "g3");
    Square d4 = findSquareByNotation("d4", board);
    board.setSelectedSquare(d4);
    board = move(board, "d4", "b6", false, boardBox.getNotation().getNotationHistory());
    MovesList highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(board, "b6"));
    assertTrue(testSameHighlight(board, highlight));

    board = move(board, "b6", "d8", false, boardBox.getNotation().getNotationHistory());
    highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(board, "d8"));
    assertTrue(testSameHighlight(board, highlight));

    board = move(board, "d8", "f6", false, boardBox.getNotation().getNotationHistory());
    highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(board, "f6"));
    assertTrue(testSameHighlight(board, highlight));

    board = move(board, "f6", "h4", false, boardBox.getNotation().getNotationHistory());
    highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(board, "h4"));
    assertTrue(testSameHighlight(board, highlight));

    board = move(board, "h4", "f2", false, boardBox.getNotation().getNotationHistory());
    highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(board, "f2"));
    assertTrue(testSameHighlight(board, highlight));

    board = move(board, "f2", "d4", false, boardBox.getNotation().getNotationHistory());
    assertTrue(testSameHighlight(board, new MovesList()));

    System.out.println(printBoardNotation(boardBox.getNotation().getNotationHistory()));
  }

  @Test
  public void should_capture_turk_stroke2_careful() {
    BoardBox boardBox = getBoardBox(false);
    Board board = boardBox.getBoard();
    board = addWhiteDraught(board, "d4");
    board = addBlackDraught(board, "c5");
    board = addBlackDraught(board, "c7");
    board = addBlackDraught(board, "e7");
    board = addBlackDraught(board, "e3");
    board = addBlackDraught(board, "g5");
    board = addBlackDraught(board, "g3");
    Square d4 = findSquareByNotation("d4", board);
    board.setSelectedSquare(d4);
    board = move(board, "d4", "b6", false, boardBox.getNotation().getNotationHistory());
    Set<String> allowed = Set.of("d8", "f6", "h4", "f2", "d4");
    Set<String> captured = Set.of("c5");
    assertTrue(testSameHighlightCustom(board, allowed, captured));
    board = move(board, "b6", "d8", false, boardBox.getNotation().getNotationHistory());
    allowed = Set.of("f6", "h4", "f2", "d4", "e1");
    captured = Set.of("c5", "c7");
    assertTrue(testSameHighlightCustom(board, allowed, captured));
    board = move(board, "d8", "f6", false, boardBox.getNotation().getNotationHistory());
    allowed = Set.of("h4", "f2", "e1", "d4");
    captured = Set.of("c5", "c7", "e7");
    assertTrue(testSameHighlightCustom(board, allowed, captured));
    board = move(board, "f6", "h4", false, boardBox.getNotation().getNotationHistory());
    allowed = Set.of("f2", "d4", "e1");
    captured = Set.of("c5", "c7", "e7", "g5");
    assertTrue(testSameHighlightCustom(board, allowed, captured));
    board = move(board, "h4", "f2", false, boardBox.getNotation().getNotationHistory());
    allowed = Set.of("d4");
    captured = Set.of("c5", "c7", "e7", "g5", "g3");
    assertTrue(testSameHighlightCustom(board, allowed, captured));
    board = move(board, "f2", "d4", false, boardBox.getNotation().getNotationHistory());
    long occupiedOne = board.getAssignedSquares().stream().filter(Square::isOccupied).count();
    assertEquals(1, occupiedOne);

//    board = move(board, "d8", "f6", false, boardBox.getNotation().getNotationHistory());
//    highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(board, "f6"));
//    assertTrue(testSameHighlight(board, highlight));
//
//    board = move(board, "f6", "h4", false, boardBox.getNotation().getNotationHistory());
//    highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(board, "h4"));
//    assertTrue(testSameHighlight(board, highlight));
//
//    board = move(board, "h4", "f2", false, boardBox.getNotation().getNotationHistory());
//    highlight = HighlightMoveUtil.getHighlightedAssignedMoves(getSquare(board, "f2"));
//    assertTrue(testSameHighlight(board, highlight));
//
//    board = move(board, "f2", "d4", false, boardBox.getNotation().getNotationHistory());
//    assertTrue(testSameHighlight(board, new MovesList()));
//
//    System.out.println(printBoardNotation(boardBox.getNotation().getNotationHistory()));
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

    board = move(board, "c7", "d8", true, boardBox.getNotation().getNotationHistory());
    assertTrue(findSquareByNotation("d8", board).isOccupied());
    assertFalse(findSquareByNotation("c7", board).isOccupied());
  }

  @Test
  public void should_move_black() {
    BoardBox boardBox = getBoardBox(false);
    Board board = boardBox.getBoard();
    board.setBlack(true);
    String c7 = "c7";
    addBlackDraught(board, "c7");
    Square squareC7 = BoardUtils.findSquareByNotation(c7, board);
    String d6 = "d6";
    Square squareD6 = (Square) BoardUtils.findSquareByNotation(d6, board).deepClone();
    squareD6.setHighlight(true);
    board.setSelectedSquare(squareC7);
    board.setNextSquare(squareD6);

    board = move(board, "c7", "d6", true, boardBox.getNotation().getNotationHistory());
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
    Set<Square> allowed = highlight.getAllowed();
    if (allowed.size() != highlighted.size()) {
      return false;
    }

    List<Square> allowedSorted = allowed.stream()
        .sorted()
        .collect(Collectors.toList());
    return highlighted.equals(allowedSorted);
  }

  private boolean testSameHighlightCustom(Board board, Set<String> allowed, @NotNull Set<String> captured) {
    List<Square> highlighted = board.getAssignedSquares()
        .stream()
        .filter(Square::isHighlight)
        .sorted()
        .collect(Collectors.toList());
    if (allowed.size() != highlighted.size()) {
      return false;
    }

    List<Square> capt = board.getAssignedSquares()
        .stream()
        .filter(sq-> sq.isOccupied() && sq.getDraught().isCaptured())
        .sorted()
        .collect(Collectors.toList());
    if (captured.size() != capt.size()) {
      return false;
    }

    List<Square> captSorted = captured.stream()
        .map(n-> findSquareByNotation(n, board))
        .sorted()
        .collect(Collectors.toList());

    boolean capturedOk = captSorted.equals(capt);
    if (!capturedOk) {
      return false;
    }

    List<Square> allowedSorted = allowed.stream()
        .map(n-> findSquareByNotation(n, board))
        .sorted()
        .collect(Collectors.toList());
    return highlighted.equals(allowedSorted);
  }

}