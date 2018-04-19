package com.workingbit.board.service;

import com.workingbit.board.controller.util.BoardUtils;
import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.Draught;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.*;
import com.workingbit.share.util.Utils;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static com.workingbit.board.BoardEmbedded.boardDao;
import static com.workingbit.board.controller.util.BoardUtils.*;

/**
 * Created by Aleksey Popryaduhin on 13:45 09/08/2017.
 */
public class BoardService {

  Board createBoard(CreateBoardPayload newBoardRequest) {
    Board board = initBoard(newBoardRequest.getFillBoard(), newBoardRequest.getBlack(),
        newBoardRequest.getRules());
    board.setBoardBoxId(board.getBoardBoxId());
    Utils.setRandomIdAndCreatedAt(board);
    save(board);
    return board;
  }

  void createBoardFromNotation(NotationHistory genNotationHistory, NotationHistory fillNotationHistory, EnumRules rules) {
    Board board = initBoard(true, false, rules);
    board.setBoardBoxId(board.getBoardBoxId());
    Utils.setRandomIdAndCreatedAt(board);
    syncBoardWithNotation(board, genNotationHistory, fillNotationHistory);
  }

  Optional<Board> findById(String boardId) {
    Optional<Board> boardOptional = boardDao.findById(boardId);
    return boardOptional.map(this::updateBoard);
  }

  Board resetHighlightAndUpdate(Board board) {
    board = updateBoard(board);
    resetBoardHighlight(board);
    return board;
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
    highlightedBoard(boardHighlight.isBlackTurn(), boardHighlight);
    return boardHighlight;
  }

  private boolean isValidHighlight(Square selectedSquare) {
    return selectedSquare == null
        || !selectedSquare.isOccupied();
  }

  /**
   * @param board map of {boardId: String, selectedSquare: Square, targetSquare: Square, allowed: List<Square>, captured: List<Square>}  @return Move info:
   *                     {v, h, targetSquare, queen} v - distance for moving vertical (minus up),
   *                     h - distance for move horizontal (minus left), targetSquare is a new square with
   */
  public Board move(Board board, NotationHistory notationHistory) {
    boolean blackTurn = board.isBlackTurn();
    board = updateBoard(board);
    MovesList movesList = highlightedBoard(blackTurn, board);
    List<Square> allowed = movesList.getAllowed();
    List<Square> captured = movesList.getCaptured();
    if (allowed.isEmpty()) {
      throw new BoardServiceException(ErrorMessages.UNABLE_TO_MOVE);
    }
    board.getSelectedSquare().getDraught().setHighlight(false);
    boardDao.save(board);

    Board nextBoard = board.deepClone();
    nextBoard.setSelectedSquare(board.getSelectedSquare());
    nextBoard.setNextSquare(board.getNextSquare());

    // should be there because in move draught, I set boardId in notation
    nextBoard.setBoardBoxId(board.getBoardBoxId());
    Utils.setRandomIdAndCreatedAt(nextBoard);

    String boardId = board.getId();
    // MOVE DRAUGHT
    nextBoard = BoardUtils.moveDraught(nextBoard, captured, boardId, notationHistory);

    boardDao.save(nextBoard);
    return nextBoard;
  }

  void save(Board board) {
    boardDao.save(board);
  }

  Board addDraught(Board currentBoard, String notation, Draught draught) {
    Board deepClone = currentBoard.deepClone();
    deepClone.setBoardBoxId(currentBoard.getBoardBoxId());
    Utils.setRandomIdAndCreatedAt(deepClone);
    BoardUtils.addDraught(deepClone, notation, draught);
    boardDao.save(deepClone);
    return deepClone;
  }

//  Optional<Board> undo(Board currentBoard) {
//    String previousId = currentBoard.popPreviousBoard();
//    if (StringUtils.isBlank(previousId)) {
//      return Optional.empty();
//    }
//    boardDao.save(currentBoard);
//    return findById(previousId).map(undoneBoard -> {
//      String selectedMove = currentBoard.getPreviousSquare().getNotation();
//      String possibleMove = currentBoard.getSelectedSquare().getNotation();
//      undoneBoard.pushNextBoard(currentBoard.getId(), selectedMove, possibleMove);
//
//      // reset highlights
//      undoneBoard.getSelectedSquare().getDraught().setHighlight(false);
//      undoneBoard.getNextSquare().setHighlight(false);
//
//      return undoneBoard;
//    });
//  }

//  Optional<Board> redo(Board currentBoard) {
//    String nextId = currentBoard.popNextBoard();
//    if (StringUtils.isBlank(nextId)) {
//      return Optional.empty();
//    }
//    boardDao.save(currentBoard);
//    return findById(nextId).map(redoneBoard -> {
//      Square square = getNextOrPrevSquare(currentBoard);
//      String notation = square != null ? square.getNotation() : null;
//      redoneBoard.pushPreviousBoard(currentBoard.getId(),
//          notation,
//          currentBoard.getSelectedSquare().getNotation());
//      return redoneBoard;
//    });
//  }

//  private Square getNextOrPrevSquare(Board currentBoard) {
//    return currentBoard.getNextSquare() == null
//        ? currentBoard.getPreviousSquare() : currentBoard.getNextSquare();
//  }

  Board updateBoard(Board board) {
    return BoardUtils.updateBoard(board);
  }

  private void syncBoardWithNotation(Board board, NotationHistory genNotationHistory, NotationHistory fillNotationHistory) {
    for (NotationDrive notationDrive : genNotationHistory.getNotation()) {
      NotationMoves drives = notationDrive.getMoves();
      for (NotationMove drive : drives) {
        board = emulateMove(drive, board, fillNotationHistory);
      }
    }
  }

  private Board emulateMove(NotationMove notationMove, Board board, NotationHistory notationHistory) {
    if (notationMove == null) {
      return board;
    }
    List<String> moves = notationMove.getMoveNotations();
    Iterator<String> iterator = moves.iterator();
    for (String move = iterator.next(); iterator.hasNext();) {
      Square selected = findSquareByNotation(move, board).deepClone();
      board.setSelectedSquare(selected);
      move = iterator.next();
      Square next = findSquareByNotation(move, board).deepClone();
      next.setHighlight(true);
      board.setNextSquare(next);
      board = move(board, notationHistory);
      if (!iterator.hasNext()) {
        break;
      }
    }
    return board;
  }
}
