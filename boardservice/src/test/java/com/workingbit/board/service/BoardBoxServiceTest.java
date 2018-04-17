package com.workingbit.board.service;

import com.workingbit.board.controller.util.BaseServiceTest;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Notation;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.*;
import com.workingbit.share.util.Utils;
import net.percederberg.grammatica.parser.ParserCreationException;
import net.percederberg.grammatica.parser.ParserLogException;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.workingbit.board.controller.util.BoardUtils.findSquareByNotation;
import static com.workingbit.share.model.EnumRules.*;
import static org.junit.Assert.*;

/**
 * Created by Aleksey Popryaduhin on 10:08 10/08/2017.
 */
@RunWith(Theories.class)
public class BoardBoxServiceTest extends BaseServiceTest {

  private static final String[] PDN_FILE_NAME_VARIANT_WITH_FORWARD_MOVE = {
//      "/pdn/notation_variant_with_forward_move1.pdn",
      "/pdn/notation_variant_with_forward_move2.pdn",
  };
  private static final String FLAT_NOTATION_FILE_NAME = "/pdn/notation_variant_nested.pdn";
  public static @DataPoints
  boolean[] blacks = {true, false};

  public static @DataPoints
  boolean[] fillBoards = {true, false};

  public static @DataPoints
  EnumRules[] ruless = {RUSSIAN, RUSSIAN_GIVEAWAY, INTERNATIONAL, INTERNATIONAL_GIVEAWAY};

  private static final String[] PDN_FILE_NAME_VARIANT = {
      "/pdn/notation_undo1.pdn",
//      "/pdn/notation_undo2.pdn",
      "/pdn/notation_variant1.pdn",
//      "/pdn/notation_variant_with_one_drive_and_two_move.pdn",
//      "/pdn/notation_variant_with_one_drive_and_one_move.pdn",
//      "/pdn/notation_variant_with_two_move.pdn",
//      "/pdn/notation_variant_with_one_move.pdn",
  };

  private final List<String> PDN_FILE_NAMES_PARSE = new ArrayList<String>() {{
    add("/pdn/example_multivariants1.pdn");
    add("/pdn/example_multivariants2.pdn");
    add("/pdn/example.pdn");
    add("/pdn/notation_error1.pdn");
    add("/pdn/notation_comment.pdn");
    add("/pdn/notation_simple.pdn");
    add("/pdn/notation_strength.pdn");
    add("/pdn/notation_variant.pdn");
    add("/pdn/notation_variant_nested.pdn");
  }};

  private final List<String> PDN_FILE_NAME_BOARDS = new ArrayList<String>() {{
    add("/pdn/notation_error1.pdn");
  }};
  private Optional<AuthUser> token;

  @Before
  public void setUp() throws Exception {
    token = Optional.of(new AuthUser(Utils.getRandomString(), Utils.getRandomString()));
  }

  @Test
  public void createBoard() {
    BoardBox boardBox = boardBoxService().createBoardBox(
        getCreateBoardRequest(false, false, RUSSIAN, EnumEditBoardBoxMode.EDIT), token).get();
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
    BoardBox board = getBoardBoxWhiteNotFilledRUSSIAN();
    toDelete(board);
    assertNotNull(board.getId());
    Optional<BoardBox> byId = boardBoxService().findById(board.getId(), token);
    assertNotNull(byId.get());
  }

  @Test
  public void delete() throws Exception {
    BoardBox board = getBoardBoxWhiteNotFilledRUSSIAN();
    String boardId = board.getId();
    assertNotNull(boardId);
    boardBoxService().delete(boardId);
    Optional<BoardBox> byId = boardBoxService().findById(boardId, token);
    assertTrue(!byId.isPresent());
  }

  protected BoardBox getBoardBoxWhiteNotFilledRUSSIAN() {
    return getBoardBox(false, false, RUSSIAN, EnumEditBoardBoxMode.EDIT);
  }

  @Test
  public void test_create_board_from_pdn_notation() throws URISyntaxException, IOException, ParserLogException, ParserCreationException {
    for (String fileName : PDN_FILE_NAME_BOARDS) {
      System.out.println("LOADED PDN FILE: " + fileName);
      URL uri = getClass().getResource(fileName);
      Path path = Paths.get(uri.toURI());
      BufferedReader bufferedReader = Files.newBufferedReader(path);

      // Parse Notation
      Notation notation = notationParserService.parse(bufferedReader);
      notation.setRules(RUSSIAN);

      String articleId = Utils.getRandomString();
      String boardBoxId = Utils.getRandomString();

      // Create BoardBox from Notation
      BoardBox boardBox = boardBoxService.createBoardBoxFromNotation(articleId, notation).get();

      // Test create BoardBox moving draughts
      NotationHistory notationDrives = boardBox.getNotation().getNotationHistory();
      BoardBox current = boardBox.deepClone();
      for (NotationDrive drive : notationDrives.getNotation()) {
        for (NotationMove move : drive.getMoves()) {
          current = moveStrokes(current, move);
        }
      }
      String newPdn = current.getNotation().toPdn();
      String oldPdn = boardBox.getNotation().toPdn();
      assertEquals(oldPdn, newPdn);
    }
  }

  @Test
  public void test_create_variant() throws URISyntaxException, IOException, ParserLogException, ParserCreationException {
    for (String fileName : PDN_FILE_NAME_VARIANT) {
      LoadNotationForkNumberAndForwardMoves loadNotationForkNumberAndForwardMoves = new LoadNotationForkNumberAndForwardMoves(fileName).invoke();
      int forkNumber = loadNotationForkNumberAndForwardMoves.getForkNumber();
      BufferedReader bufferedReader = loadNotationForkNumberAndForwardMoves.getBufferedReader();

      // Parse Notation
      Notation notation = notationParserService.parse(bufferedReader);
      notation.setRules(RUSSIAN);

      String articleId = Utils.getRandomString();
      String boardBoxId = Utils.getRandomString();

      // Create BoardBox from Notation
      BoardBox boardBox = boardBoxService.createBoardBoxFromNotation(articleId, notation).get();

      NotationHistory notationDrives = boardBox.getNotation().getNotationHistory();

      NotationDrive forkDrive = notationDrives.get(forkNumber);

      BoardBox boardBoxVariant = boardBoxService.forkNotation(boardBox, token).get();

      NotationHistory nds = boardBoxVariant.getNotation().getNotationHistory();
      NotationDrive nd = nds.get(forkNumber - 1);
      assertEquals(nd.getVariants().getLast().getVariants().size(), notationDrives.size() - forkNumber);
      System.out.println("Prev: " + notationDrives.variantsToPdn());
      System.out.println("New: " + nds.variantsToPdn());
    }
  }

  @Test
  public void test_switch_to_variant() throws IOException, ParserLogException, ParserCreationException, URISyntaxException {
    for (String fileName : PDN_FILE_NAME_VARIANT) {
      LoadNotationForkNumberAndForwardMoves loadNotationForkNumberAndForwardMoves = new LoadNotationForkNumberAndForwardMoves(fileName).invoke();
      int forkNumber = loadNotationForkNumberAndForwardMoves.getForkNumber();
      BufferedReader bufferedReader = loadNotationForkNumberAndForwardMoves.getBufferedReader();

      // Parse Notation
      Notation notation = notationParserService.parse(bufferedReader);
      notation.setRules(RUSSIAN);

      String articleId = Utils.getRandomString();
      String boardBoxId = Utils.getRandomString();

      // Create BoardBox from Notation
      BoardBox boardBox = boardBoxService.createBoardBoxFromNotation(articleId, notation).get();

      // forkNumber notation by index from test file
      NotationHistory notationDrives = boardBox.getNotation().getNotationHistory();
      NotationDrive forkDrive = notationDrives.get(forkNumber);
      BoardBox boardBoxVariant = boardBoxService.forkNotation(boardBox, token).get();

      // get previous drive
      NotationHistory nds = boardBoxVariant.getNotation().getNotationHistory();
      NotationDrive nd = nds.get(forkNumber - 1);

      // switch
      BoardBox switched = boardBoxService.switchNotation(boardBoxVariant, token).get();
      switched.getNotation().print();
      System.out.println(switched.getNotation().toPdn());
    }
  }

  @Test
  public void test_create_variant_with_forward_move() throws IOException, ParserLogException, ParserCreationException, URISyntaxException {
    for (String fileName : PDN_FILE_NAME_VARIANT_WITH_FORWARD_MOVE) {
      LoadNotationForkNumberAndForwardMoves loadNotationForkNumberAndForwardMoves = new LoadNotationForkNumberAndForwardMoves(fileName).invoke();
      int forkNumber = loadNotationForkNumberAndForwardMoves.getForkNumber();
      List<String> forwardNotationLines = loadNotationForkNumberAndForwardMoves.getForwardNotationLines();
      BufferedReader bufferedReader = loadNotationForkNumberAndForwardMoves.getBufferedReader();

      // Parse Notation
      Notation notation = notationParserService.parse(bufferedReader);
      notation.setRules(RUSSIAN);

      String articleId = Utils.getRandomString();
      String boardBoxId = Utils.getRandomString();

      // Create BoardBox from Notation
      BoardBox boardBox = boardBoxService.createBoardBoxFromNotation(articleId, notation).get();

      // forkNumber notation by index from test file
      NotationHistory notationDrives = boardBox.getNotation().getNotationHistory();
      NotationDrive forkDrive = notationDrives.get(forkNumber);
      BoardBox boardBoxVariant = boardBoxService.forkNotation(boardBox, token).get();

      System.out.println(boardBoxVariant.getNotation().getNotationHistory().variantsToPdn());

      Notation forwardNotation = notationParserService.parse(StringUtils.join(forwardNotationLines, "\n"));
      NotationDrive forwardDrive = forwardNotation.getNotationHistory().get(1);

      BoardBox current = boardBoxVariant.deepClone();
      for (NotationMove move : forwardDrive.getMoves()) {
        current = moveStrokes(current, move);
      }

      System.out.println(current.getNotation().getNotationHistory().variantsToPdn());
    }
  }

  @Test
  public void test_switch_to_variant_with_forward_move() throws IOException, ParserLogException, ParserCreationException, URISyntaxException {
    for (String fileName : PDN_FILE_NAME_VARIANT_WITH_FORWARD_MOVE) {
      System.out.println("LOADED PDN FILE: " + fileName);
      URL uri = getClass().getResource(fileName);
      Path path = Paths.get(uri.toURI());
      List<String> lines = Files.readAllLines(path);
      int lineCount = Integer.parseInt(lines.remove(0));
      int forkNumber = Integer.parseInt(lines.get(0));
      List<String> forwardNotationLines = new ArrayList<>(lines.subList(0, lineCount));
      lines.removeAll(forwardNotationLines);
      forwardNotationLines.remove(0);

      BufferedReader bufferedReader = new BufferedReader(new StringReader(StringUtils.join(lines, "\n")));

      // Parse Notation
      Notation notation = notationParserService.parse(bufferedReader);
      notation.setRules(RUSSIAN);

      String articleId = Utils.getRandomString();
      String boardBoxId = Utils.getRandomString();

      // Create BoardBox from Notation
      BoardBox boardBox = boardBoxService.createBoardBoxFromNotation(articleId, notation).get();

      // forkNumber notation by index from test file
      NotationHistory notationDrives = boardBox.getNotation().getNotationHistory();
      NotationDrive forkDrive = notationDrives.get(forkNumber);
      BoardBox boardBoxVariant = boardBoxService.forkNotation(boardBox, token).get();

      Notation forwardNotation = notationParserService.parse(StringUtils.join(forwardNotationLines, "\n"));
      NotationDrive forwardDrive = forwardNotation.getNotationHistory().get(1);

      BoardBox current = boardBoxVariant.deepClone();
      for (NotationMove move : forwardDrive.getMoves()) {
        current = moveStrokes(current, move);
      }

      System.out.println(current.getNotation().getNotationHistory().variantsToPdn());

      // get previous drive
      NotationHistory nds = current.getNotation().getNotationHistory();
      NotationDrive nd = nds.get(forkNumber - 1);

      boardBox = current.deepClone();
      BoardBox switched = boardBoxService.switchNotation(current, token).get();

      System.out.println(switched.getNotation().getNotationHistory().variantsToPdn());

      boardBox = boardBoxService.find(boardBox, token.get()).get();
      System.out.println("SWITCH: " + boardBox.getNotation().getNotationHistory().variantsToPdn());
    }
  }

  @Test
  public void test_double_switch_to_variant_with_forward_move() throws IOException, ParserLogException, ParserCreationException, URISyntaxException {
    for (String fileName : PDN_FILE_NAME_VARIANT_WITH_FORWARD_MOVE) {
      System.out.println("LOADED PDN FILE: " + fileName);
      URL uri = getClass().getResource(fileName);
      Path path = Paths.get(uri.toURI());
      List<String> lines = Files.readAllLines(path);
      int lineCount = Integer.parseInt(lines.remove(0));
      int forkNumber = Integer.parseInt(lines.get(0));
      List<String> forwardNotationLines = new ArrayList<>(lines.subList(0, lineCount));
      lines.removeAll(forwardNotationLines);
      forwardNotationLines.remove(0);

      BufferedReader bufferedReader = new BufferedReader(new StringReader(StringUtils.join(lines, "\n")));

      // Parse Notation
      Notation notation = notationParserService.parse(bufferedReader);
      notation.setRules(RUSSIAN);

      String articleId = Utils.getRandomString();
      String boardBoxId = Utils.getRandomString();

      // Create BoardBox from Notation
      BoardBox boardBox = boardBoxService.createBoardBoxFromNotation(articleId, notation).get();

      // forkNumber notation by index from test file
      NotationHistory notationDrives = boardBox.getNotation().getNotationHistory();
      NotationDrive forkDrive = notationDrives.get(forkNumber);
      BoardBox fork1 = boardBoxService.forkNotation(boardBox, token).get();

      Notation forwardNotation = notationParserService.parse(StringUtils.join(forwardNotationLines, "\n"));
      NotationDrive forwardDrive = forwardNotation.getNotationHistory().get(1);

      BoardBox current = fork1.deepClone();
      for (NotationMove move : forwardDrive.getMoves()) {
        current = moveStrokes(current, move);
      }

      System.out.println(current.getNotation().getNotationHistory().variantsToPdn());

      // get previous drive
      NotationHistory nds = current.getNotation().getNotationHistory();
      NotationDrive nd = nds.get(forkNumber - 1);

      boardBox = current.deepClone();
      BoardBox switch1 = boardBoxService.switchNotation(current, token).get();

      System.out.println(switch1.getNotation().getNotationHistory().variantsToPdn());

      boardBox = boardBoxService.find(boardBox, token.get()).get();
      System.out.println("SWITCH: " + boardBox.getNotation().getNotationHistory().variantsToPdn());

      // forkNumber notation by index from test file
      notationDrives = boardBox.getNotation().getNotationHistory();
      forkDrive = notationDrives.get(forkNumber);
      BoardBox fork2 = boardBoxService.forkNotation(boardBox, token).get();

      forwardNotation = notationParserService.parse(StringUtils.join(forwardNotationLines, "\n"));
      forwardDrive = forwardNotation.getNotationHistory().get(1);

      current = fork2.deepClone();
      for (NotationMove move : forwardDrive.getMoves()) {
        current = moveStrokes(current, move);
      }

      // get previous drive
      nds = current.getNotation().getNotationHistory();
      nd = nds.get(forkNumber - 1);

      BoardBox switched2 = boardBoxService.switchNotation(current, token).get();

      System.out.println(switched2.getNotation().getNotationHistory().variantsToPdn());

      assertEquals(fork1.getNotation().getNotationHistory().getNotation(),
          fork2.getNotation().getNotationHistory().getNotation());

      assertEquals(switch1.getNotation().getNotationHistory().getNotation(),
          switched2.getNotation().getNotationHistory().getNotation());
    }
  }

  @Test
  public void test_double_fork() throws IOException, ParserLogException, ParserCreationException, URISyntaxException {
    for (String fileName : PDN_FILE_NAME_VARIANT) {
      System.out.println("LOADED PDN FILE: " + fileName);
      URL uri = getClass().getResource(fileName);
      Path path = Paths.get(uri.toURI());
      List<String> lines = Files.readAllLines(path);
      String startVariantDriveMove = lines.remove(0);

      BufferedReader bufferedReader = new BufferedReader(new StringReader(StringUtils.join(lines, "\n")));

      // Parse Notation
      Notation notation = notationParserService.parse(bufferedReader);
      notation.setRules(RUSSIAN);

      String articleId = Utils.getRandomString();
      String boardBoxId = Utils.getRandomString();

      // Create BoardBox from Notation
      BoardBox boardBox = boardBoxService.createBoardBoxFromNotation(articleId, notation).get();

      // forkNumber notation by index from test file
      int forkDriveIndex = Integer.parseInt(startVariantDriveMove);
      NotationHistory notationDrives = boardBox.getNotation().getNotationHistory();
      NotationDrive forkDrive = notationDrives.get(forkDriveIndex);
      BoardBox boardBoxVariant = boardBoxService.forkNotation(boardBox, token).get();
      String firstForkPdn = boardBoxVariant.getNotation().toPdn();

      // get previous drive
      NotationHistory nds = boardBoxVariant.getNotation().getNotationHistory();
      NotationDrive nd = nds.get(forkDriveIndex - 1);

      // switch
      BoardBox switched = boardBoxService.switchNotation(boardBoxVariant, token).get();

      BoardBox doubleFork = boardBoxService.forkNotation(switched, token).get();
      String secondForkPdn = doubleFork.getNotation().toPdn();
      doubleFork.getNotation().print();
      System.out.println(doubleFork.getNotation().toPdn());
      assertEquals(firstForkPdn, secondForkPdn);
    }
  }

  private BoardBox undoMove(BoardBox boardBoxCurrent, NotationMove notationMove) {
    LinkedList<NotationSimpleMove> move = notationMove.getMove();
    for (NotationSimpleMove m : move) {
      boardBoxCurrent = boardBoxService.save(boardBoxCurrent, token).get();
      boardBoxCurrent = boardBoxService.highlight(boardBoxCurrent, token).get();
      boardBoxCurrent = boardBoxService.undo(boardBoxCurrent, token).get();
    }
    return boardBoxCurrent;
  }

  public BoardBox moveStrokes(BoardBox boardBoxCurrent, NotationMove notationMove) {
    NotationSimpleMove[] move = notationMove.getMove().toArray(new NotationSimpleMove[0]);
    for (int i = 0; i < move.length - 1; i++) {
//      String boardId = notationMove.getBoardId();
      Board board = boardBoxCurrent.getBoard(); /*boardBoxService.find(boardId).get();*/

      String selMove = move[i].getNotation();
      Square selected = findSquareByNotation(selMove, board);
      board.setSelectedSquare(selected);

      String nextMove = move[i + 1].getNotation();
      Square next = findSquareByNotation(nextMove, board);
      next.setHighlight(true);
      board.setNextSquare(next);

      boardDao.save(board);

      boardBoxCurrent.setBoard(board);
      boardBoxCurrent.setBoardId(board.getId());
      boardBoxDao.save(boardBoxCurrent);

//      boardBoxCurrent = boardBoxService.save(boardBoxCurrent).get();
//      boardBoxCurrent = boardBoxService.highlight(boardBoxCurrent).get();
      boardBoxCurrent = boardBoxService.move(boardBoxCurrent, token).get();
    }
    return boardBoxCurrent;
  }

  @Test
  public void test_reparse_from_pdn() throws Exception {
    for (String fileName : PDN_FILE_NAMES_PARSE) {
      System.out.println("Test filename: " + fileName);
      URL uri = getClass().getResource(fileName);
      Path path = Paths.get(uri.toURI());
      BufferedReader bufferedReader = Files.newBufferedReader(path);

      Notation notation = notationParserService.parse(bufferedReader);
      String reparsed = notation.toPdn();
      List<String> lines = Files.readAllLines(path);
      String origin = StringUtils.join(lines, "\n");
      assertEquals(origin, reparsed);


      System.out.println("LOADED PDN FILE: " + fileName);
      System.out.println(reparsed);
      notation.print();
      System.out.println("---");
      System.out.println();
    }
  }

  @Test
  public void test_add_draught_in_not_place_mode_fail() {
    BoardBox boardBox = getBoardBoxWhiteNotFilledRUSSIAN();

    Board board = boardBox.getBoard();
    board = addWhiteDraught(board, "c3");
    boardBox.setBoard(board);
    boolean isPresent = boardBoxService.addDraught(boardBox, token).isPresent();
    assertFalse(isPresent);
  }

  @Test
  public void test_add_draught_in_place_mode() {
    BoardBox boardBox = getBoardBoxWhiteNotFilledRUSSIAN();
    boardBox.setEditMode(EnumEditBoardBoxMode.PLACE);
    boardBox = boardBoxService.save(boardBox, token).get();

    Board board = boardBox.getBoard();
    board = addWhiteDraught(board, "c3");
    boardBox.setBoard(board);
    boolean isPresent = boardBoxService.addDraught(boardBox, token).isPresent();
    assertTrue(isPresent);
  }

  @Test
  public void test_capture_on_placed_board() {
    BoardBox boardBox = getBoardBoxWhiteNotFilledRUSSIAN();
    boardBox.setEditMode(EnumEditBoardBoxMode.PLACE);
    boardBox = boardBoxService.save(boardBox, token).get();

    Board board = boardBox.getBoard();
    board = addWhiteDraught(board, "c3");
    boardBox.setBoard(board);
    boardBox = boardBoxService.addDraught(boardBox, token).get();

    board = boardBox.getBoard();
    board = addBlackDraught(board, "d4");
    boardBox.setBoard(board);
    boardBox = boardBoxService.addDraught(boardBox, token).get();

    // because place mode
    boolean isPresent = boardBoxService.move(boardBox, token).isPresent();
    assertFalse(isPresent);

    boardBox.setEditMode(EnumEditBoardBoxMode.MOVE);
    boardBox = boardBoxService.save(boardBox, token).get();

    board = boardBox.getBoard();

    Square c3 = getSquare(board, "c3");
    board.setSelectedSquare(c3);
    Square e5 = getSquare(board, "e5");
    e5.setHighlight(true);
    board.setNextSquare(e5);
    boardBox.setBoard(board);

    boardBox = boardBoxService.move(boardBox, token).get();
    board = boardBox.getBoard();
    Square e5n = getSquare(board, "e5");
    Square c3n = getSquare(board, "c3");
    assertNotNull(e5n.getDraught());
    assertNull(c3n.getDraught());

    String pdn = boardBox.getNotation().getNotationHistory().variantsToPdn();
    assertTrue(pdn.contains("c3xe5"));
  }

  @Test
  public void test_undo() throws URISyntaxException, IOException, ParserLogException, ParserCreationException {
    for (String fileName : PDN_FILE_NAME_VARIANT) {
      LoadNotationForkNumberAndForwardMoves loadNotationForkNumberAndForwardMoves = new LoadNotationForkNumberAndForwardMoves(fileName).invoke();
      int forkNumber = loadNotationForkNumberAndForwardMoves.getForkNumber();
      List<String> forwardNotationLines = loadNotationForkNumberAndForwardMoves.getForwardNotationLines();
      BufferedReader bufferedReader = loadNotationForkNumberAndForwardMoves.getBufferedReader();

      // Parse Notation
      Notation notation = notationParserService.parse(bufferedReader);
      notation.setRules(RUSSIAN);

      String articleId = Utils.getRandomString();
      String boardBoxId = Utils.getRandomString();

      // Create BoardBox from Notation
      BoardBox boardBox = boardBoxService.createBoardBoxFromNotation(articleId, notation).get();

//      String firstBoardId = boardBox.getNotation().getNotationHistory().get(1).getMoves().getFirst().getBoardId();
//      Board board = boardDao.findById(firstBoardId).get();
//      String initBoardId = board.getPreviousBoards().getLastOrCreateIfRoot().getBoardId();
//      board = boardDao.findById(initBoardId).get();
//      board.setNotationHistory(NotationHistory.createWithRoot());
//      boardBox.setBoard(board);
//      boardBox.getNotation().setNotationHistory(board.getNotationHistory());
//      boardBoxService.saveAndFillBoard(boardBox);
//
//      Notation forwardNotation = notationParserService.parse(StringUtils.join(forwardNotationLines, "\n"));
//      NotationDrive forwardDrive = forwardNotation.getNotationHistory().get(1);
//
//      BoardBox current = boardBox.deepClone();
//      for (NotationMove move : forwardDrive.getMoves()) {
//        current = moveStrokes(current, move);
//      }

//      System.out.println(current.getNotation().getNotationHistory().variantsToPdn());

      NotationHistory notationDrives = boardBox.getNotation().getNotationHistory().deepClone();

      boardBox.getNotation().setNotationHistory(NotationHistory.createWithRoot());
      boardBoxDao.save(boardBox);
      for (NotationDrive notationDrive : notationDrives.getNotation()) {
        for (NotationMove notationMove : notationDrive.getMoves()) {
          boardBox = moveStrokes(boardBox, notationMove);
        }
      }

      boardBox = boardBoxService.undo(boardBox, token).get();
      System.out.println("UNDO: " + boardBox.getNotation().toPdn());
      assertEquals(0, boardBox.getNotation().getNotationHistory().getNotation().getLast().getVariants().size());
      boolean undoNotPossible = !boardBoxService.undo(boardBox, token).isPresent();
      System.out.println("UNDO: " + boardBox.getNotation().toPdn());
      assertTrue(undoNotPossible);
      break;
    }
  }

  @Test
  public void test_undo_redo() throws URISyntaxException, IOException, ParserLogException, ParserCreationException {
    for (String fileName : PDN_FILE_NAME_VARIANT) {
      LoadNotationForkNumberAndForwardMoves loadNotationForkNumberAndForwardMoves = new LoadNotationForkNumberAndForwardMoves(fileName).invoke();
      int forkNumber = loadNotationForkNumberAndForwardMoves.getForkNumber();
      List<String> forwardNotationLines = loadNotationForkNumberAndForwardMoves.getForwardNotationLines();
      BufferedReader bufferedReader = loadNotationForkNumberAndForwardMoves.getBufferedReader();

      // Parse Notation
      Notation notation = notationParserService.parse(bufferedReader);
      notation.setRules(RUSSIAN);

      String articleId = Utils.getRandomString();
      String boardBoxId = Utils.getRandomString();

      // Create BoardBox from Notation
      BoardBox boardBox = boardBoxService.createBoardBoxFromNotation(articleId, notation).get();

      NotationHistory notationDrives = boardBox.getNotation().getNotationHistory().deepClone();

      boardBox.getNotation().setNotationHistory(NotationHistory.createWithRoot());
      boardBoxDao.save(boardBox);
      for (NotationDrive notationDrive : notationDrives.getNotation()) {
        for (NotationMove notationMove : notationDrive.getMoves()) {
          boardBox = moveStrokes(boardBox, notationMove);
        }
      }

      boardBox = boardBoxService.undo(boardBox, token).get();
      System.out.println("UNDO: " + boardBox.getNotation().toPdn());
      assertEquals(1, boardBox.getNotation().getNotationHistory().getNotation().getLast().getVariants().size());
      boardBox = boardBoxService.undo(boardBox, token).get();
      System.out.println("UNDO: " + boardBox.getNotation().toPdn());
      assertEquals(1, boardBox.getNotation().getNotationHistory().getNotation().getLast().getVariants().size());

      boardBox = boardBoxService.redo(boardBox, token).get();
      System.out.println("UNDO: " + boardBox.getNotation().toPdn());
      assertEquals(1, boardBox.getNotation().getNotationHistory().getNotation().getLast().getVariants().size());
      boardBox = boardBoxService.redo(boardBox, token).get();
      System.out.println("UNDO: " + boardBox.getNotation().toPdn());
      assertEquals(0, boardBox.getNotation().getNotationHistory().getNotation().getLast().getVariants().size());
      break;
    }
  }

  @Test
  public void test_undo_redo2() throws URISyntaxException, IOException, ParserLogException, ParserCreationException {
    LoadNotationForkNumberAndForwardMoves loadNotationForkNumberAndForwardMoves = new LoadNotationForkNumberAndForwardMoves("/pdn/notation_undo1.pdn").invoke();
    int forkNumber = loadNotationForkNumberAndForwardMoves.getForkNumber();
    List<String> forwardNotationLines = loadNotationForkNumberAndForwardMoves.getForwardNotationLines();
    BufferedReader bufferedReader = loadNotationForkNumberAndForwardMoves.getBufferedReader();

    // Parse Notation
    Notation notation = notationParserService.parse(bufferedReader);
    notation.setRules(RUSSIAN);

    String articleId = Utils.getRandomString();
    String boardBoxId = Utils.getRandomString();

    // Create BoardBox from Notation
    BoardBox boardBox = boardBoxService.createBoardBoxFromNotation(articleId, notation).get();

    BoardBox boardBoxOrig = boardBox.deepClone();

    NotationHistory notationDrives = boardBox.getNotation().getNotationHistory().deepClone();

    boardBox.getNotation().setNotationHistory(NotationHistory.createWithRoot());
    boardBoxDao.save(boardBox);
    for (NotationDrive notationDrive : notationDrives.getNotation()) {
      for (NotationMove notationMove : notationDrive.getMoves()) {
        boardBox = moveStrokes(boardBox, notationMove);
      }
    }

    boardBox.getNotation().getNotationHistory().printPdn();

    boardBox = boardBoxService.undo(boardBox, token).get();
    System.out.print("UNDO ");
    boardBox.getNotation().getNotationHistory().printPdn();
    boardBox = boardBoxService.undo(boardBox, token).get();
    System.out.print("UNDO ");
    boardBox.getNotation().getNotationHistory().printPdn();

    boardBox = redo(boardBox);
    boardBox = redo(boardBox);
    assertEquals(boardBoxOrig.getNotation().getNotationHistory().getNotation().toPdn(),
        boardBox.getNotation().getNotationHistory().getNotation().toPdn());

    // MOVE FORWARD
    Notation forwardNotation = notationParserService.parse(StringUtils.join(forwardNotationLines, "\n"));

    BoardBox current = boardBox;
    for (NotationDrive forwardDrive: forwardNotation.getNotationHistory().getNotation()) {
      for (NotationMove move : forwardDrive.getMoves()) {
        current = moveStrokes(current, move);
        System.out.println(move.toPdn());
      }
    }

    boardBoxOrig = current.deepClone();

    boardBox = boardBoxService.undo(boardBox, token).get();
    System.out.print("UNDO ");
    boardBox.getNotation().getNotationHistory().printPdn();
    boardBox = boardBoxService.undo(boardBox, token).get();
    System.out.print("UNDO ");
    boardBox.getNotation().getNotationHistory().printPdn();

    boardBox = redo(boardBox);
    boardBox = redo(boardBox);
    assertEquals(boardBoxOrig.getNotation().getNotationHistory().getNotation(),
        boardBox.getNotation().getNotationHistory().getNotation());
  }

  @Test
  public void test_undo_redo3() throws URISyntaxException, IOException, ParserLogException, ParserCreationException {
    LoadNotationForkNumberAndForwardMoves loadNotationForkNumberAndForwardMoves = new LoadNotationForkNumberAndForwardMoves("/pdn/notation_undo3.pdn").invoke();
    int forkNumber = loadNotationForkNumberAndForwardMoves.getForkNumber();
    List<String> forwardNotationLines = loadNotationForkNumberAndForwardMoves.getForwardNotationLines();
    BufferedReader bufferedReader = loadNotationForkNumberAndForwardMoves.getBufferedReader();

    // Parse Notation
    Notation notation = notationParserService.parse(bufferedReader);
    notation.setRules(RUSSIAN);
    notation.getNotationHistory().printPdn();

    String articleId = Utils.getRandomString();
    String boardBoxId = Utils.getRandomString();

    // Create BoardBox from Notation
    BoardBox boardBox = boardBoxService.createBoardBoxFromNotation(articleId, notation).get();

    BoardBox boardBoxOrig = boardBox.deepClone();

    NotationHistory notationDrives = boardBox.getNotation().getNotationHistory().deepClone();
    notationDrives.printPdn();


    for (NotationDrive notationDrive : notationDrives.getNotation()) {
      for (NotationMove notationMove : notationDrive.getMoves()) {
        boardBox = moveStrokes(boardBox, notationMove);
      }
    }

    boardBox.getNotation().getNotationHistory().printPdn();

//    boardBox = boardBoxService.undo(boardBox).get();
//    System.out.print("UNDO ");
//    boardBox.getNotation().getNotationHistory().printPdn();
    boardBox = undo(boardBox);

//    boardBox = boardBoxService.redo(boardBox).get();
//    System.out.print("REDO ");
//    boardBox.getNotation().getNotationHistory().printPdn();
//    boardBox = boardBoxService.redo(boardBox).get();
//    System.out.print("REDO ");
//    boardBox.getNotation().getNotationHistory().printPdn();
//    assertEquals(boardBoxOrig.getNotation().getNotationHistory().getNotation().toPdn(),
//        boardBox.getNotation().getNotationHistory().getNotation().toPdn());

//    boardBox = boardBoxService.undo(boardBox).get();
//    System.out.print("UNDO ");
//    boardBox.getNotation().getNotationHistory().printPdn();
//    boardBox = boardBoxService.undo(boardBox).get();
//    System.out.print("UNDO ");
//    boardBox.getNotation().getNotationHistory().printPdn();

    boardBox = redo(boardBox);
//    boardBox = boardBoxService.redo(boardBox).get();
//    System.out.print("REDO ");
//    boardBox.getNotation().getNotationHistory().printPdn();
//    assertEquals(boardBoxOrig.getNotation().getNotationHistory().getNotation(),
//        boardBox.getNotation().getNotationHistory().getNotation());

    // MOVE FORWARD
    Notation forwardNotation = notationParserService.parse(StringUtils.join(forwardNotationLines, "\n"));

    BoardBox current = boardBox;
    for (NotationDrive forwardDrive: forwardNotation.getNotationHistory().getNotation()) {
      for (NotationMove move : forwardDrive.getMoves()) {
        current = moveStrokes(current, move);
        System.out.println(move.toPdn());
      }
    }
    boardBoxOrig = current.deepClone();

    boardBox = undo(boardBox);
    boardBox = undo(boardBox);

    boardBox = redo(boardBox);

    System.out.println("***");
    BoardBox orig1 = boardBox.deepClone();
    boardBox = redo(boardBox);

    boardBox = undo(boardBox);
    boardBox = undo(boardBox);
    boardBox = undo(boardBox);

    boardBox = redo(boardBox);
    boardBox = redo(boardBox);
    boardBox = redo(boardBox);
    BoardBox orig2 = boardBox.deepClone();
    System.out.println("---");

    boardBox = undo(boardBox);
    boardBox = undo(boardBox);
    boardBox = undo(boardBox);

    boardBox = redo(boardBox);
    boardBox = redo(boardBox);
    boardBox = redo(boardBox);

    assertEquals(orig2.getNotation().getNotationHistory().pdnString(),
        boardBox.getNotation().getNotationHistory().pdnString());
  }

  public BoardBox redo(BoardBox boardBox) {
    boardBox = boardBoxService.redo(boardBox, token).get();
    System.out.println("REDO ");
    boardBox.getNotation().getNotationHistory().printPdn();
    return boardBox;
  }

  public BoardBox undo(BoardBox boardBox) {
    boardBox = boardBoxService.undo(boardBox, token).get();
    System.out.println("UNDO ");
    boardBox.getNotation().getNotationHistory().printPdn();
    return boardBox;
  }

  @After
  public void tearUp() {
    boards.forEach(board -> boardBoxService().delete(board.getId()));
  }

  private List<BoardBox> boards = new ArrayList<>();

  private void toDelete(BoardBox board) {
    boards.add(board);
  }

  protected BoardBox getBoardBox(boolean black, boolean fillBoard, EnumRules rules, EnumEditBoardBoxMode editMode) {
    CreateBoardPayload createBoardPayload = getCreateBoardRequest(black, fillBoard, rules, editMode);
    return boardBoxService().createBoardBox(createBoardPayload, token).get();
  }

  private class LoadNotationForkNumberAndForwardMoves {
    private String fileName;
    private int forkNumber;
    private List<String> forwardNotationLines;
    private BufferedReader bufferedReader;

    public LoadNotationForkNumberAndForwardMoves(String fileName) {
      this.fileName = fileName;
    }

    public int getForkNumber() {
      return forkNumber;
    }

    public List<String> getForwardNotationLines() {
      return forwardNotationLines;
    }

    public BufferedReader getBufferedReader() {
      return bufferedReader;
    }

    public LoadNotationForkNumberAndForwardMoves invoke() throws URISyntaxException, IOException {
      System.out.println("LOADED PDN FILE: " + fileName);
      URL uri = getClass().getResource(fileName);
      Path path = Paths.get(uri.toURI());
      List<String> lines = Files.readAllLines(path);
      int lineCount = Integer.parseInt(lines.remove(0));
      forkNumber = Integer.parseInt(lines.get(0));
      forwardNotationLines = new ArrayList<>(lines.subList(0, lineCount));
      lines.removeAll(forwardNotationLines);
      forwardNotationLines.remove(0);

      bufferedReader = new BufferedReader(new StringReader(StringUtils.join(lines, "\n")));
      return this;
    }
  }
}