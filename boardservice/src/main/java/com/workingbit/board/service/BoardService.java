package com.workingbit.board.service;

import com.workingbit.board.board.util.BoardUtils;
import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.share.common.Utils;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.Draught;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.CreateBoardRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Optional;

import static com.workingbit.board.BoardApplication.boardDao;
import static com.workingbit.board.board.util.BoardUtils.highlightedBoard;
import static com.workingbit.board.board.util.BoardUtils.initBoard;

/**
 * Created by Aleksey Popryaduhin on 13:45 09/08/2017.
 */
public class BoardService {

  private static final BoardService INSTANCE = new BoardService();

  private static final Logger logger = Logger.getLogger(BoardService.class);

  public static BoardService getInstance() {
    return INSTANCE;
  }

  public Board createBoard(CreateBoardRequest newBoardRequest) {
    Board board = initBoard(newBoardRequest.getFillBoard(), newBoardRequest.getBlack(),
        newBoardRequest.getRules());
    Utils.setRandomIdAndCreatedAt(board);
    board.setCursor(true);
    save(board);
    return board;
  }

  public Optional<Board> findById(String boardId) {
    Optional<Board> boardOptional = boardDao.findByKey(boardId);
    return boardOptional.map(BoardUtils::updateBoard);
  }

  public void delete(String boardId) {
    boardDao.delete(boardId);
  }

  /**
   * @return map of {allowed, beaten}
   * @throws BoardServiceException
   */
  public Board highlight(Board boardHighlight) {
    Square selectedSquare = boardHighlight.getSelectedSquare();
    if (isValidHighlight(selectedSquare)) {
      throw new BoardServiceException("Invalid highlight square");
    }
    highlightedBoard(selectedSquare, boardHighlight);
    return boardHighlight;
  }

  private boolean isValidHighlight(Square selectedSquare) {
    return selectedSquare == null
        || !selectedSquare.isOccupied();
  }

  /**
   * @param selectedSquare
   * @param nextSquare
   * @param currentBoard         map of {boardId: String, selectedSquare: Square, targetSquare: Square, allowed: List<Square>, beaten: List<Square>}  @return Move info:
   *                       {v, h, targetSquare, queen} v - distance for moving vertical (minus up),
   *                       h - distance for move horizontal (minus left), targetSquare is a new square with
   *                       moved draught, queen is a draught has become the queen
   * @throws BoardServiceException
   */
  public Board move(Square selectedSquare, Square nextSquare, Board currentBoard) {
    currentBoard.setCursor(false);
    boardDao.save(currentBoard);

    Board nextBoard = (Board) currentBoard.deepClone();
    nextBoard.setSelectedSquare(selectedSquare);
    nextBoard.setNextSquare(nextSquare);

    nextBoard = BoardUtils.moveDraught(selectedSquare, nextBoard);
    nextBoard.pushPreviousBoard(currentBoard.getId(), selectedSquare.getNotation());

    Utils.setRandomIdAndCreatedAt(nextBoard);
    nextBoard.setCursor(true);

    boardDao.save(nextBoard);
    return nextBoard;
  }

  public void save(Board board) {
    boardDao.save(board);
  }

  public Board addDraught(Board currentBoard, String notation, Draught draught) {
    Board deepClone = (Board) currentBoard.deepClone();
    Utils.setRandomIdAndCreatedAt(deepClone);
    BoardUtils.addDraught(deepClone, notation, draught);
    boardDao.save(deepClone);
    return deepClone;
  }

  public Optional<Board> undo(Board currentBoard) {
    String previousId = currentBoard.popPreviousBoard();
    if (StringUtils.isBlank(previousId)) {
      return Optional.empty();
    }
    boardDao.save(currentBoard);
    return findById(previousId).map(previousBoard -> {
      previousBoard.pushNextBoard(currentBoard.getId(), currentBoard.getSelectedSquare().getNotation());
      boardDao.save(previousBoard);
      return previousBoard;
    });
  }

  public Optional<Board> redo(Board currentBoard) {
    String nextId = currentBoard.popNextBoard();
    if (StringUtils.isBlank(nextId)) {
      return Optional.empty();
    }
    boardDao.save(currentBoard);
    return findById(nextId).map(nextBoard -> {
      nextBoard.pushPreviousBoard(currentBoard.getId(), currentBoard.getSelectedSquare().getNotation());
      boardDao.save(nextBoard);
      return nextBoard;
    });
  }
}
