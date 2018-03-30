package com.workingbit.board.controller.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.Draught;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.workingbit.board.controller.util.HighlightMoveUtil.highlightedAssignedMoves;


/**
 * Created by Aleksey Popryaduhin on 20:56 11/08/2017.
 */
public class BoardUtils {

  /**
   * Fill board with draughts
   *
   * @param black is player plays black?
   */
  public static Board initBoard(boolean fillBoard, boolean black, EnumRules rules) {
    Board board = new Board(black, rules);
    return updateBoard(fillBoard, false, board);
  }

  public static Board updateBoard(Board board) {
    return updateBoard(false, true, board);
  }

  private static Board updateBoard(boolean fillBoard, boolean update, Board board) {
    Board boardClone = board.deepClone();
    EnumRules rules = boardClone.getRules();
    boolean black = boardClone.isBlack();

    Map<String, Draught> blackDraughts = new HashMap<>(boardClone.getBlackDraughts());
    Map<String, Draught> whiteDraughts = new HashMap<>(boardClone.getWhiteDraughts());
    List<Square> boardSquares = getAssignedSquares(rules.getDimension());
    for (Square square : boardSquares) {
      int v = square.getV(), h = square.getH();
      if (update) {
        Draught blackDraught = blackDraughts.get(square.getNotation());
        Draught whiteDraught = whiteDraughts.get(square.getNotation());
        if (blackDraught != null) {
          square.setDraught(blackDraught);
        } else if (whiteDraught != null) {
          square.setDraught(whiteDraught);
        }
      } else if (fillBoard) {
        if (v < rules.getRowsForDraughts()) {
          placeDraught(!black, rules, blackDraughts, square, v, h);
        } else if (v >= rules.getDimension() - rules.getRowsForDraughts() && v < rules.getDimension()) {
          placeDraught(black, rules, whiteDraughts, square, v, h);
        }
      }
    }

    boardClone.setBlackDraughts(blackDraughts);
    boardClone.setWhiteDraughts(whiteDraughts);

    boardClone.setAssignedSquares(boardSquares);
    updateMoveSquaresDimensionAndDiagonals(boardClone);

    List<Square> squares = getSquares(boardSquares, rules.getDimension());
    boardClone.setSquares(squares);
    return boardClone;
  }

  private static void placeDraught(boolean black, EnumRules rules, Map<String, Draught> draughts, Square square, int v, int h) {
    Draught draught = new Draught(v, h, rules.getDimension());
    draught.setBlack(black);
    draughts.put(square.getNotation(), draught);
    square.setDraught(draught);
  }

  static List<Square> getSquareArray(int offset, int dim, boolean main) {
    List<Square> squares = new ArrayList<>();
    for (int v = 0; v < dim; v++) {
      for (int h = 0; h < dim; h++) {
        if (((v + h + 1) % 2 == 0)
            && (main && (v - h + offset) == 0
            || !main && (v + h - offset) == dim - 1)) {
          Square square = new Square(v, h, dim, main);
          squares.add(square);
        }
      }
    }
    return squares;
  }

  static List<List<Square>> getDiagonals(int dim, boolean main) {
    List<List<Square>> diagonals = new ArrayList<>(dim - 2);
    for (int i = -dim; i < dim - 1; i++) {
      if ((i == 1 - dim) && main) {
        continue;
      }
      List<Square> diagonal = BoardUtils.getSquareArray(i, dim, main);
      if (!diagonal.isEmpty()) {
        diagonals.add(diagonal);
      }
    }
    return diagonals;
  }

  static boolean isSubDiagonal(List<Square> subDiagonal, List<Square> diagonal) {
    return subDiagonal.stream().allMatch(diagonal::contains);
  }

  /**
   * Assign square subdiagonal and main diagonal. Assign diagonal's squares link to squares
   */
  private static List<Square> getAssignedSquares(int dim) {
    List<List<Square>> mainDiagonals = getDiagonals(dim, true);
    List<List<Square>> subDiagonals = getDiagonals(dim, false);

    List<Square> squares = new ArrayList<>();
    for (List<Square> subDiagonal : subDiagonals) {
      for (Square subSquare : subDiagonal) {
        subSquare.addDiagonal(subDiagonal);
        squares.add(subSquare);
        subDiagonal.set(subDiagonal.indexOf(subSquare), subSquare);
        for (List<Square> mainDiagonal : mainDiagonals) {
          for (Square mainSquare : mainDiagonal) {
            if (subSquare.equals(mainSquare)) {
              subSquare.addDiagonal(mainDiagonal);
              mainDiagonal.set(mainDiagonal.indexOf(subSquare), subSquare);
            }
          }
        }
      }
    }

    return squares;
  }

  private static List<Square> getSquares(List<Square> diagonals, int dim) {
    List<Square> squares = new ArrayList<>();
    List<Square> collect = diagonals
        .stream()
        .sorted(Comparator.comparingInt(Square::getV))
        .collect(Collectors.toList());
    Iterator<Square> iterator = collect.iterator();
    for (int i = 0; i < dim; i++) {
      for (int j = 0; j < dim; j++) {
        if (isValidPlaceForSquare(i, j)) {
          squares.add(null);
        } else if (iterator.hasNext()) {
          squares.add(iterator.next());
        }
      }
    }
    return squares;
  }

  private static boolean isValidPlaceForSquare(int i, int j) {
    return (i + j) % 2 == 0;
  }

  /**
   * Find variable link to square from board
   */
  public static Square findSquareByLink(Square square, Board board) {
    if (square == null) {
      return null;
    }
    return findSquareByVH(board, square.getV(), square.getH());
  }

  static Square findSquareByVH(Board board, int v, int h) {
    for (Square square : board.getAssignedSquares()) {
      if (square.getH() == h && square.getV() == v) {
        return square;
      }
    }
    throw new BoardServiceException("Square not found");
  }

  public static Square findSquareByNotation(String notation, Board board) {
    if (StringUtils.isBlank(notation)) {
      throw new BoardServiceException("Invalid notation " + notation);
    }
    for (Square square : board.getAssignedSquares()) {
      if (square.getNotation().equals(notation)) {
        return square;
      }
    }
    throw new BoardServiceException("Square not found " + notation);
  }


  public static void addDraught(Board board, String notation, Draught draught) throws BoardServiceException {
    if (draught == null) {
      return;
    }
    addDraught(board, notation, draught.isBlack(), draught.isQueen(), draught.isCaptured());
  }

  private static void addDraught(Board board, String notation, boolean black, boolean queen, boolean remove) {
    Square square = findSquareByNotation(notation, board);
    Draught draught = null;
    if (!remove && !isOverloadDraughts(board, black)) {
      draught = new Draught(square.getV(), square.getH(), square.getDim(), black, queen);
      if (black) {
        board.getWhiteDraughts().remove(notation);
        board.addBlackDraughts(notation, draught);
      } else {
        board.getBlackDraughts().remove(notation);
        board.addWhiteDraughts(notation, draught);
      }
    } else {
      board.getBlackDraughts().remove(notation);
      board.getWhiteDraughts().remove(notation);
    }
    square.setDraught(draught);
  }

  private static boolean isOverloadDraughts(Board board, boolean black) {
    return black ? board.getBlackDraughts().size() >= board.getRules().getDraughtsCount()
        : board.getWhiteDraughts().size() >= board.getRules().getDraughtsCount();
  }

  private static void removeDraught(Board board, String notation, boolean black) {
    addDraught(board, notation, black, false, true);
  }

  public static Board moveDraught(Board board, List<Square> capturedSquares) {
    performMoveDraught(board, capturedSquares);
    Board newBoard = board.deepClone();
    boolean blackTurn = board.isBlackTurn();
    Square selectedSquareNew = newBoard.getSelectedSquare();
    MovesList nextMovesSquares = highlightedBoard(blackTurn, selectedSquareNew, newBoard);
    boolean previousCaptured = !capturedSquares.isEmpty();
    boolean nextCaptured = !nextMovesSquares.getCaptured().isEmpty();
    if (previousCaptured && nextCaptured) {
      updateNotationMiddle(newBoard);
      return newBoard;
    }
    updateNotationEnd(previousCaptured, board);
    resetBoardHighlight(board);
    return board;
  }

  private static void updateNotationMiddle(Board board) {
    NotationDrives notation = board.getNotationDrives();
    boolean blackTurn = board.isBlackTurn();
    int strokeCount = blackTurn ? board.getDriveCount() : board.getDriveCount() + 1;
    board.setDriveCount(strokeCount);
    String boardId = board.getId();
    NotationDrive notationDrive = getFirstNotationDrive(strokeCount, notation, boardId);
    // todo check on turn
    notationDrive.getMoves().add(getNotationMoveWithCapturedStrokes(board));
  }

  private static void updateNotationEnd(boolean previousCaptured, Board board) {
    boolean blackTurn = board.isBlackTurn();
    int driveCount = blackTurn ? board.getDriveCount() : board.getDriveCount() + 1;
    board.setDriveCount(driveCount);
    NotationDrives notation = board.getNotationDrives();
    if (previousCaptured) {
      String boardId = board.getId();
      NotationDrive notationDrive = getFirstNotationDrive(driveCount, notation, boardId);
      // todo check on turn
      NotationMove move = getNotationMoveWithCapturedStrokes(board);
      notationDrive.getMoves().add(move);
    } else {
      pushSimpleStrokeToNotation(driveCount, notation, board);
    }
    board.setBlackTurn(!blackTurn);
  }

  private static NotationMove getNotationMoveWithCapturedStrokes(Board board) {
    resetBoardNotationCursor(board.getNotationDrives());
    NotationDrive drive = board.getNotationDrives().getFirst();
    NotationMove move = drive.getMoves().getFirst().deepClone();
    if (move.getMove().length == 0) {
      move.setMove(new String[] {
          board.getPreviousSquare().getNotation(),
          board.getSelectedSquare().getNotation()
      });
    }
    move.setType(NotationDrive.EnumMoveType.CAPTURE);
    move.setCursor(true);
    return move;
  }

  private static void resetBoardNotationCursor(NotationDrives notationDrives) {
    notationDrives.forEach(drive -> {
      drive.getMoves().forEach(move -> move.setCursor(false));
    });
  }

  private static void pushSimpleStrokeToNotation(int moveNumber, NotationDrives notationDrives, Board board) {
    resetBoardNotationCursor(board.getNotationDrives());

    String[] move = new String[]{
        board.getPreviousSquare().getNotation(), board.getSelectedSquare().getNotation()
    };

    String boardId = board.getId();
    NotationMove notationMove = NotationMove.create(NotationDrive.EnumMoveType.SIMPLE, boardId, true);
    notationMove.setMove(move);

    NotationMoves moves = NotationMoves.Builder.getInstance()
        .add(notationMove)
        .build();

    NotationDrive notationDrive = NotationDrive.create(moves);
    notationDrive.setMoveNumber(moveNumber);
    notationDrive.setNotationNumberInt(moveNumber);
    notationDrives.add(notationDrive);
  }

  private static NotationDrive getFirstNotationDrive(int strokeCount, NotationDrives notationDrives, String boardId) {
    if (notationDrives.isEmpty()) {
      NotationMove firstMove = NotationMove.create(NotationDrive.EnumMoveType.SIMPLE, boardId, true);
      NotationMoves moves = NotationMoves.Builder.getInstance()
          .add(firstMove)
          .build();
      NotationDrive notationDrive = NotationDrive.create(moves);
      notationDrive.setNotationNumberInt(strokeCount);
//      notationDrives.push(notationDrive);
    } else {
      NotationDrive notationDrive = notationDrives.getFirst();
      if (notationDrive.getNotationNumberInt() != strokeCount && notationDrive.getMoves() != null) {
        NotationMove move = NotationMove.create(null, boardId, false);
        NotationMoves moves = NotationMoves.Builder.getInstance()
            .add(move)
            .build();
        NotationDrive drive = NotationDrive.create(moves);
        drive.setNotationNumberInt(strokeCount);
        notationDrives.push(drive);
      }
    }
    return notationDrives.getFirst();
  }

  private static void resetBoardHighlight(Board board) {
    board.getAssignedSquares()
        .forEach(square -> {
          square.setHighlighted(false);
          if (square.isOccupied() && square.getDraught().isCaptured()) {
            removeDraught(board, square.getNotation(), square.getDraught().isBlack());
          }
        });
  }

  /**
   * Highlight board and returns is next move allowed
   *
   * @return is next move allowed
   */
  public static MovesList highlightedBoard(boolean blackTurn, Square selectedSquare, Board board) {
    MovesList movesList = highlightedAssignedMoves(selectedSquare);
    List<Square> allowed = movesList.getAllowed();
    List<Square> captured = movesList.getCaptured();
    if (!captured.isEmpty()) {
      board.getAssignedSquares().forEach((Square square) -> {
        square.setHighlighted(false);
        if (square.isOccupied()) {
          square.getDraught().setMarkCaptured(false);
        }
        if (square.isOccupied() && square.equals(selectedSquare)) {
          square.getDraught().setHighlighted(true);
        }
        if (allowed.contains(square)) {
          square.setHighlighted(true);
        }
        if (captured.contains(square)) {
          square.getDraught().setMarkCaptured(true);
        }
      });
      return movesList;
    } else {
      Set<String> draughtsNotations;
      if (blackTurn) {
        draughtsNotations = board.getBlackDraughts().keySet();
      } else {
        draughtsNotations = board.getWhiteDraughts().keySet();
      }
      // find squares occupied by current user
      List<Square> draughtsSquares = board.getAssignedSquares()
          .stream()
          .filter(square -> !square.equals(selectedSquare))
          .filter(square -> draughtsNotations.contains(square.getAlphanumericNotation64()))
          .collect(Collectors.toList());
      // find all squares captured by current user
      List<Square> allCaptured = draughtsSquares
          .stream()
          .flatMap(square -> highlightedAssignedMoves(square).getCaptured().stream())
          .collect(Collectors.toList());
      // reset highlight
      board.getAssignedSquares()
          .stream()
          .filter(Square::isHighlighted)
          .forEach(square -> square.setHighlighted(false));
      if (allCaptured.isEmpty()) { // if there is no captured then highlight allowed
        board.getAssignedSquares()
            .stream()
            .peek((square -> {
              // highlight selected draught
              if (square.isOccupied() && square.equals(selectedSquare)) {
                square.getDraught().setHighlighted(true);
              }
            }))
            .filter(allowed::contains)
            .forEach(square -> square.setHighlighted(true));
      }
      return movesList;
    }
  }

  private static void performMoveDraught(Board board, List<Square> capturedSquares) {
    Square sourceSquare = board.getSelectedSquare();
    Square targetSquare = board.getNextSquare();
    if (!targetSquare.isHighlighted()
        || sourceSquare == null
        || !sourceSquare.isOccupied()) {
      throw new BoardServiceException(ErrorMessages.UNABLE_TO_MOVE);
    }
    markCapturedDraught(capturedSquares, board, sourceSquare, targetSquare);

    Draught draught = sourceSquare.getDraught();
    removeDraught(board, sourceSquare.getNotation(), draught.isBlack());

    draught.setV(targetSquare.getV());
    draught.setH(targetSquare.getH());

    checkQueen(board, draught);

    BoardUtils.addDraught(board, targetSquare.getNotation(), draught);
    targetSquare = BoardUtils.findSquareByNotation(targetSquare.getNotation(), board);
    targetSquare.setDraught(draught);

    board.setNextSquare(null);
    board.setPreviousSquare(board.getSelectedSquare());
    board.getPreviousSquare().setDraught(null);
    board.setSelectedSquare(targetSquare);

    replaceDraught(board.getWhiteDraughts(), targetSquare.getNotation(), sourceSquare.getNotation());
    replaceDraught(board.getBlackDraughts(), targetSquare.getNotation(), sourceSquare.getNotation());
  }

  private static void markCapturedDraught(List<Square> capturedSquares, Board board, Square sourceSquare, Square targetSquare) {
    if (!capturedSquares.isEmpty()) {
      List<Square> toBeatSquares = findCapturedSquare(sourceSquare, targetSquare);
      if (toBeatSquares.isEmpty()) {
        throw new BoardServiceException(ErrorMessages.UNABLE_TO_MOVE);
      }
      toBeatSquares.forEach(square -> findSquareByLink(square, board).getDraught().setCaptured(true));
    }
  }

  private static void checkQueen(Board board, Draught draught) {
    if (!draught.isQueen()) {
      if (draught.isBlack() && board.getRules().getDimension() == draught.getV() + 1) {
        draught.setQueen(true);
      } else if (!draught.isBlack() && draught.getV() == 0) {
        draught.setQueen(true);
      }
    }
  }

  private static List<Square> findCapturedSquare(Square sourceSquare, Square targetSquare) {
    boolean upDirection = isUpDirection(sourceSquare, targetSquare);
    List<Square> captured = new ArrayList<>();
    for (List<Square> diagonal : sourceSquare.getDiagonals()) {
      if (isSubDiagonal(Arrays.asList(sourceSquare, targetSquare), diagonal)) {
        ListIterator<Square> squareListIterator = diagonal.listIterator(diagonal.indexOf(sourceSquare));
        if (!upDirection) {
          while (squareListIterator.hasNext()) {
            Square next = squareListIterator.next();
            if (targetSquare.equals(next)) {
              break;
            }
            if (isValidToBeat(sourceSquare, next)) {
              captured.add(next);
            }
          }
        } else {
          while (squareListIterator.hasPrevious()) {
            Square next = squareListIterator.previous();
            if (targetSquare.equals(next)) {
              break;
            }
            if (isValidToBeat(sourceSquare, next)) {
              captured.add(next);
            }
          }
        }
      }
    }
    return captured;
  }

  private static boolean isValidToBeat(Square sourceSquare, Square next) {
    return next.isOccupied()
        && next.getDraught().isBlack() != sourceSquare.getDraught().isBlack();
  }

  private static boolean isUpDirection(Square source, Square target) {
    return target.getV() - source.getV() < 0;
  }

  /**
   * Move draught on whiteDraughts and blackDraughts lists
   */
  private static void replaceDraught(Map<String, Draught> draughts, String targetSquareNotation, String sourceSquareNotation) {
    Draught draughtFromSource = draughts.remove(sourceSquareNotation);
    if (draughtFromSource != null) {
      draughts.put(targetSquareNotation, draughtFromSource);
    }
  }

  public static void updateMoveSquaresHighlightAndDraught(Board currentBoard, Board origBoard) {
    Square selectedSquare = findSquareByLink(origBoard.getSelectedSquare(), currentBoard);
    if (selectedSquare != null) {
      currentBoard.setSelectedSquare(updateSquare(selectedSquare, origBoard.getSelectedSquare()));
    }
    Square nextSquare = findSquareByLink(origBoard.getNextSquare(), currentBoard);
    if (nextSquare != null) {
      currentBoard.setNextSquare(updateSquare(nextSquare, origBoard.getNextSquare()));
    }
    Square previousSquare = findSquareByLink(origBoard.getPreviousSquare(), currentBoard);
    if (previousSquare != null) {
      currentBoard.setPreviousSquare(updateSquare(previousSquare, origBoard.getPreviousSquare()));
    }
    updateMoveDraughtsNotation(selectedSquare);
    updateMoveDraughtsNotation(nextSquare);
    updateMoveDraughtsNotation(previousSquare);
  }

  private static Square updateSquare(Square selectedSquare, Square origSquare) {
    return selectedSquare.highlight(origSquare.isHighlighted()).draught(origSquare.getDraught());
  }

  private static void updateMoveDraughtsNotation(Square square) {
    if (square != null && square.isOccupied()) {
      square.getDraught().setDim(square.getDim());
    }
  }

  private static void updateMoveSquaresDimensionAndDiagonals(Board currentBoard) {
    currentBoard.setSelectedSquare(updateSquareDimension(currentBoard.getSelectedSquare(), currentBoard));
    currentBoard.setNextSquare(updateSquareDimension(currentBoard.getNextSquare(), currentBoard));
    currentBoard.setPreviousSquare(updateSquareDimension(currentBoard.getPreviousSquare(), currentBoard));
  }

  private static Square updateSquareDimension(Square square, Board board) {
    if (square != null) {
      return findSquareByLink(square, board);
    }
    return null;
  }

  public static String printBoardNotation(NotationDrives notationDrives) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(notationDrives);
    } catch (JsonProcessingException e) {
      return "";
    }
  }

  public static NotationDrives reverseBoardNotation(NotationDrives notationDrives) {
    Collections.reverse(notationDrives);
    return notationDrives;
  }

//  public static void assignBoardNotationCursor(NotationDrives notationDrives, String boardId) {
//    resetBoardNotationCursor(notationDrives);
//    setCursorForAtomStroke(notationDrives, boardId, NotationDrive::getFirst);
//    setCursorForAtomStroke(notationDrives, boardId, NotationDrive::getSecond);
//  }

  private static void setCursorForAtomStroke(NotationDrives notationDrives, String boardId, Function<NotationDrive, NotationMove> predicate) {
    notationDrives
        .stream()
        .map(predicate)
        .filter(Objects::nonNull)
        .filter(atomStroke -> atomStroke.getBoardId().equals(boardId))
        .findFirst()
        .ifPresent(notationAtomStroke -> notationAtomStroke.setCursor(true));
  }
}
