package com.workingbit.board.service;

import com.workingbit.board.controller.util.BaseServiceTest;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.*;
import net.percederberg.grammatica.parser.ParserCreationException;
import net.percederberg.grammatica.parser.ParserLogException;
import org.apache.commons.lang3.StringUtils;
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
import java.util.*;

import static com.workingbit.board.controller.util.BoardUtils.findSquareByNotation;
import static com.workingbit.share.model.EnumRules.*;
import static com.workingbit.share.util.Utils.getRandomString;
import static org.junit.Assert.*;

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

  private final Map<String, String> PDN_FILE_NAMES_PARSE = new HashMap<String, String>() {{
    put("/pdn/example.pdn", "/pdn/test1.test");
    put("/pdn/notation_comment.pdn", "/pdn/test1.test");
    put("/pdn/notation_simple.pdn", "/pdn/test1.test");
    put("/pdn/notation_strength.pdn", "/pdn/test1.test");
    put("/pdn/notation_variant.pdn", "/pdn/test1.test");
    put("/pdn/notation_variant_nested.pdn", "/pdn/test1.test");
  }};

  private final List<String> PDN_FILE_NAME_BOARDS = new ArrayList<String>() {{
    add("/pdn/notation_error1.pdn");
  }};

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
  public void test_create_board_from_pdn_notation() throws URISyntaxException, IOException, ParserLogException, ParserCreationException {
    for (String fileName : PDN_FILE_NAME_BOARDS) {
      System.out.println("LOADED PDN FILE: " + fileName);
      URL uri = getClass().getResource(fileName);
      Path path = Paths.get(uri.toURI());
      BufferedReader bufferedReader = Files.newBufferedReader(path);

      Notation notation = notationParserService.parse(bufferedReader);
      notation.setRules(RUSSIAN);

      BoardBox boardBoxEmpty = getSavedBoardBoxEmpty();
      String articleId = boardBoxEmpty.getArticleId();
      String boardBoxId = boardBoxEmpty.getId();
      Board boardFromNotation = boardService.createBoardFromNotation(notation, articleId, boardBoxId);

      assertNotNull(boardFromNotation);

      NotationDrives notationDrives = boardFromNotation.getNotationDrives();
      Board startBoard = boardService.findById(boardFromNotation.getPreviousBoards().getLast().getBoardId()).get();
      boardBoxEmpty.setBoard(startBoard);
      for (NotationDrive stroke : notationDrives) {
//        Board board = boardBoxEmpty.getBoard();
//        List<Board> firstStrokes = emulateMove(board, stroke.getMove());
//        boardBoxEmpty = moveStrokes(boardBoxEmpty, firstStrokes);
//        board = boardBoxEmpty.getBoard();
//        List<Board> secondStrokes = emulateMove(board, stroke.getSecond());
//        boardBoxEmpty = moveStrokes(boardBoxEmpty, secondStrokes);
      }
    }
  }

  public BoardBox moveStrokes(BoardBox boardBoxEmpty, List<Board> firstStrokes) {
    for (Board b : firstStrokes) {
      boardBoxEmpty.setBoard(b);
      boardBoxEmpty.setBoardId(b.getId());
      boardBoxEmpty = boardBoxService.saveAndFillBoard(boardBoxEmpty).get();
      boardBoxEmpty = boardBoxService.highlight(boardBoxEmpty).get();
      boardBoxEmpty = boardBoxService.move(boardBoxEmpty).get();
      boardBoxEmpty.getNotation().print();
      NotationDrive lastNewNotation = boardBoxEmpty.getNotation().getNotationDrives().getLast();
//      NotationMove atomStroke = lastNewNotation.getSecond() == null || lastNewNotation.getSecond().getType() == null
//          ? lastNewNotation.getFirst() : lastNewNotation.getSecond();
//      NotationDrive.EnumStrokeType moveType = atomStroke.getType();
//      String notationNew = atomStroke.getNotation();
//      String notationProposed = b.getSelectedSquare().getNotation() + moveType.getType() + b.getNextSquare().getNotation();
//      if (!notationProposed.equals(notationNew)) {
//        notationProposed = b.getSelectedSquare().getNotation() + moveType.getPdnType() + b.getNextSquare().getNotation();
//        if (!notationProposed.equals(notationNew)) {
//          assertFalse(notationProposed + " != " + notationNew, false);
//        }
//      }
    }
    return boardBoxEmpty;
  }

  private List<Board> emulateMove(Board board, NotationMove move) {
    if (move == null) {
      return Collections.emptyList();
    }
    List<Board> boards = new ArrayList<>();
    for (int i = 0; i < move.getMove().length - 1; i++) {
      Square selected = findSquareByNotation(move.getMove()[i], board);
      board.setSelectedSquare(selected);
      Square next = findSquareByNotation(move.getMove()[i + 1], board);
      next.setHighlighted(true);
      board.setNextSquare(next);
      board.setId(getRandomString());
      boardDao.save(board);
      boards.add(board);
    }
    return boards;
  }


  @Test
  public void test_reparse_from_pdn() throws Exception {
    for (Map.Entry<String, String> fileName : PDN_FILE_NAMES_PARSE.entrySet()) {
      System.out.println("LOADED PDN FILE: " + fileName);
      URL uri = getClass().getResource(fileName.getKey());
      Path path = Paths.get(uri.toURI());
      BufferedReader bufferedReader = Files.newBufferedReader(path);

      Notation notation = notationParserService.parse(bufferedReader);
      String reparsed = notation.toPdn();
      List<String> lines = Files.readAllLines(path);
      String origin = StringUtils.join(lines, "\n");
      assertEquals(origin, reparsed);
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