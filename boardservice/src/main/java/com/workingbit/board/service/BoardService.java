package com.workingbit.board.service;

import com.workingbit.board.controller.util.BoardUtils;
import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.Draught;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.CreateBoardPayload;
import com.workingbit.share.model.MovesList;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.workingbit.board.BoardApplication.boardDao;
import static com.workingbit.board.controller.util.BoardUtils.highlightedBoard;
import static com.workingbit.board.controller.util.BoardUtils.initBoard;
import static com.workingbit.share.util.JsonUtils.dataToJson;

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

  Optional<Board> findById(String boardId) {
    List<Board> all = boardDao.findAll(100);
    String s = dataToJson(all);
    System.out.println(s);
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
   */
  public Board move(Square selectedSquare, Square nextSquare, Board currentBoard, String articleId) {
    boolean blackTurn = currentBoard.isBlackTurn();
    MovesList movesList = highlightedBoard(blackTurn, selectedSquare, currentBoard);
    List<Square> allowed = movesList.getAllowed();
    List<Square> captured = movesList.getCaptured();
    if (allowed.isEmpty()) {
      throw new BoardServiceException(ErrorMessages.UNABLE_TO_MOVE);
    }
    currentBoard.setCursor(false);
    boardDao.save(currentBoard);

    Board nextBoard = (Board) currentBoard.deepClone();
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

    boardDao.save(nextBoard);
    return nextBoard;
  }

  void save(Board board) {
    boardDao.save(board);
  }

  Board addDraught(String articleId, Board currentBoard, String notation, Draught draught) {
    Board deepClone = (Board) currentBoard.deepClone();
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
          currentBoard.getPreviousSquare().getNotation(),
          currentBoard.getSelectedSquare().getNotation());
      previousBoard.setUndo(true);
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
      String notation = square != null ? square.getNotation() : null;
      nextBoard.pushPreviousBoard(currentBoard.getId(),
          notation,
          currentBoard.getSelectedSquare().getNotation());
      nextBoard.setRedo(true);
      boardDao.save(nextBoard);
      return nextBoard;
    });
  }

  public Board updateBoard(Board board) {
    return BoardUtils.updateBoard(board);
  }
}
