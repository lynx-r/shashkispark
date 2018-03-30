package com.workingbit.board.service;

import com.workingbit.board.controller.util.BoardUtils;
import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.Draught;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.*;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.workingbit.board.BoardApplication.boardDao;
import static com.workingbit.board.controller.util.BoardUtils.findSquareByNotation;
import static com.workingbit.board.controller.util.BoardUtils.highlightedBoard;
import static com.workingbit.board.controller.util.BoardUtils.initBoard;

/**
 * Created by Aleksey Popryaduhin on 13:45 09/08/2017.
 */
public class BoardService {

  Board createBoard(CreateBoardPayload newBoardRequest) {
    Board board = initBoard(newBoardRequest.getFillBoard(), newBoardRequest.getBlack(),
        newBoardRequest.getRules());
    Utils.setBoardIdAndCreatedAt(board, newBoardRequest.getArticleId(), newBoardRequest.getBoardBoxId());
    board.setCursor(true);
    save(board);
    return board;
  }

  public Board createBoardFromNotation(Notation notation, String articleId, String boardBoxId) {
    Board board = initBoard(true, false, notation.getRules());
    Utils.setBoardIdAndCreatedAt(board, articleId, boardBoxId);
    return syncBoardWithStrokes(board, notation.getNotationDrives(), articleId);
  }

  Optional<Board> findById(String boardId) {
    Optional<Board> boardOptional = boardDao.findByKey(boardId);
    return boardOptional.map(this::updateBoard);
  }

  void delete(String boardId) {
    boardDao.delete(boardId);
  }

  /**
   * @return map of {allowed, captured}
   */
  Board highlight(Board boardHighlight) {
    Square selectedSquare = boardHighlight.getSelectedSquare();
    if (isValidHighlight(selectedSquare)) {
      throw new BoardServiceException("Invalid highlight square");
    }
    highlightedBoard(boardHighlight.isBlackTurn(), selectedSquare, boardHighlight);
    return boardHighlight;
  }

  private boolean isValidHighlight(Square selectedSquare) {
    return selectedSquare == null
        || !selectedSquare.isOccupied();
  }

  /**
   * @param currentBoard map of {boardId: String, selectedSquare: Square, targetSquare: Square, allowed: List<Square>, captured: List<Square>}  @return Move info:
   *                     {v, h, targetSquare, queen} v - distance for moving vertical (minus up),
   *                     h - distance for move horizontal (minus left), targetSquare is a new square with
   * @param articleId
   * @param boardBoxNotationDrives
   */
  public Board move(Square selectedSquare, Square nextSquare, Board currentBoard, String articleId,
                    NotationDrives boardBoxNotationDrives) {
    boolean blackTurn = currentBoard.isBlackTurn();
    MovesList movesList = highlightedBoard(blackTurn, selectedSquare, currentBoard);
    List<Square> allowed = movesList.getAllowed();
    List<Square> captured = movesList.getCaptured();
    if (allowed.isEmpty()) {
      throw new BoardServiceException(ErrorMessages.UNABLE_TO_MOVE);
    }
    currentBoard.setCursor(false);
    boardDao.save(currentBoard);

    Board nextBoard = currentBoard.deepClone();
    nextBoard.setSelectedSquare(selectedSquare);
    nextBoard.setNextSquare(nextSquare);

    // should be there because in move draught, I set boardId in notation
    String boardBoxId = nextBoard.getBoardBoxId();
    Utils.setBoardIdAndCreatedAt(nextBoard, articleId, boardBoxId);
    nextBoard.setCursor(true);

    nextBoard = BoardUtils.moveDraught(nextBoard, captured);
    String boardId = currentBoard.getId();
    String notation = selectedSquare.getNotation();
    String nextNotation = nextSquare.getNotation();
    nextBoard.pushPreviousBoard(boardId, notation, nextNotation);

    if (boardBoxNotationDrives != null) { // in case when I fill it from NotationParseService
      NotationDrives boardNotationDrives = nextBoard.getNotationDrives();
      updateBoardAlternativeNotation(boardBoxNotationDrives, boardNotationDrives);
    }

    boardDao.save(nextBoard);
    return nextBoard;
  }

  private void updateBoardAlternativeNotation(NotationDrives boardBoxNotationDrives,
                                              NotationDrives boardNotationDrives) {
    if (boardBoxNotationDrives.size() > 0) {
      NotationDrive lastNotation = boardBoxNotationDrives.getLast();
      boardNotationDrives
          .stream()
          .filter(lastNotation::equals)
          .findFirst()
          .ifPresent(notationStroke -> notationStroke.setVariants(lastNotation.getVariants()));
    }
  }

  void save(Board board) {
    boardDao.save(board);
  }

  Board addDraught(String articleId, Board currentBoard, String notation, Draught draught) {
    Board deepClone = currentBoard.deepClone();
    Utils.setBoardIdAndCreatedAt(deepClone, articleId, currentBoard.getBoardBoxId());
    BoardUtils.addDraught(deepClone, notation, draught);
    boardDao.save(deepClone);
    return deepClone;
  }

  Optional<Board> undo(Board currentBoard) {
    String previousId = currentBoard.popPreviousBoard();
    if (StringUtils.isBlank(previousId)) {
      return Optional.empty();
    }
    boardDao.save(currentBoard);
    return findById(previousId).map(previousBoard -> {
      previousBoard.pushNextBoard(currentBoard.getId(),
          currentBoard.getPreviousSquare().getAlphanumericNotation64(),
          currentBoard.getSelectedSquare().getAlphanumericNotation64());
      boardDao.save(previousBoard);
      return previousBoard;
    });
  }

  Optional<Board> redo(Board currentBoard) {
    String nextId = currentBoard.popNextBoard();
    if (StringUtils.isBlank(nextId)) {
      return Optional.empty();
    }
    boardDao.save(currentBoard);
    return findById(nextId).map(nextBoard -> {
      Square square = currentBoard.getNextSquare() == null
          ? currentBoard.getPreviousSquare() : currentBoard.getNextSquare();
      String notation = square != null ? square.getAlphanumericNotation64() : null;
      nextBoard.pushPreviousBoard(currentBoard.getId(),
          notation,
          currentBoard.getSelectedSquare().getAlphanumericNotation64());
      boardDao.save(nextBoard);
      return nextBoard;
    });
  }

  public Board updateBoard(Board board) {
    return BoardUtils.updateBoard(board);
  }

  private Board syncBoardWithStrokes(Board board, NotationDrives notationDrives, String articleId) {
    for (NotationDrive notationDrive : notationDrives) {
      NotationMoves drives = notationDrive.getMoves();
      for (NotationMove drive : drives) {
        board = emulateMove(board, articleId, drive);
      }
    }
    NotationDrives syncedNotationDrives = board.getNotationDrives();
    Collections.reverse(syncedNotationDrives);
    board.setNotationDrives(syncedNotationDrives);
    return board;
  }

  private Board emulateMove(Board board, String articleId, NotationMove notationMove) {
    if (notationMove == null) {
      return board;
    }
    String[] moves = notationMove.getMove();
    for (int i = 0; i < moves.length - 1; i++) {
      Square selected = findSquareByNotation(moves[i], board);
      board.setSelectedSquare(selected);
      Square next = findSquareByNotation(moves[i + 1], board);
      board.setNextSquare(next);
      board = move(selected, next, board, articleId, null);
    }
    return board;
  }
}
