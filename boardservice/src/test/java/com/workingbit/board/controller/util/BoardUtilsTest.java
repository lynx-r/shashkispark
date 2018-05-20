package com.workingbit.board.controller.util;

import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.share.domain.ICoordinates;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.Draught;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.enumarable.EnumRules;
import com.workingbit.share.model.NotationHistory;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.workingbit.board.controller.util.BoardUtils.isSubDiagonal;
import static org.junit.Assert.*;

/**
 * Created by Aleksey Popryaduhin on 13:52 27/08/2017.
 */
public class BoardUtilsTest extends BaseServiceTest {

  @Test
  public void test_main_road() {
    List<Square> squareDouble1Array = BoardUtils.getSquareArray(0, 8, false);
    String notation = squareDouble1Array.stream().map(ICoordinates::getNotation).collect(Collectors.joining(","));
    assertEquals("h8,g7,f6,e5,d4,c3,b2,a1", notation);
  }

  @Test
  public void test_double_diagonals_main() throws Exception {
    List<Square> squareDouble1Array = BoardUtils.getSquareArray(-1, 8, true);
    String notation = squareDouble1Array.stream().map(ICoordinates::getNotation).collect(Collectors.joining(","));
    assertEquals("a7,b6,c5,d4,e3,f2,g1", notation);

    List<Square> squareDouble2Array = BoardUtils.getSquareArray(1, 8, true);
    notation = squareDouble2Array.stream().map(ICoordinates::getNotation).collect(Collectors.joining(","));
    assertEquals("b8,c7,d6,e5,f4,g3,h2", notation);
  }

  @Test
  public void test_triple_diagonals_sub() throws Exception {
    List<Square> squareDouble1Array = BoardUtils.getSquareArray(2, 8, false);
    String notation = squareDouble1Array.stream().map(ICoordinates::getNotation).collect(Collectors.joining(","));
    assertEquals("h6,g5,f4,e3,d2,c1", notation);

    List<Square> squareDouble2Array = BoardUtils.getSquareArray(-2, 8, false);
    notation = squareDouble2Array.stream().map(ICoordinates::getNotation).collect(Collectors.joining(","));
    assertEquals("f8,e7,d6,c5,b4,a3", notation);
  }

  @Test
  public void test_main_diagonals() {
    List<List<Square>> mainDiagonals = BoardUtils.getDiagonals(8, true);
    String stringStream = mainDiagonals.stream().map(squares -> squares.stream().map(Square::getNotation).collect(Collectors.joining(","))).collect(Collectors.joining(";"));
    assertEquals("a3,b2,c1;a5,b4,c3,d2,e1;a7,b6,c5,d4,e3,f2,g1;b8,c7,d6,e5,f4,g3,h2;d8,e7,f6,g5,h4;f8,g7,h6", stringStream);
  }

  @Test
  public void test_sub_diagonals() {
    List<List<Square>> mainDiagonals = BoardUtils.getDiagonals(8, false);
    String stringStream = mainDiagonals.stream().map(squares -> squares.stream().map(Square::getNotation).collect(Collectors.joining(","))).collect(Collectors.joining(";"));
    assertEquals("b8,a7;d8,c7,b6,a5;f8,e7,d6,c5,b4,a3;h8,g7,f6,e5,d4,c3,b2,a1;h6,g5,f4,e3,d2,c1;h4,g3,f2,e1;h2,g1", stringStream);
  }

  @Test
  public void test_all_diagonals() {
//    List<List<Square>> diagonals = BoardUtils.getAssignSquares(8, getSquareSize());
//    String stringStream = diagonals.stream().map(squares -> squares.stream().map(Square::getNotation).collect(Collectors.joining(","))).collect(Collectors.joining(";"));
//    assertEquals("a3,b2,c1;a5,b4,c3,d2,e1;a7,b6,c5,d4,e3,f2,g1;b8,c7,d6,e5,f4,g3,h2;d8,e7,f6,g5,h4;f8,g7,h6;b8,a7;d8,c7,b6,a5;f8,e7,d6,c5,b4,a3;h8,g7,f6,e5,d4,c3,b2,a1;h6,g5,f4,e3,d2,c1;h4,g3,f2,e1;h2,g1", stringStream);
  }

  @Test
  public void test_init_board() {
    Board boardBox = getBoardFilled();
    boardBox.getAssignedSquares().forEach(square -> {
      if (square.getNotation().equals("a1") || square.getNotation().equals("h8")) {
        assertEquals(1, square.getDiagonals().size());
      } else {
        assertEquals(2, square.getDiagonals().size());
      }
    });
//    Map<Integer, List<List<List<String>>>> collect = boardBox.getWhiteDraughts().stream().map(draught -> draught.getDiagonals()).map(lists -> lists.stream().map(squares -> squares.stream().map(square -> square.getNotation()).collect(Collectors.toList())).collect(Collectors.toList())).collect(Collectors.groupingBy(o -> o.size()));
//    assertEquals("{1=[[[h8, g7, f6, e5, d4, c3, b2, a1]]], 2=[[[a3, b2, c1], [f8, e7, d6, c5, b4, a3]], [[a3, b2, c1], [h8, g7, f6, e5, d4, c3, b2, a1]], [[a3, b2, c1], [h6, g5, f4, e3, d2, c1]], [[a5, b4, c3, d2, e1], [h8, g7, f6, e5, d4, c3, b2, a1]], [[a5, b4, c3, d2, e1], [h6, g5, f4, e3, d2, c1]], [[a5, b4, c3, d2, e1], [h4, g3, f2, e1]], [[a7, b6, c5, d4, e3, f2, g1], [h6, g5, f4, e3, d2, c1]], [[a7, b6, c5, d4, e3, f2, g1], [h4, g3, f2, e1]], [[a7, b6, c5, d4, e3, f2, g1], [h2, g1]], [[b8, c7, d6, e5, f4, g3, h2], [h4, g3, f2, e1]], [[b8, c7, d6, e5, f4, g3, h2], [h2, g1]]]}", collect.printVariants());
//    collect = boardBox.getBlackDraughts().stream().map(draught -> draught.getDiagonals()).map(lists -> lists.stream().map(squares -> squares.stream().map(square -> square.getNotation()).collect(Collectors.toList())).collect(Collectors.toList())).collect(Collectors.groupingBy(o -> o.size()));
//    assertEquals("{1=[[[h8, g7, f6, e5, d4, c3, b2, a1]]], 2=[[[a7, b6, c5, d4, e3, f2, g1], [b8, a7]], [[a7, b6, c5, d4, e3, f2, g1], [d8, c7, b6, a5]], [[b8, c7, d6, e5, f4, g3, h2], [b8, a7]], [[b8, c7, d6, e5, f4, g3, h2], [d8, c7, b6, a5]], [[b8, c7, d6, e5, f4, g3, h2], [f8, e7, d6, c5, b4, a3]], [[d8, e7, f6, g5, h4], [d8, c7, b6, a5]], [[d8, e7, f6, g5, h4], [f8, e7, d6, c5, b4, a3]], [[d8, e7, f6, g5, h4], [h8, g7, f6, e5, d4, c3, b2, a1]], [[f8, g7, h6], [f8, e7, d6, c5, b4, a3]], [[f8, g7, h6], [h8, g7, f6, e5, d4, c3, b2, a1]], [[f8, g7, h6], [h6, g5, f4, e3, d2, c1]]]}", collect.printVariants());
  }

  @Test
  public void add_draught_to_board() throws BoardServiceException {
    Board boardBox = getBoard();
    BoardUtils.addDraught(boardBox, "c3", getDraught(false));
    Square c3 = BoardUtils.findSquareByNotation("c3", boardBox);
    assertTrue(c3.isOccupied());
    c3.getDiagonals().forEach(squares -> {
      int index = squares.indexOf(c3);
      Square square1 = squares.get(index);
      assertNotNull(square1.getDraught());
    });
  }

  @Test
  public void add_draught_fails_on_filled() {
    Board boardBox = getBoardFilled();
    BoardUtils.addDraught(boardBox, "c3", getDraught(true));
    Square c3 = BoardUtils.findSquareByNotation("c3", boardBox);
    assertFalse(c3.isOccupied());
    c3.getDiagonals().forEach(squares -> {
      int index = squares.indexOf(c3);
      Square square1 = squares.get(index);
      assertNull(square1.getDraught());
    });
  }

  @Test
  public void test_main_diagonal() {
    Board boardBox = getBoardFilled();
    Square square = boardBox.getAssignedSquares().get(4);
    List<Square> diagonal0 = square.getDiagonals().get(0);
    System.out.println(diagonal0);
    List<Square> diagonal1 = square.getDiagonals().get(1);
    System.out.println(diagonal1);
    assertFalse(isSubDiagonal(diagonal1, diagonal0));
//     TODO more checks
//    assertTrue(isSubDiagonal(diagonal0, Collections.singletonList(diagonal0.get(2))));
  }

  private Board getBoardFilled() {
    return BoardUtils.initBoard(true, false, EnumRules.RUSSIAN);
  }

  private Draught getDraught(boolean black) {
    return new Draught(0, 0, 0, black);
  }

  @Test
  public void move_draught() throws BoardServiceException {
    Board board = getBoardFilled();
    Square c3 = BoardUtils.findSquareByNotation("c3", board);
    board.setSelectedSquare(c3);
    c3 = BoardUtils.findSquareByNotation(c3.getNotation(), board);
    assertTrue(c3.isOccupied());
    Square d4 = BoardUtils.findSquareByNotation("d4", board);
    board.setNextSquare(d4);

    board = move(board, "c3", "d4", false, new NotationHistory());
    c3 = BoardUtils.findSquareByNotation(c3.getNotation(), board);
    assertFalse(c3.isOccupied());
    d4 = BoardUtils.findSquareByNotation(d4.getNotation(), board);
    assertTrue(d4.isOccupied());

    Square e5 = BoardUtils.findSquareByNotation("e5", board);
    board.setNextSquare(e5);
    board = move(board, "d4", "e5", false, new NotationHistory());
    d4 = BoardUtils.findSquareByNotation(d4.getNotation(), board);
    assertFalse(d4.isOccupied());
    e5 = BoardUtils.findSquareByNotation(e5.getNotation(), board);
    assertTrue(e5.isOccupied());

    assertEquals(board.getWhiteDraughts().size(), board.getRules().getDraughtsCount());
    assertEquals(board.getBlackDraughts().size(), board.getRules().getDraughtsCount());
  }
}