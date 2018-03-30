package com.workingbit.board.service;

import com.workingbit.board.controller.util.BaseServiceTest;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.CreateBoardPayload;
import com.workingbit.share.model.EnumRules;
import com.workingbit.share.model.Notation;
import com.workingbit.share.util.Utils;
import net.percederberg.grammatica.parser.ParserCreationException;
import net.percederberg.grammatica.parser.ParserLogException;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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

  public final String[] PDN_FILE_NAMES = {
      "/pdn/test1.pdn",
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

  @Test
  public void test_pdn_notations() throws URISyntaxException, IOException, ParserLogException, ParserCreationException {
    for (String fileName : PDN_FILE_NAMES) {
      URL uri = getClass().getResource(fileName);
      Path path = Paths.get(uri.toURI());
      BufferedReader bufferedReader = Files.newBufferedReader(path);

      Notation notation = notationParserService.parse(bufferedReader);
      notation.setRules(RUSSIAN);

      String articleId = Utils.getRandomUUID();
      String boardBoxId = Utils.getRandomUUID();
      Board boardFromNotation = boardService.createBoardFromNotation(notation, articleId, boardBoxId);

      assertNotNull(boardFromNotation);

      System.out.println(boardFromNotation);
    }
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