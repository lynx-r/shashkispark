package com.workingbit.board.service;

import com.workingbit.board.controller.util.BaseServiceTest;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.CreateBoardPayload;
import com.workingbit.share.model.EnumRules;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.workingbit.share.model.EnumRules.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Aleksey Popryaduhin on 10:08 10/08/2017.
 */
@RunWith(Theories.class)
public class BoardBoxServiceTest extends BaseServiceTest {

  public static @DataPoints
  boolean[] blacks = {true, false};

  public static @DataPoints
  boolean[] fillBoards = {true, false};

  public static @DataPoints
  EnumRules[] ruless = {RUSSIAN, RUSSIAN_GIVEAWAY, INTERNATIONAL, INTERNATIONAL_GIVEAWAY};

  public static @DataPoints
  String[] notations = {
      "1. ",
      ""
  };


  @Test
  public void createBoard() throws Exception {
    BoardBox boardBox = boardBoxService().createBoardBox(getCreateBoardRequest(false, false, RUSSIAN)).get();
    toDelete(boardBox);
    assertNotNull(boardBox.getId());
  }

//  @Test
//  public void findAll() throws Exception {
//    BoardBox board = getBoardBox();
//    toDelete(board);
//    assertNotNull(board.getId());
//    List<BoardBox> all = boardBoxService().findAll(null);
//    assertTrue(all.contains(board));
//  }

  @Test
  public void findById() throws Exception {
    BoardBox board = getBoardBoxBlackNotFilledRUSSIAN();
    toDelete(board);
    assertNotNull(board.getId());
    Optional<BoardBox> byId = boardBoxService().findById(board.getId());
    assertNotNull(byId.get());
  }

  @Test
  public void delete() throws Exception {
    BoardBox board = getBoardBoxBlackNotFilledRUSSIAN();
    String boardId = board.getId();
    assertNotNull(boardId);
    boardBoxService().delete(boardId);
    Optional<BoardBox> byId = boardBoxService().findById(boardId);
    assertTrue(!byId.isPresent());
  }

  private BoardBox getBoardBoxBlackNotFilledRUSSIAN() {
    return getBoardBox(false, false, RUSSIAN);
  }

  @Theory
  public void theory_move(String[] notation) {
    EnumRules rules = RUSSIAN;
    BoardBox boardBox = getBoardBox(false, true, rules);
    BoardBoxService boardBoxService = boardBoxService();


    Square nextSquare = boardBox.getBoard().getSquares().stream()
        .filter(Objects::nonNull)
        .peek(square -> square.setDim(rules.getDimension()))
        .filter(square -> square.getPdnNotation().equals("b4"))
        .findFirst()
        .get();
    nextSquare.setHighlighted(true);
    boardBox.getBoard().setNextSquare(nextSquare);

    Optional<BoardBox> optionalBoardBox = boardBoxService.move(boardBox);

    Square moved = optionalBoardBox.get().getBoard().getSquares()
        .stream()
        .filter(Objects::nonNull)
        .peek(square -> square.setDim(rules.getDimension()))
        .filter(square -> square.getPdnNotation().equals("b4"))
        .findFirst()
        .get();
    TestCase.assertTrue(moved.isOccupied());
  }

  @After
  public void tearUp() {
    boards.forEach(board -> boardBoxService().delete(board.getId()));
  }

  private List<BoardBox> boards = new ArrayList<>();

  private void toDelete(BoardBox board) {
    boards.add(board);
  }

  private BoardBox getBoardBox(boolean black, boolean fillBoard, EnumRules rules) {
    CreateBoardPayload createBoardPayload = getCreateBoardRequest(black, fillBoard, rules);
    return boardBoxService().createBoardBox(createBoardPayload).get();
  }
}