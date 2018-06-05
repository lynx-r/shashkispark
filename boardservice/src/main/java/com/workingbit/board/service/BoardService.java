package com.workingbit.board.service;

import com.workingbit.board.controller.util.BoardUtils;
import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.domain.ICoordinates;
import com.workingbit.share.domain.impl.*;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumRules;
import com.workingbit.share.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.workingbit.board.BoardEmbedded.boardDao;
import static com.workingbit.board.controller.util.BoardUtils.*;
import static com.workingbit.board.controller.util.HighlightMoveUtil.getHighlightedAssignedMoves;
import static java.lang.String.format;

/**
 * Created by Aleksey Popryaduhin on 13:45 09/08/2017.
 */
public class BoardService {

  private Logger logger = LoggerFactory.getLogger(BoardService.class);
  private static final String SERVER_BOARD = "serverBoard";
  private static final String MOVES_LIST = "movesList";

  Board createBoard(@NotNull CreateBoardPayload newBoardRequest) {
    Board board = initBoard(newBoardRequest.isFillBoard(), newBoardRequest.isBlack(),
        newBoardRequest.getRules());
    board.setBoardBoxId(board.getBoardBoxId());
    Utils.setRandomIdAndCreatedAt(board);
    save(board);
    return board;
  }

  Board createBoard(@NotNull Board newBoardRequest) {
    Board board = initBoard(!newBoardRequest.getAssignedSquares().isEmpty(), newBoardRequest.isBlack(),
        newBoardRequest.getRules());
    board.setBoardBoxId(board.getBoardBoxId());
    Utils.setRandomIdAndCreatedAt(board);
    save(board);
    return board;
  }

  Board initWithDraughtsOnBoard(@NotNull Board board) {
    Board newBoard = initBoard(true, board.isBlack(), board.getRules());
    board.setBoardBoxId(board.getBoardBoxId());
    newBoard.setDomainId(board.getDomainId());
    save(newBoard);
    return newBoard;
  }

  /**
   * Create temp board and use it to emulate moves to populate notation
   *  @param boardBoxId
   * @param notationFen
   * @param genNotationHistory
   * @param fillNotationHistory
   * @param rules
   */
  void fillNotation(DomainId boardBoxId, @Nullable NotationFen notationFen, @NotNull NotationHistory genNotationHistory, @NotNull NotationHistory fillNotationHistory, EnumRules rules) {
    Board board;
    if (notationFen == null) {
      board = initBoard(true, false, rules);
    } else {
      board = initBoard(false, false, rules);
      for (NotationFen.Square square : notationFen.getWhite().getSquares()) {
        board = addDraughtFen(board, square, false);
      }
      for (NotationFen.Square square : notationFen.getBlack().getSquares()) {
        board = addDraughtFen(board, square, true);
      }
      board.setBoardBoxId(boardBoxId);
      Utils.setRandomIdAndCreatedAt(board);
      boardDao.save(board);
      NotationDrive notationDrive = new NotationDrive();
      notationDrive.setSelected(true);
      notationDrive.setCurrent(true);
      notationDrive.setNotationNumberInt(0);
      NotationMoves notationMoves = new NotationMoves();
      LinkedList<NotationSimpleMove> notationSimpleMoves = new LinkedList<>();
      NotationSimpleMove notationSimpleMove = new NotationSimpleMove("", board.getDomainId(), false);
      notationSimpleMoves.add(notationSimpleMove);
      NotationMove notationMove = new NotationMove();
      notationMove.setMove(notationSimpleMoves);
      notationMoves.add(notationMove);
      notationDrive.setMoves(notationMoves);
      fillNotationHistory.add(notationDrive);
    }
    List<Board> batchBoards = new ArrayList<>();
    fillNotationHistory.setRules(board.getRules());
    if (!genNotationHistory.isEmpty()) {
      board.setBoardBoxId(boardBoxId);
      populateBoardWithNotation(board, genNotationHistory, fillNotationHistory, batchBoards);
      if (!genNotationHistory.isEqual(fillNotationHistory)) {
        throw new BoardServiceException(ErrorMessages.UNABLE_TO_PARSE_PDN);
      }
      boardDao.batchSave(batchBoards);
    }
  }

  Board findById(DomainId boardId) {
    Board board = boardDao.findById(boardId);
    updateBoard(board);
    return board;
  }

  @NotNull Board resetHighlightAndUpdate(@NotNull Board board) {
    updateBoard(board);
    resetBoardHighlight(board);
    return board;
  }

  void delete(DomainId boardId) {
    boardDao.delete(boardId);
  }

  /**
   * @return map of {allowed, captured}
   */
  @NotNull Map getHighlight(@NotNull Board serverBoard, @NotNull Board clientBoard) {
    BoardUtils.updateMoveSquaresHighlightAndDraught(serverBoard, clientBoard);
    Square selectedSquare = serverBoard.getSelectedSquare();
    if (isValidHighlight(selectedSquare)) {
      throw new BoardServiceException(ErrorMessages.INVALID_HIGHLIGHT);
    }
    MovesList movesList = getHighlightedBoard(serverBoard.isBlackTurn(), serverBoard);
    return Map.of(SERVER_BOARD, serverBoard, MOVES_LIST, movesList);
  }

  @NotNull Map getSimpleHighlight(@NotNull Board serverBoard, @NotNull Board clientBoard) {
    BoardUtils.updateMoveSquaresHighlightAndDraught(serverBoard, clientBoard);
    Square selectedSquare = serverBoard.getSelectedSquare();
    if (isValidHighlight(selectedSquare)) {
      throw new BoardServiceException(ErrorMessages.INVALID_HIGHLIGHT);
    }
    MovesList movesList = getHighlightedAssignedMoves(serverBoard.getSelectedSquare());
    return Map.of(SERVER_BOARD, serverBoard, MOVES_LIST, movesList);
  }

  private boolean isValidHighlight(@Nullable Square selectedSquare) {
    return selectedSquare == null
        || !selectedSquare.isOccupied();
  }

  public Board move(@NotNull Board serverBoard, @NotNull Board clientBoard, @NotNull NotationHistory notationHistory) {
    return move(serverBoard, clientBoard, notationHistory, true);
  }

  /**
   * @param serverBoard map of {boardId: String, selectedSquare: Square, targetSquare: Square, allowed: List<Square>, captured: List<Square>}  @return Move info:
   *                    {v, h, targetSquare, queen} v - distance for moving vertical (minus up),
   *                    h - distance for move horizontal (minus left), targetSquare is a new square with
   * @param clientBoard
   */
  public Board move(@NotNull Board serverBoard, @NotNull Board clientBoard, @NotNull NotationHistory notationHistory, boolean save) {
    Map highlight = getHighlight(serverBoard, clientBoard);
    serverBoard = (Board) highlight.get(SERVER_BOARD);
    MovesList movesList = (MovesList) highlight.get(MOVES_LIST);
    Set<Square> allowed = movesList.getAllowed();
    TreeSquare captured = movesList.getCaptured();
    if (allowed.isEmpty()) {
      throw new BoardServiceException(ErrorMessages.UNABLE_TO_MOVE);
    }
    Square nextSquare = serverBoard.getNextSquare();
    for (Square allow : allowed) {
      if (allow.equals(nextSquare)) {
        nextSquare.setHighlight(allow.isHighlight());
        break;
      }
    }
    serverBoard.getSelectedSquare().getDraught().setHighlight(false);
    Map<String, List<Square>> capturedMapped = captured.flatTree()
        .stream()
        .collect(Collectors.groupingBy(ICoordinates::getNotation));
    updateBoardDraughts(capturedMapped, serverBoard.getWhiteDraughts(), serverBoard.getRules().getDimensionAbs());
    updateBoardDraughts(capturedMapped, serverBoard.getBlackDraughts(), serverBoard.getRules().getDimensionAbs());
    if (save) {
      boardDao.save(serverBoard);
    }

    Board nextBoard = serverBoard.deepClone();
    Utils.setRandomIdAndCreatedAt(nextBoard);
    DomainId previousBoardId = serverBoard.getDomainId();
    boolean prevBlackTurn = nextBoard.isBlackTurn();
    // MOVE DRAUGHT
    nextBoard = moveDraught(nextBoard, captured, previousBoardId, notationHistory);

    if (prevBlackTurn != nextBoard.isBlackTurn()) {
      nextBoard.setSelectedSquare(null);
    }
    if (save) {
      boardDao.save(nextBoard);
    }
    return nextBoard;
  }

  private void updateBoardDraughts(@NotNull Map<String, List<Square>> capturedMapped, @NotNull Map<String, Draught> whiteDraughts, int dimension) {
    whiteDraughts
        .values()
        .forEach(draught -> {
          draught.setDim(dimension);
          List<Square> squares = capturedMapped.get(draught.getNotation());
          if (squares != null && !squares.isEmpty()) {
            Draught capturedDraught = squares.get(0).getDraught();
            draught.setCaptured(capturedDraught.isCaptured());
            draught.setMarkCaptured(capturedMapped.size());
          }
        });
  }

  void save(Board board) {
    boardDao.save(board);
  }

  Board addDraught(@NotNull Board currentBoard, String notation, Draught draught) {
    return addDraught(currentBoard, notation, draught, true);
  }

  private Board addDraught(@NotNull Board currentBoard, String notation, Draught draught, boolean save) {
    Board deepClone = currentBoard;
    if (save) {
      deepClone = currentBoard.deepClone();
      deepClone.setBoardBoxId(currentBoard.getBoardBoxId());
      Utils.setRandomIdAndCreatedAt(deepClone);
      boardDao.save(deepClone);
    }
    BoardUtils.addDraught(deepClone, notation, draught);
    return deepClone;
  }

  void updateBoard(@NotNull Board board) {
    Board boardUpdated = BoardUtils.updateBoard(board);
    board.setWhiteDraughts(boardUpdated.getWhiteDraughts());
    board.setBlackDraughts(boardUpdated.getBlackDraughts());
    board.setSquares(boardUpdated.getSquares());
    board.setPreviousSquare(boardUpdated.getPreviousSquare());
    board.setNextSquare(boardUpdated.getNextSquare());
    board.setSelectedSquare(boardUpdated.getSelectedSquare());
    board.setAssignedSquares(boardUpdated.getAssignedSquares());
  }

  private void populateBoardWithNotation(@NotNull Board board, @NotNull NotationHistory genNotationHistory,
                                         @NotNull NotationHistory fillNotationHistory,
                                         @NotNull List<Board> batchBoards) {
    NotationHistory recursiveFillNotationHistory = NotationHistory.createWithRoot();
    populateBoardWithNotation(board, genNotationHistory, recursiveFillNotationHistory, fillNotationHistory, batchBoards, 0);
  }

  private void populateBoardWithNotation(@NotNull Board board,
                                         @NotNull NotationHistory genNotationHistory,
                                         @NotNull NotationHistory recursiveNotationHistory,
                                         @NotNull NotationHistory fillNotationHistory,
                                         @NotNull List<Board> batchBoards,
                                         int deep
  ) {
    ++deep;
    Board recursiveBoard = board;
    if (deep == 1) {
      recursiveBoard = board.deepClone();
    }
    NotationDrives innerNotations = new NotationDrives();
    for (NotationDrive notationDrive : genNotationHistory.getNotation()) {
      NotationMoves drives = notationDrive.getMoves();
      for (NotationMove drive : drives) {
        logger.trace(format("EMULATE MOVE: %s", drive.asString()));
        recursiveBoard = emulateMove(drive, recursiveBoard, recursiveNotationHistory, batchBoards);
      }
      if (!notationDrive.getVariants().isEmpty()) {
        NotationDrives variants = notationDrive.getVariants();
        NotationDrives recurNotations = new NotationDrives();
        for (NotationDrive vDrive : variants) {
          // createNotationDrives history from current drive's variant
          NotationHistory variantsNotationHistory = NotationHistory.createNotationDrives();
          variantsNotationHistory.setNotation(vDrive.getVariants().subList(1, vDrive.getVariantsSize()));

          // sync populate notation
          NotationHistory tempNotationHistory = NotationHistory.createWithRoot();
          populateBoardWithNotation(recursiveBoard, variantsNotationHistory, tempNotationHistory, fillNotationHistory, batchBoards, deep);
          NotationDrives tNotation = tempNotationHistory.getNotation();
          tNotation.removeFirst();
          if (tNotation.isEmpty()) {
            NotationDrive topDrive = recursiveNotationHistory.getNotation().getLast();
            NotationDrive topVariant = new NotationDrive();
            topVariant.getVariants().addAll(fillNotationHistory.getNotation());
            topVariant.setIdInVariants(topDrive.getVariantsSize());
            topDrive.getVariants().add(topVariant);
            innerNotations.add(topDrive);
            fillNotationHistory.getNotation().clear();
            continue;
          }
          NotationHistory tNotationHistory = NotationHistory.createNotationDrives();
          tNotationHistory.setNotation(tNotation);
          NotationDrive tDrive = vDrive.deepClone();
          tDrive.setMoves(tNotation.getFirst().getMoves());
          int idInVariants = recurNotations.size() + 1;
          tDrive.setIdInVariants(idInVariants);
          tNotation.setIdInVariants(idInVariants);
          tDrive.setVariants(tNotation);
          recurNotations.add(tDrive);
        }
        if (recurNotations.isEmpty()) {
          continue;
        }
        NotationDrive tvDrive = notationDrive.deepClone();
        if (!notationDrive.getMoves().isEmpty()) {
          tvDrive.setMoves(recursiveNotationHistory.getLast().getMoves());
        }
        tvDrive.setVariants(recurNotations);
        innerNotations.add(tvDrive);
      } else if (deep == 1) {
        innerNotations.add(recursiveNotationHistory.getLast());
      }
    }
    fillNotationHistory.getNotation().addAll(innerNotations);
  }

  private Board emulateMove(@Nullable NotationMove notationMove, Board serverBoard, @NotNull NotationHistory notationHistory, @NotNull List<Board> batchBoards) {
    if (notationMove == null) {
      return serverBoard;
    }
    serverBoard = serverBoard.deepClone();
    List<String> moves = notationMove.getMoveNotations();
    Iterator<String> iterator = moves.iterator();
    Utils.setRandomIdAndCreatedAt(serverBoard);
    batchBoards.add(serverBoard);
    String move;
    while (iterator.hasNext()) {
      move = iterator.next();
      Square selected = findSquareByNotation(move, serverBoard).deepClone();
      serverBoard.setSelectedSquare(selected.deepClone());
      move = iterator.next();
      Square next = findSquareByNotation(move, serverBoard).deepClone();
      next.setHighlight(true);
      serverBoard.setNextSquare(next);
      Board clientBoard = serverBoard.deepClone();
      serverBoard = move(serverBoard, clientBoard, notationHistory, false);
      batchBoards.add(serverBoard);

      if (!iterator.hasNext()) {
        break;
      }
    }
    return serverBoard;
  }

  private Board moveDraught(@NotNull Board board, @NotNull TreeSquare capturedSquares, DomainId prevBoardId,
                            NotationHistory notationHistory) {
    notationHistory.getNotation().getLast().setSelected(false);
    performMoveDraught(board, capturedSquares);
    Board newBoard = board.deepClone();
    boolean blackTurn = board.isBlackTurn();
    MovesList nextHighlight = getHighlightedBoard(blackTurn, newBoard);
    boolean previousCaptured = !capturedSquares.isEmpty();
    boolean hasNextCapture = isNextCapture(board, nextHighlight);
    if (previousCaptured && hasNextCapture) {
      updateNotationMiddle(newBoard, prevBoardId, notationHistory);
      return newBoard;
    }
    updateNotationEnd(board, prevBoardId, notationHistory, previousCaptured);
    resetBoardHighlight(board);
    resetCaptured(board);
    return board;
  }

  @NotNull
  private Board addDraughtFen(@NotNull Board board, NotationFen.Square square, boolean black) {
    Square sq = findSquareByNotation(square.getNumber(), board);
    Draught draught = new Draught(sq.getV(), sq.getH(), sq.getDim(), black, square.isK());
    board = addDraught(board, sq.getNotation(), draught, false);
    return board;
  }

  private boolean isNextCapture(@NotNull Board board, MovesList nextHighlight) {
    List<Draught> captured = board.getAssignedSquares()
        .stream()
        .filter(Square::isOccupied)
        .map(Square::getDraught)
        .filter(draught -> draught.getMarkCaptured() != 0)
        .collect(Collectors.toList());
    return !captured.isEmpty()
        && nextHighlight.getCaptured()
        .flatTree()
        .stream()
        .map(Square::getDraught)
        .collect(Collectors.toList())
        .containsAll(captured);
  }
}
