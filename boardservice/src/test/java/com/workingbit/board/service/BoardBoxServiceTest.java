package com.workingbit.board.service;

import com.workingbit.board.controller.util.BaseServiceTest;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.*;
import com.workingbit.share.util.Utils;
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
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
  public static @DataPoints
  boolean[] blacks = {true, false};

  public static @DataPoints
  boolean[] fillBoards = {true, false};

  public static @DataPoints
  EnumRules[] ruless = {RUSSIAN, RUSSIAN_GIVEAWAY, INTERNATIONAL, INTERNATIONAL_GIVEAWAY};

  private static final String[] PDN_FILE_NAME_VARIANT = {
//      "/pdn/notation_undo1.pdn",
//      "/pdn/notation_undo2.pdn",
      "/pdn/notation_variant_with_one_drive_and_two_move.pdn",
      "/pdn/notation_variant_with_one_drive_and_one_move.pdn",
      "/pdn/notation_variant_with_two_move.pdn",
      "/pdn/notation_variant_with_one_move.pdn",
      "/pdn/notation_variant1.pdn",
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

  @Test
  public void createBoard() {
    BoardBox boardBox = boardBoxService().createBoardBox(
        getCreateBoardRequest(false, false, RUSSIAN, EnumEditBoardBoxMode.EDIT)).get();
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
    Optional<BoardBox> byId = boardBoxService().findById(board.getId());
    assertNotNull(byId.get());
  }

  @Test
  public void delete() throws Exception {
    BoardBox board = getBoardBoxWhiteNotFilledRUSSIAN();
    String boardId = board.getId();
    assertNotNull(boardId);
    boardBoxService().delete(boardId);
    Optional<BoardBox> byId = boardBoxService().findById(boardId);
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
      BoardBox boardBox = boardBoxService.createBoardBoxFromNotation(articleId, boardBoxId, notation).get();

      // Test create BoardBox moving draughts
      NotationDrivesContainer notationDrives = boardBox.getNotation().getNotationDrivesContainer();
      BoardBox current = boardBox.deepClone();
      for (NotationDrive drive : notationDrives.getVariants()) {
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
      BoardBox boardBox = boardBoxService.createBoardBoxFromNotation(articleId, boardBoxId, notation).get();

      NotationDrivesContainer notationDrives = boardBox.getNotation().getNotationDrivesContainer();

      NotationDrive forkDrive = notationDrives.get(forkNumber);

      BoardBox boardBoxVariant = boardBoxService.forkBoardBox(boardBox, forkDrive).get();

      NotationDrivesContainer nds = boardBoxVariant.getNotation().getNotationDrivesContainer();
      NotationDrive nd = nds.get(forkNumber - 1);
      assertEquals(nd.getVariants().getLast().getVariants().size(), notationDrives.countVariants() - forkNumber);
      System.out.println("Prev: " + notationDrives.variantsToPdn());
      System.out.println("New: " + nds.variantsToPdn());
    }
  }

  @Test
  public void test_switch_to_variant() throws IOException, ParserLogException, ParserCreationException, URISyntaxException {
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
      BoardBox boardBox = boardBoxService.createBoardBoxFromNotation(articleId, boardBoxId, notation).get();

      // forkNumber notation by index from test file
      int forkDriveIndex = Integer.parseInt(startVariantDriveMove);
      NotationDrivesContainer notationDrives = boardBox.getNotation().getNotationDrivesContainer();
      NotationDrive forkDrive = notationDrives.get(forkDriveIndex);
      BoardBox boardBoxVariant = boardBoxService.forkBoardBox(boardBox, forkDrive).get();

      // get previous drive
      NotationDrivesContainer nds = boardBoxVariant.getNotation().getNotationDrivesContainer();
      NotationDrive nd = nds.get(forkDriveIndex - 1);

      // switch
      BoardBox switched = boardBoxService.switchToNotationDrive(boardBoxVariant, nd).get();
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
      BoardBox boardBox = boardBoxService.createBoardBoxFromNotation(articleId, boardBoxId, notation).get();

      // forkNumber notation by index from test file
      NotationDrivesContainer notationDrives = boardBox.getNotation().getNotationDrivesContainer();
      NotationDrive forkDrive = notationDrives.get(forkNumber);
      BoardBox boardBoxVariant = boardBoxService.forkBoardBox(boardBox, forkDrive).get();

      System.out.println(boardBoxVariant.getNotation().getNotationDrivesContainer().variantsToPdn());

      Notation forwardNotation = notationParserService.parse(StringUtils.join(forwardNotationLines, "\n"));
      NotationDrive forwardDrive = forwardNotation.getNotationDrivesContainer().get(1);

      BoardBox current = boardBoxVariant.deepClone();
      for (NotationMove move : forwardDrive.getMoves()) {
        current = moveStrokes(current, move);
      }

      System.out.println(current.getNotation().getNotationDrivesContainer().variantsToPdn());
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
      BoardBox boardBox = boardBoxService.createBoardBoxFromNotation(articleId, boardBoxId, notation).get();

      // forkNumber notation by index from test file
      NotationDrivesContainer notationDrives = boardBox.getNotation().getNotationDrivesContainer();
      NotationDrive forkDrive = notationDrives.get(forkNumber);
      BoardBox boardBoxVariant = boardBoxService.forkBoardBox(boardBox, forkDrive).get();

      Notation forwardNotation = notationParserService.parse(StringUtils.join(forwardNotationLines, "\n"));
      NotationDrive forwardDrive = forwardNotation.getNotationDrivesContainer().get(1);

      BoardBox current = boardBoxVariant.deepClone();
      for (NotationMove move : forwardDrive.getMoves()) {
        current = moveStrokes(current, move);
      }

      System.out.println(current.getNotation().getNotationDrivesContainer().variantsToPdn());

      // get previous drive
      NotationDrivesContainer nds = current.getNotation().getNotationDrivesContainer();
      NotationDrive nd = nds.get(forkNumber - 1);

      boardBox = current.deepClone();
      BoardBox switched = boardBoxService.switchToNotationDrive(current, nd).get();

      System.out.println(switched.getNotation().getNotationDrivesContainer().variantsToPdn());

      boardBox = boardBoxService.find(boardBox).get();
      System.out.println(boardBox.getNotation().getNotationDrivesContainer().variantsToPdn());
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
      BoardBox boardBox = boardBoxService.createBoardBoxFromNotation(articleId, boardBoxId, notation).get();

      // forkNumber notation by index from test file
      int forkDriveIndex = Integer.parseInt(startVariantDriveMove);
      NotationDrivesContainer notationDrives = boardBox.getNotation().getNotationDrivesContainer();
      NotationDrive forkDrive = notationDrives.get(forkDriveIndex);
      BoardBox boardBoxVariant = boardBoxService.forkBoardBox(boardBox, forkDrive).get();
      String firstForkPdn = boardBoxVariant.getNotation().toPdn();

      // get previous drive
      NotationDrivesContainer nds = boardBoxVariant.getNotation().getNotationDrivesContainer();
      NotationDrive nd = nds.get(forkDriveIndex - 1);

      // switch
      BoardBox switched = boardBoxService.switchToNotationDrive(boardBoxVariant, nd).get();

      BoardBox doubleFork = boardBoxService.forkBoardBox(switched, forkDrive).get();
      String secondForkPdn = doubleFork.getNotation().toPdn();
      doubleFork.getNotation().print();
      System.out.println(doubleFork.getNotation().toPdn());
      assertEquals(firstForkPdn, secondForkPdn);
    }
  }

  private BoardBox undoMove(BoardBox boardBoxCurrent, NotationMove notationMove) {
    String[] move = notationMove.getMove();
    for (int i = move.length - 1; i > 0; i--) {
      boardBoxCurrent = boardBoxService.saveAndFillBoard(boardBoxCurrent).get();
      boardBoxCurrent = boardBoxService.highlight(boardBoxCurrent).get();
      boardBoxCurrent = boardBoxService.undo(boardBoxCurrent).get();
    }
    return boardBoxCurrent;
  }

  public BoardBox moveStrokes(BoardBox boardBoxCurrent, NotationMove notationMove) {
    String[] move = notationMove.getMove();
    for (int i = 0; i < move.length - 1; i++) {
//      String boardId = notationMove.getBoardId();
      Board board = boardBoxCurrent.getBoard(); /*boardService.find(boardId).get();*/

      String selMove = move[i];
      Square selected = findSquareByNotation(selMove, board);
      board.setSelectedSquare(selected);

      String nextMove = move[i + 1];
      Square next = findSquareByNotation(nextMove, board);
      next.setHighlighted(true);
      board.setNextSquare(next);

      boardDao.save(board);

      boardBoxCurrent.setBoard(board);
//      boardBoxCurrent.setBoardId(boardId);

      boardBoxCurrent = boardBoxService.saveAndFillBoard(boardBoxCurrent).get();
      boardBoxCurrent = boardBoxService.highlight(boardBoxCurrent).get();
      boardBoxCurrent = boardBoxService.move(boardBoxCurrent).get();
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
    boolean isPresent = boardBoxService.addDraught(boardBox).isPresent();
    assertFalse(isPresent);
  }

  @Test
  public void test_add_draught_in_place_mode() {
    BoardBox boardBox = getBoardBoxWhiteNotFilledRUSSIAN();
    boardBox.setEditMode(EnumEditBoardBoxMode.PLACE);
    boardBox = boardBoxService.saveAndFillBoard(boardBox).get();

    Board board = boardBox.getBoard();
    board = addWhiteDraught(board, "c3");
    boardBox.setBoard(board);
    boolean isPresent = boardBoxService.addDraught(boardBox).isPresent();
    assertTrue(isPresent);
  }

  @Test
  public void test_capture_on_placed_board() {
    BoardBox boardBox = getBoardBoxWhiteNotFilledRUSSIAN();
    boardBox.setEditMode(EnumEditBoardBoxMode.PLACE);
    boardBox = boardBoxService.saveAndFillBoard(boardBox).get();

    Board board = boardBox.getBoard();
    board = addWhiteDraught(board, "c3");
    boardBox.setBoard(board);
    boardBox = boardBoxService.addDraught(boardBox).get();

    board = boardBox.getBoard();
    board = addBlackDraught(board, "d4");
    boardBox.setBoard(board);
    boardBox = boardBoxService.addDraught(boardBox).get();

    // because place mode
    boolean isPresent = boardBoxService.move(boardBox).isPresent();
    assertFalse(isPresent);

    boardBox.setEditMode(EnumEditBoardBoxMode.MOVE);
    boardBox = boardBoxService.saveAndFillBoard(boardBox).get();

    board = boardBox.getBoard();

    Square c3 = getSquare(board, "c3");
    board.setSelectedSquare(c3);
    Square e5 = getSquare(board, "e5");
    e5.setHighlighted(true);
    board.setNextSquare(e5);
    boardBox.setBoard(board);

    boardBox = boardBoxService.move(boardBox).get();
    board = boardBox.getBoard();
    Square e5n = getSquare(board, "e5");
    Square c3n = getSquare(board, "c3");
    assertNotNull(e5n.getDraught());
    assertNull(c3n.getDraught());

    String pdn = boardBox.getNotation().getNotationDrivesContainer().variantsToPdn();
    assertTrue(pdn.contains("c3xe5"));
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
      BoardBox boardBox = boardBoxService.createBoardBoxFromNotation(articleId, boardBoxId, notation).get();

//      String firstBoardId = boardBox.getNotation().getNotationDrivesContainer().get(1).getMoves().getFirst().getBoardId();
//      Board board = boardDao.findByKey(firstBoardId).get();
//      String initBoardId = board.getPreviousBoards().getLastVariant().getBoardId();
//      board = boardDao.findByKey(initBoardId).get();
//      board.setNotationDrivesContainer(NotationDrivesContainer.createWithRoot());
//      boardBox.setBoard(board);
//      boardBox.getNotation().setNotationDrivesContainer(board.getNotationDrivesContainer());
//      boardBoxService.saveAndFillBoard(boardBox);
//
//      Notation forwardNotation = notationParserService.parse(StringUtils.join(forwardNotationLines, "\n"));
//      NotationDrive forwardDrive = forwardNotation.getNotationDrivesContainer().get(1);
//
//      BoardBox current = boardBox.deepClone();
//      for (NotationMove move : forwardDrive.getMoves()) {
//        current = moveStrokes(current, move);
//      }

//      System.out.println(current.getNotation().getNotationDrivesContainer().variantsToPdn());

      NotationDrivesContainer notationDrives = boardBox.getNotation().getNotationDrivesContainer().deepClone();

      Board board = boardBox.getBoard();
      board.setNotationDrivesContainer(NotationDrivesContainer.createWithRoot());
      boardDao.save(board);
      boardBox.getNotation().setNotationDrivesContainer(NotationDrivesContainer.createWithRoot());
      boardBoxDao.save(boardBox);
      for (NotationDrive notationDrive : notationDrives.getVariants()) {
        for (NotationMove notationMove : notationDrive.getMoves()) {
          boardBox = moveStrokes(boardBox, notationMove);
        }
      }

      boardBox = boardBoxService.undo(boardBox).get();
      System.out.println("UNDO: " + boardBox.getNotation().toPdn());
      boardBox = boardBoxService.undo(boardBox).get();
      System.out.println("UNDO: " + boardBox.getNotation().toPdn());
//      boardBox = boardBoxService.redo(boardBox).get();
//      System.out.println("REDO: " + boardBox.getNotation().variantsToPdn());
      break;
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

  protected BoardBox getBoardBox(boolean black, boolean fillBoard, EnumRules rules, EnumEditBoardBoxMode editMode) {
    CreateBoardPayload createBoardPayload = getCreateBoardRequest(black, fillBoard, rules, editMode);
    return boardBoxService().createBoardBox(createBoardPayload).get();
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