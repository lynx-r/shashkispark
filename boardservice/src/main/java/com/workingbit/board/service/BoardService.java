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

import static com.workingbit.board.BoardEmbedded.*;
import static com.workingbit.board.controller.util.BoardUtils.*;

/**
 * Created by Aleksey Popryaduhin on 13:45 09/08/2017.
 */
public class BoardService {

  private Logger logger = LoggerFactory.getLogger(BoardService.class);

  Board createBoard(@NotNull CreateBoardPayload newBoardRequest) {
    Board board = initBoard(newBoardRequest.isFillBoard(), newBoardRequest.isBlack(),
        newBoardRequest.getRules());
    board.setBoardBoxId(board.getBoardBoxId());
    Utils.setRandomIdAndCreatedAt(board);
    save(board);
    return board;
  }

  Board createBoard(@NotNull Board newBoardRequest, DomainId boardBoxId) {
    Board board = initBoard(!newBoardRequest.getAssignedSquares().isEmpty(), newBoardRequest.isBlack(),
        newBoardRequest.getRules());
    board.setBoardBoxId(boardBoxId);
    Utils.setRandomIdAndCreatedAt(board);
    save(board);
    return board;
  }

  Board initWithDraughtsOnBoard(@NotNull Board board) {
    Board newBoard = initBoard(true, board.isBlack(), board.getRules());
    newBoard.setBoardBoxId(board.getBoardBoxId());
    newBoard.setDomainId(board.getDomainId());
    save(newBoard);
    return newBoard;
  }

  /**
   * Create temp board and use it to emulate moves to populate notation
   */
  void fillNotation(DomainId boardBoxId, @Nullable NotationFen notationFen, DomainId notationId,
                    @NotNull Notation genNotation, EnumRules rules) {
    Board board;
    if (notationFen == null) {
      board = initBoard(true, false, rules);
    } else {
      board = initBoard(false, notationFen.isBlackTurn(), rules);
      for (NotationFen.Square square : notationFen.getWhite().getSquares()) {
        board = addDraughtFen(board, square, false);
      }
      for (NotationFen.Square square : notationFen.getBlack().getSquares()) {
        board = addDraughtFen(board, square, true);
      }
      board.setBoardBoxId(boardBoxId);
      board.setBlackTurn(notationFen.isBlackTurn());
      Utils.setRandomIdAndCreatedAt(board);
      boardDao.save(board);
      genNotation.getNotationFen().setBoardId(board.getDomainId());
    }
    List<Board> batchBoards = new ArrayList<>();
    if (!genNotation.getNotationHistory().isEmpty()) {
      board.setBoardBoxId(boardBoxId);
      notationService.populateBoardWithNotation(notationId, board, genNotation, batchBoards);
      if (!genNotation.getNotationHistory().isEqual(genNotation.getNotationHistory())) {
        throw new BoardServiceException(ErrorMessages.UNABLE_TO_PARSE_PDN);
      }
      genNotation.setBoardBoxId(boardBoxId);
      notationDao.save(genNotation);
      boardDao.batchSave(batchBoards);
    }
  }

  Board findById(DomainId boardId) {
    Board board = boardDao.findById(boardId);
    updateBoard(board);
    return board;
  }

  @NotNull
  Board resetHighlightAndUpdate(@NotNull Board board) {
    updateBoard(board);
    resetBoardHighlight(board);
    return board;
  }

  /**
   * @return map of {allowed, captured}
   */
  @NotNull
  Map getHighlight(@NotNull Board serverBoard, @NotNull Board clientBoard) {
    BoardUtils.updateMoveSquaresHighlightAndDraught(serverBoard, clientBoard);
    Square selectedSquare = serverBoard.getSelectedSquare();
    if (isInvalidHighlight(selectedSquare)) {
      throw new BoardServiceException(ErrorMessages.INVALID_HIGHLIGHT);
    }
    MovesList movesList = getHighlightedBoard(serverBoard.isBlackTurn(), serverBoard);
    return Map.of(SERVER_BOARD, serverBoard, MOVES_LIST, movesList);
  }

  public Board move(@NotNull Board clientBoard, @NotNull NotationHistory notationHistory) {
    return move(clientBoard, notationHistory, true);
  }

  public Board move(@NotNull Board clientBoard, @NotNull NotationHistory notationHistory, boolean save) {
    MovesList movesList = getHighlightedBoard(clientBoard.isBlackTurn(), clientBoard);
    Set<Square> allowed = movesList.getAllowed();
    TreeSquare captured = movesList.getCaptured();
    if (allowed.isEmpty()) {
      throw new BoardServiceException(ErrorMessages.UNABLE_TO_MOVE);
    }
    Square nextSquare = clientBoard.getNextSquare();
    for (Square allow : allowed) {
      if (allow.equals(nextSquare)) {
        nextSquare.setHighlight(allow.isHighlight());
        break;
      }
    }
    clientBoard.getSelectedSquare().getDraught().setHighlight(false);
    Map<String, List<Square>> capturedMapped = captured.flatTree()
        .stream()
        .collect(Collectors.groupingBy(ICoordinates::getNotation));
    updateBoardDraughts(capturedMapped, clientBoard.getWhiteDraughts(), clientBoard.getRules().getDimension());
    updateBoardDraughts(capturedMapped, clientBoard.getBlackDraughts(), clientBoard.getRules().getDimension());

    Board nextBoard = clientBoard.deepClone();
    boolean prevBlackTurn = nextBoard.isBlackTurn();
    // MOVE DRAUGHT
    nextBoard = moveDraught(nextBoard, captured, notationHistory);

    if (prevBlackTurn != nextBoard.isBlackTurn()) {
      nextBoard.setSelectedSquare(null);
    }
    if (!nextBoard.getId().equals(clientBoard.getId()) && save) {
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
//            draught.setMarkCaptured(capturedDraught.getMarkCaptured());
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

  public void updateBoard(@NotNull Board board) {
    Board boardUpdated = BoardUtils.updateBoard(board);
    board.setWhiteDraughts(boardUpdated.getWhiteDraughts());
    board.setBlackDraughts(boardUpdated.getBlackDraughts());
    board.setSquares(boardUpdated.getSquares());
    board.setPreviousSquare(boardUpdated.getPreviousSquare());
    board.setNextSquare(boardUpdated.getNextSquare());
    board.setSelectedSquare(boardUpdated.getSelectedSquare());
    board.setAssignedSquares(boardUpdated.getAssignedSquares());
  }

  Board emulateMove(@Nullable NotationMove notationMove, Board serverBoard,
                    @NotNull NotationHistory notationHistory, @NotNull List<Board> batchBoards) {
    if (notationMove == null) {
      return serverBoard;
    }
    serverBoard = serverBoard.deepClone();
    List<String> moves = notationMove.getMoveNotations();
    String move = moves.get(0);
    Square shortPrev = null;
    for (int i = 1; i < moves.size(); i++) {
      Square selected;
      if (shortPrev != null && moves.size() > 2) {
        selected = findSquareByNotation(shortPrev.getNotation(), serverBoard);
      } else {
        selected = findSquareByNotationWithHint(move, moves.subList(i - 1, moves.size()), serverBoard,
            notationMove.getNotationFormat(), moves.size());
      }
      serverBoard.setSelectedSquare(selected);
      move = moves.get(i);
      Square next = findSquareByNotationWithHint(move, moves.subList(i, moves.size()), serverBoard,
          notationMove.getNotationFormat(), moves.size());
      next.setHighlight(true);
      serverBoard.setNextSquare(next);
      Board clientBoard = serverBoard.deepClone();
      serverBoard = move(clientBoard, notationHistory, false);
      move = moves.get(i);
      shortPrev = next;
    }
    batchBoards.add(serverBoard);
    return serverBoard;
  }

  private Board moveDraught(@NotNull Board board, @NotNull TreeSquare capturedSquares,
                            NotationHistory notationHistory) {
    notationHistory.getNotation().getLast().setSelected(false);
    performMoveDraught(board, capturedSquares);
    Board newBoard = board.deepClone();
    boolean blackTurn = board.isBlackTurn();
    MovesList nextHighlight = getHighlightedBoard(blackTurn, newBoard);
    boolean previousCaptured = !capturedSquares.isEmpty();
    boolean hasNextCapture = isNextCapture(board, nextHighlight);
    if (previousCaptured && hasNextCapture) {
      updateNotationMiddle(newBoard, notationHistory);
      return newBoard;
    }
    updateNotationEnd(board, notationHistory, previousCaptured);
    resetBoardHighlight(board);
    resetCaptured(board);
//    if (!capturedSquares.isEmpty()) {
//      for (Square square : capturedSquares.flatTree()) {
//        boolean beaten = !findSquareByLink(square, board).isOccupied();
//        if (!beaten) {
//          throw new RuntimeException("Шашка не была  побита: " + square.getNotation());
//        }
//      }
//    }
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

  Square getPredictedSelectedSquare(Board board) {
    return BoardUtils.getPredictedSelectedSquare(board);
  }

  void updateAssigned(Board board) {
    List<Square> prevSquares = board.getSquares();
    List<Square> prevAssignedSquares = prevSquares
        .stream()
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    board.setAssignedSquares(prevAssignedSquares);
    boardService.updateBoard(board);
  }
}
