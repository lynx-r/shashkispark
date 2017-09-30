package com.workingbit.board.service;

import com.workingbit.board.board.util.BoardUtils;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Square;
import org.junit.Test;

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
    BoardUtils.addDraught(board, c3, false);
    Square squareC3 = BoardUtils.findSquareByNotation(c3, board);
    String d4 = "d4";
    Square squareD4 = BoardUtils.findSquareByNotation(d4, board);
    squareD4.setHighlighted(true);
    board.setSelectedSquare(squareC3);
    board.setNextSquare(squareD4);

    BoardUtils.moveDraught(squareC3, board);
    assertFalse(squareC3.isOccupied());
    assertTrue(squareD4.isOccupied());
  }

  @Test
  public void should_move_white_on_edge() {
    BoardBox boardBox = getBoard(false);
    Board board = boardBox.getBoard();
    String c7 = "c7";
    BoardUtils.addDraught(board, c7, false);
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
    BoardUtils.addDraught(board, c7, true);
    Square squareC7 = BoardUtils.findSquareByNotation(c7, board);
    String d6 = "d6";
    Square squareD6 = BoardUtils.findSquareByNotation(d6, board);
    squareD6.setHighlighted(true);
    board.setSelectedSquare(squareC7);
    board.setNextSquare(squareD6);

    BoardUtils.moveDraught(squareC7, board);
    assertFalse(squareC7.isOccupied());
    assertTrue(squareD6.isOccupied());
  }
}