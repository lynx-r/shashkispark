package com.workingbit.board.controller.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.domain.ICoordinates;
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
        Draught blackDraught = blackDraughts.get(square.getPdnNotationNumeric64());
        Draught whiteDraught = whiteDraughts.get(square.getPdnNotationNumeric64());
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
    draughts.put(square.getPdnNotationNumeric64(), draught);
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
      throw new BoardServiceException("Invalid notation");
    }
    for (Square square : board.getAssignedSquares()) {
      if (ICoordinates.toAlphanumericNotation64(notation).equals(square.getAlphanumericNotation64())) {
        return square;
      }
    }
    throw new BoardServiceException("Square not found");
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
    LinkedList<NotationStroke> notation = board.getNotationStrokes();
    boolean blackTurn = board.isBlackTurn();
    int strokeCount = blackTurn ? board.getStrokeCount() : board.getStrokeCount() + 1;
//    board.setStrokeCount(strokeCount);
    String boardId = board.getId();
    NotationStroke notationStroke = getFirstNotationStroke(strokeCount, notation, boardId);
    if (board.isBlackTurn()) {
      notationStroke.setSecond(getNotationAtomWithCapturedStrokes(board));
    } else {
      notationStroke.setFirst(getNotationAtomWithCapturedStrokes(board));
    }
  }

  private static void updateNotationEnd(boolean previousCaptured, Board board) {
    boolean blackTurn = board.isBlackTurn();
    int strokeCount = blackTurn ? board.getStrokeCount() : board.getStrokeCount() + 1;
    board.setStrokeCount(strokeCount);
    NotationStrokes notation = board.getNotationStrokes();
    if (previousCaptured) {
      String boardId = board.getId();
      NotationStroke notationStroke = getFirstNotationStroke(strokeCount, notation, boardId);
      if (board.isBlackTurn()) {
        NotationAtomStroke second = getNotationAtomWithCapturedStrokes(board);
        notationStroke.setSecond(second);
      } else {
        NotationAtomStroke first = getNotationAtomWithCapturedStrokes(board);
        notationStroke.setFirst(first);
      }
    } else {
      pushSimpleStrokeToNotation(strokeCount, notation, board);
    }
    board.setBlackTurn(!blackTurn);
  }

  private static NotationAtomStroke getNotationAtomWithCapturedStrokes(Board board) {
    resetBoardNotationCursor(board.getNotationStrokes());
    NotationStroke firstStroke = board.getNotationStrokes().getFirst();
    NotationAtomStroke atomStroke;
    if (!board.isBlackTurn()) {
      atomStroke = firstStroke.getFirst().deepClone();
    } else {
      atomStroke = firstStroke.getSecond().deepClone();
    }
    List<String> strokes = atomStroke.getStrokes();
    if (strokes.isEmpty()) {
      strokes.addAll(Arrays.asList(board.getPreviousSquare().getPdnNotationNumeric64(),
          board.getSelectedSquare().getPdnNotationNumeric64()));
    } else {
      strokes.add(board.getSelectedSquare().getPdnNotationNumeric64());
    }
    atomStroke.setType(NotationStroke.EnumStrokeType.CAPTURE);
    atomStroke.setCursor(true);
    return atomStroke;
  }

  private static void resetBoardNotationCursor(NotationStrokes notationStrokes) {
    notationStrokes.forEach(notationStroke -> {
      if (notationStroke.getFirst() != null) {
        notationStroke.getFirst().setCursor(false);
      }
      if (notationStroke.getSecond() != null) {
        notationStroke.getSecond().setCursor(false);
      }
    });
  }

  private static void pushSimpleStrokeToNotation(int strokeNumber, NotationStrokes notation, Board board) {
    List<String> stroke = new ArrayList<>(Arrays.asList(board.getPreviousSquare().getPdnNotationNumeric64(), board.getSelectedSquare().getPdnNotationNumeric64()));
    resetBoardNotationCursor(board.getNotationStrokes());
    String boardId = board.getId();
    NotationStroke notationStroke = getFirstNotationStroke(strokeNumber, notation, boardId);
    NotationAtomStroke notationAtomStroke = NotationAtomStroke.create(NotationStroke.EnumStrokeType.SIMPLE, stroke, boardId, true);
    if (board.isBlackTurn()) {
      notationStroke.setSecond(notationAtomStroke);
    } else {
      notationStroke.setFirst(notationAtomStroke);
    }
  }

  private static NotationStroke getFirstNotationStroke(int strokeCount, LinkedList<NotationStroke> notationStrokes, String boardId) {
    if (notationStrokes.isEmpty()) {
      NotationAtomStroke atomStroke =
          NotationAtomStroke.create(NotationStroke.EnumStrokeType.SIMPLE, new ArrayList<>(), boardId, true);
      NotationStroke notationStroke = new NotationStroke(atomStroke, null);
      notationStroke.setMoveNumberInt(strokeCount);
      notationStrokes.push(notationStroke);
    } else {
      NotationStroke notationStroke = notationStrokes.getFirst();
      if (notationStroke.getMoveNumberInt() != strokeCount && notationStroke.getSecond() != null) {
        NotationAtomStroke atomStroke = new NotationAtomStroke();
        atomStroke.setBoardId(boardId);
        NotationStroke stroke = new NotationStroke(atomStroke, null);
        stroke.setMoveNumberInt(strokeCount);
        notationStrokes.push(stroke);
      }
    }
    return notationStrokes.getFirst();
  }

  private static void resetBoardHighlight(Board board) {
    board.getAssignedSquares()
        .forEach(square -> {
          square.setHighlighted(false);
          if (square.isOccupied() && square.getDraught().isCaptured()) {
            removeDraught(board, square.getPdnNotationNumeric64(), square.getDraught().isBlack());
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
          .filter(square -> draughtsNotations.contains(square.getPdnNotationNumeric64()))
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
    removeDraught(board, sourceSquare.getPdnNotationNumeric64(), draught.isBlack());

    draught.setV(targetSquare.getV());
    draught.setH(targetSquare.getH());

    checkQueen(board, draught);

    BoardUtils.addDraught(board, targetSquare.getPdnNotationNumeric64(), draught);
    targetSquare = BoardUtils.findSquareByNotation(targetSquare.getPdnNotationNumeric64(), board);
    targetSquare.setDraught(draught);

    board.setNextSquare(null);
    board.setPreviousSquare(board.getSelectedSquare());
    board.getPreviousSquare().setDraught(null);
    board.setSelectedSquare(targetSquare);

    replaceDraught(board.getWhiteDraughts(), targetSquare.getPdnNotationNumeric64(), sourceSquare.getPdnNotationNumeric64());
    replaceDraught(board.getBlackDraughts(), targetSquare.getPdnNotationNumeric64(), sourceSquare.getPdnNotationNumeric64());
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

  public static String printBoardNotation(NotationStrokes notationStrokes) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(notationStrokes);
    } catch (JsonProcessingException e) {
      return "";
    }
  }

  public static NotationStrokes reverseBoardNotation(NotationStrokes notationStrokes) {
    Collections.reverse(notationStrokes);
    return notationStrokes;
  }

  public static void assignBoardNotationCursor(NotationStrokes notationStrokes, String boardId) {
    resetBoardNotationCursor(notationStrokes);
    setCursorForAtomStroke(notationStrokes, boardId, NotationStroke::getFirst);
    setCursorForAtomStroke(notationStrokes, boardId, NotationStroke::getSecond);
  }

  private static void setCursorForAtomStroke(NotationStrokes notationStrokes, String boardId, Function<NotationStroke, NotationAtomStroke> predicate) {
    notationStrokes
        .stream()
        .map(predicate)
        .filter(Objects::nonNull)
        .filter(atomStroke -> atomStroke.getBoardId().equals(boardId))
        .findFirst()
        .ifPresent(notationAtomStroke -> notationAtomStroke.setCursor(true));
  }
}
