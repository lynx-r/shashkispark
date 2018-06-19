package com.workingbit.board.controller.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.domain.impl.*;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumNotation;
import com.workingbit.share.model.enumarable.EnumNotationFormat;
import com.workingbit.share.model.enumarable.EnumRules;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.workingbit.board.controller.util.HighlightMoveUtil.getHighlightedAssignedMoves;


/**
 * Created by Aleksey Popryaduhin on 20:56 11/08/2017.
 */
public class BoardUtils {

  public static final String SERVER_BOARD = "serverBoard";
  public static final String MOVES_LIST = "movesList";

  private BoardUtils() {
  }

  /**
   * Fill board with draughts
   *
   * @param black is player plays black?
   */
  public static Board initBoard(boolean fillBoard, boolean black, EnumRules rules) {
    Board board = new Board(black, rules);
    return updateBoard(fillBoard, false, board);
  }

  public static Board updateBoard(@NotNull Board board) {
    return updateBoard(false, true, board);
  }

  private static Board updateBoard(boolean fillBoard, boolean update, Board board) {
    Board boardClone = board.deepClone();
    EnumRules rules = boardClone.getRules();
    boolean black = boardClone.isBlack();

    Map<String, Draught> blackDraughts = new HashMap<>(boardClone.getBlackDraughts());
    Map<String, Draught> whiteDraughts = new HashMap<>(boardClone.getWhiteDraughts());
    List<Square> boardSquares = getAssignedSquares(rules.getDimension(), black);
    for (Square square : boardSquares) {
      int v = square.getV();
      int h = square.getH();
      if (update) {
        Draught blackDraught = blackDraughts.get(square.getNotation());
        Draught whiteDraught = whiteDraughts.get(square.getNotation());
        if (blackDraught != null) {
          square.setDraught(blackDraught);
        } else if (whiteDraught != null) {
          square.setDraught(whiteDraught);
        }
        square.setDim(rules.getDimension());
      } else if (fillBoard) {
        if (v < rules.getNumRowsForDraughts()) {
          placeDraught(!black, rules, blackDraughts, square, v, h);
        } else if (v >= rules.getDimension() - rules.getNumRowsForDraughts() && v < rules.getDimension()) {
          placeDraught(black, rules, whiteDraughts, square, v, h);
        }
      }
    }

    boardClone.setBlackDraughts(blackDraughts);
    boardClone.setWhiteDraughts(whiteDraughts);

    boardClone.setAssignedSquares(boardSquares);
    updateMoveSquaresHighlightAndDraught(boardClone, board);

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

  @NotNull
  static List<Square> getSquareArray(int offset, int dim, boolean main, boolean black) {
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

  @NotNull
  static List<List<Square>> getDiagonals(int dim, boolean main, boolean black) {
    List<List<Square>> diagonals = new ArrayList<>(dim - 2);
    for (int i = -dim; i < dim - 1; i++) {
      if ((i == 1 - dim) && main) {
        continue;
      }
      List<Square> diagonal = BoardUtils.getSquareArray(i, dim, main, black);
      if (!diagonal.isEmpty()) {
        diagonals.add(diagonal);
      }
    }
    return diagonals;
  }

  static boolean isSubDiagonal(@NotNull List<Square> subDiagonal, List<Square> diagonal) {
    return diagonal.containsAll(subDiagonal);
  }

  /**
   * Assign square subdiagonal and main diagonal. Assign diagonal's squares link to squares
   */
  @NotNull
  private static List<Square> getAssignedSquares(int dim, boolean black) {
    List<List<Square>> mainDiagonals = getDiagonals(dim, true, black);
    List<List<Square>> subDiagonals = getDiagonals(dim, false, black);

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

  @NotNull
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
  @Nullable
  public static Square findSquareByLink(@Nullable Square square, @NotNull Board board) {
    if (square == null) {
      return null;
    }
    return findSquareByVH(board, square.getV(), square.getH());
  }

  @NotNull
  static Square findSquareByVH(Board board, int v, int h) {
    for (Square square : board.getAssignedSquares()) {
      if (square.getH() == h && square.getV() == v) {
        return square;
      }
    }
    throw new BoardServiceException("Square not found");
  }

  @NotNull
  public static Square findSquareByNotation(String notation, @NotNull Board board) {
    if (StringUtils.isBlank(notation)) {
      throw new BoardServiceException("Invalid notation " + notation);
    }
    for (Square square : board.getAssignedSquares()) {
      if (square.getNotation().equals(notation) || square.getNotationNum().equals(notation)) {
        return square;
      }
    }
    throw new BoardServiceException("Square not found " + notation);
  }

  public static Square findSquareByNotationWithHint(String notation, List<String> moves, Board board, EnumNotationFormat notationFormat) {
    if (StringUtils.isBlank(notation)) {
      throw new BoardServiceException("Invalid notation " + notation);
    }
    switch (notationFormat) {
      case ALPHANUMERIC:
      case DIGITAL:
        return findSquareByNotation(notation, board);
      case SHORT:
        return findSquareByShortNotation(moves, board);
      default:
        throw new BoardServiceException("Square not found " + notation);
    }
  }

  private static Square findSquareByShortNotation(List<String> moves, Board board) {
    Square nextOld = board.getNextSquare();
    Square lastSquare = null;
    List<Square> passed = new ArrayList<>();
    for (int i = moves.size() - 1; i >= 0; i--) {
      String move = moves.get(i);
      if (i == moves.size() - 1) {
        lastSquare = findSquareByNotation(move, board);
      } else {
        boolean blackTurn = board.isBlackTurn();
        List<Square> first = lastSquare.getDiagonals()
            .stream()
            .filter(squares -> {
              int size = passed.size();
              return (size >= 2 && !isSubDiagonal(List.of(passed.get(size - 1), passed.get(size - 2)), squares) || size < 2);
            })
            .flatMap(Collection::stream)
            .filter(square -> {
              if (square.isOccupied()) {
                if (square.getDraught().isBlack() == blackTurn) {
                  String n = square.getNotation().replaceAll("\\d+", "");
                  return n.equals(move);
                }
              } else {
                String n = square.getNotation().replaceAll("\\d+", "");
                return n.equals(move);
              }
              return false;
            })
            .collect(Collectors.toList());
        Square found;
        if (first.size() > 1) {
          found = first.stream().filter(Square::isOccupied).findFirst().orElseThrow();
        } else {
          found = first.get(0);
        }
        String lastMove = found.getNotation();
        for (Square square : board.getAssignedSquares()) {
          if (square.getNotation().equals(lastMove)) {
            lastSquare = square;
            break;
          }
        }
      }
      passed.add(lastSquare);
    }
    board.setNextSquare(nextOld);
    return lastSquare;
  }

  public static void addDraught(@NotNull Board board, String notation, @Nullable Draught draught) throws BoardServiceException {
    if (draught == null) {
      return;
    }
    addDraught(board, notation, draught.isBlack(), draught.isQueen(), draught.isCaptured());
  }

  private static void addDraught(@NotNull Board board, String notation, boolean black, boolean queen, boolean remove) {
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

  private static boolean isOverloadDraughts(@NotNull Board board, boolean black) {
    return black ? board.getBlackDraughts().size() >= board.getRules().getDraughtsCount()
        : board.getWhiteDraughts().size() >= board.getRules().getDraughtsCount();
  }

  private static void removeDraught(@NotNull Board board, String notation, boolean black) {
    addDraught(board, notation, black, false, true);
  }

  public static void updateNotationMiddle(Board board, DomainId prevBoardId, @NotNull NotationHistory notationHistory) {
    String previousNotation = board.getPreviousSquare().getNotation();
    boolean isContinueCapture = isContinueCapture(notationHistory, previousNotation);
    NotationMove move;
    if (isContinueCapture) {
      move = notationHistory.getLastMove().orElseThrow(BoardServiceException::new);
    } else {
      move = NotationMove.create(EnumNotation.CAPTURE);
      Square selectedSquare = board.getSelectedSquare();
      String currentNotation = selectedSquare.getNotation();
      DomainId currentBoardId = board.getDomainId();
      move.addMove(previousNotation, currentNotation, currentBoardId);
    }

    // using this var in Place mode
    if (isContinueCapture) {
      String currentNotation = board.getSelectedSquare().getNotation();
      NotationMove lastMove = notationHistory.getLastMove().orElseThrow(BoardServiceException::new);
      DomainId currentBoardId = board.getDomainId();
      lastMove.getMove().add(new NotationSimpleMove(currentNotation));
    } else {
      if (!isFirstMove(board)) {
        int notationNumber = notationHistory.getLast().getNotationNumberInt() + 1;
        NotationMoves moves = new NotationMoves();
        moves.add(move);
        NotationDrive lastNotationDrive = NotationDrive.create(moves);
        lastNotationDrive.setNotationNumberInt(notationNumber);
        notationHistory.add(lastNotationDrive);
      } else {
        NotationDrive lastNotationDrive = notationHistory.getLast();
        lastNotationDrive.getMoves().add(move);
      }
    }
    notationHistory.setLastSelected(true);
    notationHistory.setLastMoveCursor();
  }

  public static void updateNotationEnd(Board board, @NotNull NotationHistory notationHistory, boolean previousCaptured) {
    Utils.setRandomIdAndCreatedAt(board);
    boolean blackTurn = board.isBlackTurn();
    int notationNumber = 0;
    if (!isFirstMove(board)) { // white move
      notationNumber = board.getDriveCount() + 1;
      board.setDriveCount(notationNumber);
    }

    if (previousCaptured) {
      pushCaptureMove(board, notationNumber, notationHistory);
    } else {
      pushSimpleMove(board, notationHistory, notationNumber);
    }
    notationHistory.syncMoves();
    board.setBlackTurn(!blackTurn);
    notationHistory.setLastSelected(true);
  }

  private static void pushCaptureMove(Board board, int notationNumber, @NotNull NotationHistory notationHistory) {
    NotationDrive notationDrive;
    String previousNotation = board.getPreviousSquare().getNotation();
    boolean firstMove = isFirstMove(board);
    boolean isContinueCapture = isContinueCapture(notationHistory, previousNotation);
    if (firstMove) {
      notationDrive = notationHistory.getLast();
    } else {
      if (!isContinueCapture) {
        notationDrive = new NotationDrive();
        NotationDrive.copyMetaOf(notationHistory.getLast(), notationDrive);
        notationDrive.setNotationNumberInt(notationNumber);
        notationHistory.add(notationDrive);
        notationDrive = notationHistory.getLast();
      } else {
        notationDrive = notationHistory.getLast();
      }
    }
    NotationMoves moves = notationDrive.getMoves();
    NotationMove lastCapturedMove;
    if (!isContinueCapture && firstMove) {
      lastCapturedMove = NotationMove.create(EnumNotation.CAPTURE);
      // take previous square notation
      // and push it in selected moves
      lastCapturedMove.getMove().add(new NotationSimpleMove(previousNotation));
      LinkedList<NotationSimpleMove> lastMove = lastCapturedMove.getMove();
      // take current notation
      String currentNotation = board.getSelectedSquare().getNotation();
      // and push it in selected moves
      lastMove.add(new NotationSimpleMove(currentNotation));
      lastCapturedMove.setMove(lastMove);
      // push selected moves in selected drive
      moves.add(lastCapturedMove);
    } else {
      if (moves.isEmpty()) {
        lastCapturedMove = NotationMove.create(EnumNotation.CAPTURE);
        // push it in selected moves
        lastCapturedMove.getMove().add(new NotationSimpleMove(previousNotation));
        moves.add(lastCapturedMove);
      }
      lastCapturedMove = moves.getLast();
      // take selected move
      LinkedList<NotationSimpleMove> lastMove = lastCapturedMove.getMove();
      String currentNotation = board.getSelectedSquare().getNotation();
      // push selected move to selected drive
      lastMove.add(new NotationSimpleMove(currentNotation));
    }
    notationHistory.getLastMove().ifPresent(notationMove -> notationMove.setBoardId(board.getDomainId()));

    board.getAssignedSquares()
        .forEach(square -> {
          if (square.isOccupied() && square.getDraught().isCaptured()) {
            removeDraught(board, square.getNotation(), square.getDraught().isBlack());
          }
        });
  }

  private static boolean isContinueCapture(NotationHistory notationDrives, String previousNotation) {
    NotationMoves moves = notationDrives.getLast()
        .getMoves();
    if (moves.isEmpty()) {
      return false;
    }
    LinkedList<NotationSimpleMove> move = moves.getLast()
        .getMove();
    if (move.isEmpty()) {
      return false;
    }
    return move.getLast()
        .getNotation().equals(previousNotation);
  }

  private static void pushSimpleMove(Board board, NotationHistory notationHistory, int notationNumber) {
    NotationMoves moves = new NotationMoves();
    NotationMove notationMove = NotationMove.create(EnumNotation.SIMPLE);

    String previousNotation = board.getPreviousSquare().getNotation();
    String currentNotation = board.getSelectedSquare().getNotation();
    DomainId currentBoardId = board.getDomainId();
    notationMove.addMove(previousNotation, currentNotation, currentBoardId);
    notationMove.getLastMove().ifPresent(nm -> nm.setCursor(true));
    moves.add(notationMove);

    boolean isWhiteTurn = notationNumber != 0;
    if (isWhiteTurn) {
      NotationDrive notationDrive = NotationDrive.create(moves);
      notationDrive.setNotationNumberInt(notationNumber);
      notationHistory.add(notationDrive);
    } else {
      notationHistory.getLast().getMoves().addAll(moves);
    }
  }

  public static void resetBoardHighlight(Board board) {
    board.getAssignedSquares()
        .replaceAll(square -> {
          square.setHighlight(false);
          return square;
        });
  }

  public static void resetCaptured(Board board) {
    board.getAssignedSquares()
        .replaceAll(square -> {
          if (!square.isOccupied()) {
            return square;
          }
          square.getDraught().setCaptured(false);
          square.getDraught().setMarkCaptured(0);
          return square;
        });
  }

  /**
   * Highlight board and returns is next move allowed
   *
   * @return is next move allowed
   */
  @NotNull
  public static MovesList getHighlightedBoard(boolean blackTurn, Board board) {
    MovesList movesList = getHighlightedAssignedMoves(board.getSelectedSquare());
    Set<Square> allowed = movesList.getAllowed();
    TreeSquare captured = movesList.getCaptured();
    boolean isCapturedFound = !captured.isEmpty();
    if (isCapturedFound) {
      highlightCaptured(board, allowed, captured.flatTree());
      return movesList;
    } else {
      return getHighlightSimple(board, movesList, allowed, blackTurn);
    }
  }

  @NotNull
  private static MovesList getHighlightSimple(@NotNull Board board, @NotNull MovesList movesList, @NotNull Set<Square> allowed, boolean blackTurn) {
    Set<String> draughtsNotations;
    if (blackTurn) {
      draughtsNotations = board.getBlackDraughts().keySet();
    } else {
      draughtsNotations = board.getWhiteDraughts().keySet();
    }
    // find squares occupied by current user
    List<Square> draughtsSquares = board.getAssignedSquares()
        .stream()
        .filter(square -> !square.equals(board.getSelectedSquare()))
        .filter(square -> draughtsNotations.contains(square.getNotation()))
        .filter(Square::isOccupied)
        .collect(Collectors.toList());
    // find all squares captured by current user
    TreeSquare allCaptured = new TreeSquare();
    draughtsSquares
        .forEach(square -> allCaptured.addTree(getHighlightedAssignedMoves(square).getCaptured()));
    // reset highlight
    board.getAssignedSquares()
        .stream()
        .filter(Square::isHighlight)
        .forEach(square -> square.setHighlight(false));
    if (allCaptured.isEmpty()) { // if there is no captured then highlight allowed
      board.getAssignedSquares()
          .stream()
          .peek((square -> {
            // highlight selected draught
            if (square.isOccupied() && square.equals(board.getSelectedSquare())) {
              square.getDraught().setHighlight(true);
            }
          }))
          .filter(allowed::contains)
          .forEach(square -> square.setHighlight(true));
    } else {
      movesList.setCaptured(allCaptured);
    }
    return movesList;
  }

  private static void highlightCaptured(Board board, @NotNull Set<Square> allowed, List<Square> captured) {
    board.getAssignedSquares().replaceAll((Square square) -> {
      square.setHighlight(false);
      if (square.isOccupied() && square.equals(board.getSelectedSquare())) {
        square.getDraught().setHighlight(true);
      }
      if (square.isOccupied() && captured.contains(square)) {
        Square squareWithCaptured = captured.get(captured.indexOf(square));
        square.setDraught(squareWithCaptured.getDraught());
      }
      if (allowed.contains(square)) {
        square.setHighlight(true);
      }
      return square;
    });
  }

  public static void performMoveDraught(Board board, @NotNull TreeSquare capturedSquares) {
    Square sourceSquare = board.getSelectedSquare();
    Square targetSquare = board.getNextSquare();
    if (!targetSquare.isHighlight()
        || sourceSquare == null
        || !sourceSquare.isOccupied()) {
      throw new BoardServiceException(ErrorMessages.UNABLE_TO_MOVE);
    }
    markCapturedDraught(capturedSquares, board, sourceSquare, targetSquare);

    Draught draught = sourceSquare.getDraught();
    removeDraught(board, sourceSquare.getNotation(), draught.isBlack());

    draught.setV(targetSquare.getV());
    draught.setH(targetSquare.getH());

    EnumRules rules = board.getRules();
    checkQueen(rules, draught);

    BoardUtils.addDraught(board, targetSquare.getNotation(), draught);
    targetSquare = BoardUtils.findSquareByNotation(targetSquare.getNotation(), board);
    targetSquare.setDraught(draught);

    board.setNextSquare(null);
    board.setPreviousSquare(board.getSelectedSquare());
    board.getPreviousSquare().setDraught(null);
    board.setSelectedSquare(targetSquare.deepClone());
  }

  private static void markCapturedDraught(TreeSquare capturedSquares, @NotNull Board board,
                                          @NotNull Square sourceSquare, @NotNull Square targetSquare) {
    if (!capturedSquares.isEmpty()) {
      Set<Square> toBeatSquares = findCapturedSquare(sourceSquare, targetSquare);
      if (toBeatSquares.size() != 1) {
        throw new BoardServiceException(ErrorMessages.UNABLE_TO_MOVE);
      }
      Square beaten = toBeatSquares.toArray(new Square[0])[0];
      Square squareByLink = findSquareByLink(beaten, board);
      squareByLink.getDraught().setCaptured(true);
      squareByLink.getDraught().setMarkCaptured(0);
      String beatenKey = beaten.getNotation();
      if (board.getBlackDraughts().containsKey(beatenKey)) {
        board.addBlackDraughts(beatenKey, beaten.getDraught());
      }
      if (board.getWhiteDraughts().containsKey(beatenKey)) {
        board.addWhiteDraughts(beatenKey, beaten.getDraught());
      }
    }
  }

  private static void checkQueen(@NotNull EnumRules rules, Draught draught) {
    if (!draught.isQueen()
        && draught.isBlack() && rules.getDimension() == draught.getV() + 1
        || !draught.isBlack() && draught.getV() == 0) {
      draught.setQueen(true);
    }
  }

  /**
   * TODO refactor
   *
   * @param sourceSquare
   * @param targetSquare
   * @return
   */
  @NotNull
  private static Set<Square> findCapturedSquare(@NotNull Square sourceSquare, @NotNull Square targetSquare) {
    boolean upDirection = isUpDirection(sourceSquare, targetSquare);
    Set<Square> captured = new HashSet<>();
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

  private static boolean isValidToBeat(@NotNull Square sourceSquare, Square next) {
    return next.isOccupied()
        && next.getDraught().isBlack() != sourceSquare.getDraught().isBlack();
  }

  private static boolean isUpDirection(Square source, Square target) {
    return target.getV() - source.getV() < 0;
  }

  public static void updateMoveSquaresHighlightAndDraught(Board currentBoard, Board origBoard) {
    int dim = currentBoard.getRules().getDimension();
    Square selectedSquare = findSquareByLink(origBoard.getSelectedSquare(), currentBoard);
    if (selectedSquare != null) {
      selectedSquare.setDim(dim);
      if (selectedSquare.getDraught() != null) {
        selectedSquare.getDraught().setDim(dim);
      }
      currentBoard.setSelectedSquare(selectedSquare);
    }
    Square origNextSquare = origBoard.getNextSquare();
    Square nextSquare = findSquareByLink(origNextSquare, currentBoard);
    if (nextSquare != null) {
      nextSquare.setDim(dim);
      // нужно для эмуляции
      if (origNextSquare.isHighlight()) {
        nextSquare.setHighlight(origNextSquare.isHighlight());
      }
      currentBoard.setNextSquare(nextSquare);
    }
    Square previousSquare = findSquareByLink(origBoard.getPreviousSquare(), currentBoard);
    if (previousSquare != null) {
      previousSquare.setDim(dim);
      if (previousSquare.getDraught() != null) {
        previousSquare.getDraught().setDim(dim);
      }
      currentBoard.setPreviousSquare(previousSquare);
    }
  }

  static String printBoardNotation(NotationHistory notationDrives) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(notationDrives);
    } catch (JsonProcessingException e) {
      return "";
    }
  }

  private static boolean isFirstMove(Board board) {
    return board.isBlackTurn() && !board.isBlack() || !board.isBlackTurn() && board.isBlack();
  }

  public static Square getPredictedSelectedSquare(Board board) {
    Square nextSquare;
    nextSquare = board.getNextSquare();
    boolean black = board.isBlackTurn();
    List<Square> found = new ArrayList<>();
    for (List<Square> diagonals : nextSquare.getDiagonals()) {
      Square selected = findSmartOnDiagonal(nextSquare, diagonals, black, false);
      if (selected != null) {
        found.add(selected);
      }
      selected = findSmartOnDiagonal(nextSquare, diagonals, black, true);
      if (selected != null) {
        found.add(selected);
      }
    }
    if (found.size() != 1) {
      Board serverBoard = board.deepClone();
      List<Square> allowed = new ArrayList<>();
      Set<Square> captured = new HashSet<>();
      for (Square square : found) {
        serverBoard.setSelectedSquare(square);
        Map highlight = getSimpleHighlight(serverBoard, board);
        MovesList movesList = (MovesList) highlight.get("movesList");
        List<Square> moveCaptured = movesList.getCaptured().flatTree();
        if (!movesList.getAllowed().isEmpty() && !captured.containsAll(moveCaptured)) {
          captured.addAll(moveCaptured);
          allowed.add(square);
        }
      }
      if (allowed.size() != 1) {
        throw RequestException.badRequest(ErrorMessages.UNABLE_TO_MOVE);
      }
      return allowed.get(0);
    }
    return found.get(0);
  }

  private static Square findSmartOnDiagonal(Square nextSquare, List<Square> diagonals, boolean black, boolean down) {
    int nextI = diagonals.indexOf(nextSquare);
    int tries = 0;
    int i = nextI;
    while (down ? i >= 0 : i < diagonals.size()) {
      Square square = diagonals.get(i);
      if (square.isOccupied() && square.getDraught().isBlack() == black) {
        // fixme
        if (!square.getDraught().isQueen() && tries > 1) {
          if (down) {
            i--;
          } else {
            i++;
          }
          continue;
        }
        return square;
      }
      if (down) {
        i--;
      } else {
        i++;
      }
      if (!square.isOccupied()) {
        tries++;
      }
    }
    return null;
  }

  @NotNull
  private static Map getSimpleHighlight(@NotNull Board serverBoard, @NotNull Board clientBoard) {
    BoardUtils.updateMoveSquaresHighlightAndDraught(serverBoard, clientBoard);
    Square selectedSquare = serverBoard.getSelectedSquare();
    if (isInvalidHighlight(selectedSquare)) {
      throw new BoardServiceException(ErrorMessages.INVALID_HIGHLIGHT);
    }
    MovesList movesList = getHighlightedAssignedMoves(serverBoard.getSelectedSquare());
    return Map.of(SERVER_BOARD, serverBoard, MOVES_LIST, movesList);
  }

  public static boolean isInvalidHighlight(@Nullable Square selectedSquare) {
    return selectedSquare == null
        || !selectedSquare.isOccupied();
  }
}
