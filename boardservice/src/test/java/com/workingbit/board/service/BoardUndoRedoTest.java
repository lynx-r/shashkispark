package com.workingbit.board.service;

import com.workingbit.board.controller.util.BaseServiceTest;
import com.workingbit.board.controller.util.BoardUtils;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Square;
import org.junit.Test;

import static com.workingbit.share.util.Utils.getRandomString;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by Aleksey Popryaduhin on 16:59 15/08/2017.
 */
public class BoardUndoRedoTest extends BaseServiceTest {

  private String articleId = getRandomString();

  @Test
  public void should_undo() {
    BoardBox boardBox = getBoardBox(false);
    Board board = boardBox.getBoard();
    String c3 = "c3";
    BoardUtils.addDraught(board, c3, getDraught(0,0));
    Square squareC3 = BoardUtils.findSquareByNotation(c3, board);
    String d4 = "d4";
    Square squareD4 = BoardUtils.findSquareByNotation(d4, board);
    squareD4.setHighlighted(true);
    board.setSelectedSquare(squareC3);
    board.setNextSquare(squareD4);

    board = boardService().move(squareC3, squareD4, board, articleId, boardBox.getNotation().getNotationStrokes());
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
    BoardBox boardBox = getBoardBox(false);
    Board board = boardBox.getBoard();
    String c3 = "c3";
    BoardUtils.addDraught(board, c3, getDraught(0,0));
    Square squareC3 = BoardUtils.findSquareByNotation(c3, board);
    String d4 = "d4";
    Square squareD4 = BoardUtils.findSquareByNotation(d4, board);
    squareD4.setHighlighted(true);
    board.setSelectedSquare(squareC3);
    board.setNextSquare(squareD4);

    board = boardService().move(squareC3, squareD4, board, articleId, boardBox.getNotation().getNotationStrokes());
    squareC3 = BoardUtils.findSquareByNotation(c3, board);
    assertFalse(squareC3.isOccupied());
    squareD4 = BoardUtils.findSquareByNotation(d4, board);
    assertTrue(squareD4.isOccupied());

    String e5 = "e5";
    Square squareE5 = BoardUtils.findSquareByNotation(e5, board);
    squareE5.setHighlighted(true);
    board.setSelectedSquare(squareD4);
    board.setNextSquare(squareE5);

    board = boardService().move(squareD4, squareE5, board, articleId, boardBox.getNotation().getNotationStrokes());
    squareE5 = BoardUtils.findSquareByNotation(squareE5.getPdnNotationNumeric64(), board);
    assertTrue(squareE5.isOccupied());
    squareD4 = BoardUtils.findSquareByNotation(squareD4.getPdnNotationNumeric64(), board);
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
    BoardBox boardBox = getBoardBox(true);
    Board board = boardBox.getBoard();
    String c3 = "c3";
    Square squareC3 = BoardUtils.findSquareByNotation(c3, board);
    String d4 = "d4";
    Square squareD4 = BoardUtils.findSquareByNotation(d4, board);
    squareD4.setHighlighted(true);
    board.setSelectedSquare(squareC3);
    board.setNextSquare(squareD4);

    board = boardService().move(squareC3, squareD4, board, articleId, boardBox.getNotation().getNotationStrokes());
    squareC3 = BoardUtils.findSquareByLink(squareC3, board);
    assertFalse(squareC3.isOccupied());
    squareD4 = BoardUtils.findSquareByLink(squareD4, board);
    assertTrue(squareD4.isOccupied());

    String e5 = "e5";
    Square squareE5 = BoardUtils.findSquareByNotation(e5, board);
    squareE5.setHighlighted(true);
    board.setSelectedSquare(squareD4);
    board.setNextSquare(squareE5);

    board = boardService().move(squareD4, squareE5, board, articleId, boardBox.getNotation().getNotationStrokes());
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
    BoardBox boardBox = getBoardBox(false);
    Board board = boardBox.getBoard();
    String c3 = "c3";
    BoardUtils.addDraught(board, c3, getDraught(0,0));
    Square squareC3 = BoardUtils.findSquareByNotation(c3, board);
    String d4 = "d4";
    Square squareD4 = BoardUtils.findSquareByNotation(d4, board);
    squareD4.setHighlighted(true);
    board.setSelectedSquare(squareC3);
    board.setNextSquare(squareD4);

    board = boardService().move(squareC3, squareD4, board, articleId, boardBox.getNotation().getNotationStrokes());
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
    BoardBox boardBox = getBoardBox(true);
    Board board = boardBox.getBoard();
    String c3 = "c3";
    Square squareC3 = BoardUtils.findSquareByNotation(c3, board);
    String d4 = "d4";
    Square squareD4 = BoardUtils.findSquareByNotation(d4, board);
    squareD4.setHighlighted(true);
    board.setSelectedSquare(squareC3);
    board.setNextSquare(squareD4);

    board = boardService().move(squareC3, squareD4, board, articleId, boardBox.getNotation().getNotationStrokes());
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
//    BoardBox board = getBoardBox();
////    boardHistoryService.addBoardAndSave(board);
//    Optional<BoardHistory> boardHistory = boardHistoryService.getHistory(board.getId());
//    assertTrue(boardHistory.isPresent());
//    assertEquals(boardHistory.get().getCurrent().getData(), board);
//  }
//
//  @Test
//  public void should_save_two_history() throws Exception, boardService()Error {
//    BoardBox board = getBoardBox();
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