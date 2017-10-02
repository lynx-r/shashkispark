package com.workingbit.board.service;

import com.workingbit.board.board.util.BoardUtils;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Square;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by Aleksey Popryaduhin on 16:59 15/08/2017.
 */
public class BoardUndoRedoTest extends BaseServiceTest {

  @Test
  public void should_undo() {
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

    board = boardService().move(blackTurn, squareC3, squareD4, board, updatedBox);
    squareC3 = BoardUtils.findSquareByLink(squareC3, board);
    assertFalse(squareC3.isOccupied());
    squareD4 = BoardUtils.findSquareByLink(squareD4, board);
    assertTrue(squareD4.isOccupied());

    board = boardService().undo(board).get();
    squareC3 = BoardUtils.findSquareByLink(squareC3, board);
    assertTrue(squareC3.isOccupied());
    squareD4 = BoardUtils.findSquareByLink(squareD4, board);
    assertFalse(squareD4.isOccupied());
  }

  @Test
  public void should_undo_2() {
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

    board = boardService().move(blackTurn, squareC3, squareD4, board, updatedBox);
    squareC3 = BoardUtils.findSquareByNotation(c3, board);
    assertFalse(squareC3.isOccupied());
    squareD4 = BoardUtils.findSquareByNotation(d4, board);
    assertTrue(squareD4.isOccupied());

    String e5 = "e5";
    Square squareE5 = BoardUtils.findSquareByNotation(e5, board);
    squareE5.setHighlighted(true);
    board.setSelectedSquare(squareD4);
    board.setNextSquare(squareE5);

    board = boardService().move(blackTurn, squareD4, squareE5, board, updatedBox);
    squareE5 = BoardUtils.findSquareByNotation(squareE5.getNotation(), board);
    assertTrue(squareE5.isOccupied());
    squareD4 = BoardUtils.findSquareByNotation(squareD4.getNotation(), board);
    assertFalse(squareD4.isOccupied());

    board = boardService().undo(board).get();
    squareD4 = BoardUtils.findSquareByLink(squareD4, board);
    assertTrue(squareD4.isOccupied());
    squareE5 = BoardUtils.findSquareByLink(squareE5, board);
    assertFalse(squareE5.isOccupied());

    board = boardService().undo(board).get();
    squareC3 = BoardUtils.findSquareByLink(squareC3, board);
    assertTrue(squareC3.isOccupied());
    squareD4 = BoardUtils.findSquareByLink(squareD4, board);
    assertFalse(squareD4.isOccupied());
  }

  @Test
  public void should_undo_on_field_board_2() {
    BoardBox boardBox = getBoard(true);
    Board board = boardBox.getBoard();
    String c3 = "c3";
    Square squareC3 = BoardUtils.findSquareByNotation(c3, board);
    String d4 = "d4";
    Square squareD4 = BoardUtils.findSquareByNotation(d4, board);
    squareD4.setHighlighted(true);
    board.setSelectedSquare(squareC3);
    board.setNextSquare(squareD4);

    board = boardService().move(blackTurn, squareC3, squareD4, board, updatedBox);
    squareC3 = BoardUtils.findSquareByLink(squareC3, board);
    assertFalse(squareC3.isOccupied());
    squareD4 = BoardUtils.findSquareByLink(squareD4, board);
    assertTrue(squareD4.isOccupied());

    String e5 = "e5";
    Square squareE5 = BoardUtils.findSquareByNotation(e5, board);
    squareE5.setHighlighted(true);
    board.setSelectedSquare(squareD4);
    board.setNextSquare(squareE5);

    board = boardService().move(blackTurn, squareD4, squareE5, board, updatedBox);
    squareE5 = BoardUtils.findSquareByLink(squareE5, board);
    assertTrue(squareE5.isOccupied());
    squareD4 = BoardUtils.findSquareByLink(squareD4, board);
    assertFalse(squareD4.isOccupied());

    board = boardService().undo(board).get();
    squareD4 = BoardUtils.findSquareByLink(squareD4, board);
    assertTrue(squareD4.isOccupied());
    squareE5 = BoardUtils.findSquareByLink(squareE5, board);
    assertFalse(squareE5.isOccupied());

    board = boardService().undo(board).get();
    squareC3 = BoardUtils.findSquareByLink(squareC3, board);
    assertTrue(squareC3.isOccupied());
    squareD4 = BoardUtils.findSquareByLink(squareD4, board);
    assertFalse(squareD4.isOccupied());
  }

  @Test
  public void should_redo() {
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

    board = boardService().move(blackTurn, squareC3, squareD4, board, updatedBox);
    squareC3 = BoardUtils.findSquareByLink(squareC3, board);
    assertFalse(squareC3.isOccupied());
    squareD4 = BoardUtils.findSquareByLink(squareD4, board);
    assertTrue(squareD4.isOccupied());

    board = boardService().undo(board).get();
    squareC3 = BoardUtils.findSquareByLink(squareC3, board);
    assertTrue(squareC3.isOccupied());
    squareD4 = BoardUtils.findSquareByLink(squareD4, board);
    assertFalse(squareD4.isOccupied());

    board = boardService().redo(board).get();
    squareC3 = BoardUtils.findSquareByLink(squareC3, board);
    assertFalse(squareC3.isOccupied());
    squareD4 = BoardUtils.findSquareByLink(squareD4, board);
    assertTrue(squareD4.isOccupied());
  }

  @Test
  public void should_redo_on_field_board() {
    BoardBox boardBox = getBoard(true);
    Board board = boardBox.getBoard();
    String c3 = "c3";
    Square squareC3 = BoardUtils.findSquareByNotation(c3, board);
    String d4 = "d4";
    Square squareD4 = BoardUtils.findSquareByNotation(d4, board);
    squareD4.setHighlighted(true);
    board.setSelectedSquare(squareC3);
    board.setNextSquare(squareD4);

    board = boardService().move(blackTurn, squareC3, squareD4, board, updatedBox);
    squareC3 = BoardUtils.findSquareByLink(squareC3, board);
    assertFalse(squareC3.isOccupied());
    squareD4 = BoardUtils.findSquareByLink(squareD4, board);
    assertTrue(squareD4.isOccupied());

    board = boardService().undo(board).get();
    squareC3 = BoardUtils.findSquareByLink(squareC3, board);
    assertTrue(squareC3.isOccupied());
    squareD4 = BoardUtils.findSquareByLink(squareD4, board);
    assertFalse(squareD4.isOccupied());

    board = boardService().redo(board).get();
    squareC3 = BoardUtils.findSquareByLink(squareC3, board);
    assertFalse(squareC3.isOccupied());
    squareD4 = BoardUtils.findSquareByLink(squareD4, board);
    assertTrue(squareD4.isOccupied());
  }

//  @Test
//  public void should_save_history() throws Exception, boardService()Error {
//    BoardBox board = getBoard();
////    boardHistoryService.addBoardAndSave(board);
//    Optional<BoardHistory> boardHistory = boardHistoryService.getHistory(board.getId());
//    assertTrue(boardHistory.isPresent());
//    assertEquals(boardHistory.get().getCurrent().getData(), board);
//  }
//
//  @Test
//  public void should_save_two_history() throws Exception, boardService()Error {
//    BoardBox board = getBoard();
////    boardHistoryService.addBoardAndSave(board);
//    Optional<BoardHistory> boardHistory = boardHistoryService.getHistory(board.getId());
//    assertTrue(boardHistory.isPresent());
//    assertEquals(boardHistory.get().getCurrent().getData(), board);
//
////    boardHistoryService.addBoardAndSave(board);
//    boardHistory = boardHistoryService.getHistory(board.getId());
//    assertTrue(boardHistory.isPresent());
//    assertEquals(boardHistory.get().getCurrent().getData(), board);
//  }

//  @Test
//  public void undo() throws Exception {
//  }

}