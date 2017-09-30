package com.workingbit.board.board;

import com.workingbit.board.board.util.BoardUtils;
import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.share.common.Log;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Draught;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.BoardBoxIds;
import com.workingbit.share.model.BoardBoxes;
import com.workingbit.share.model.CreateBoardRequest;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.workingbit.board.BoardApplication.boardBoxDao;

/**
 * Created by Aleksey Popryaduhin on 07:00 22/09/2017.
 */
public class BoardBoxService {

  private static BoardBoxService INSTANCE = new BoardBoxService();

  private static final Logger logger = Logger.getLogger(BoardBoxService.class);

  public static BoardBoxService getInstance() {
    return INSTANCE;
  }

  public BoardBox createBoard(CreateBoardRequest createBoardRequest) {
    Board board = BoardService.getInstance().createBoard(createBoardRequest);

    BoardBox boardBox = new BoardBox(board);
    boardBox.setArticleId(createBoardRequest.getArticleId());
    boardBox.setId(createBoardRequest.getBoardBoxId());
    boardBox.setCreatedAt(new Date());
    save(boardBox);

    board.setBoardBoxId(boardBox.getId());
    BoardService.getInstance().save(board);
    return boardBox;
  }

  public Optional<BoardBox> findById(String boardBoxId) {
    return boardBoxDao.findByKey(boardBoxId).map(this::updateBoardBox);
  }

  private BoardBox updateBoardBox(BoardBox boardBox) {
    Optional<Board> boardOptional = BoardService.getInstance().findById(boardBox.getBoardId());
    return boardOptional.map(board -> {
      boardBox.setBoard(board);
      return boardBox;
    }).orElse(null);
  }

  public void delete(String boardBoxId) {
    boardBoxDao.findByKey(boardBoxId)
        .map(boardBox -> {
          BoardService.getInstance().delete(boardBox.getBoardId());
          boardBoxDao.delete(boardBox.getId());
          return null;
        });
  }

  public Optional<BoardBox> highlight(BoardBox boardBox) {
    return findById(boardBox.getId())
        .map(updated -> {
          Board currentBoard = updated.getBoard();
          Square selectedSquare = boardBox.getBoard().getSelectedSquare();
          if (!selectedSquare.isOccupied()
              || boardBox.isBlackTurn() != selectedSquare.getDraught().isBlack()) {
            return updated;
          }
          BoardUtils.updateMoveSquaresHighlight(currentBoard, boardBox.getBoard());
          currentBoard = BoardService.getInstance().highlight(currentBoard);
          updated.setBoard(currentBoard);
          return updated;
        });
  }

  public Optional<BoardBox> move(BoardBox boardBox) {
    return findById(boardBox.getId())
        .map(updatedBox -> {
          Board boardUpdated = updatedBox.getBoard();
          BoardUtils.updateMoveSquaresHighlight(boardUpdated, boardBox.getBoard());
          Square nextSquare = boardUpdated.getNextSquare();
          Square selectedSquare = boardUpdated.getSelectedSquare();
          if (isValidMove(nextSquare, selectedSquare)) {
            Log.error(String.format("Invalid move Next: %s, Selected: %s", nextSquare, selectedSquare));
            return null;
          }
          boardUpdated = BoardService.getInstance().move(selectedSquare, nextSquare, boardUpdated);
          updatedBox.setBoard(boardUpdated);
          updatedBox.setBoardId(boardUpdated.getId());
          updatedBox.setBlackTurn(!updatedBox.isBlackTurn());
          boardBoxDao.save(updatedBox);
          return updatedBox;
        });
  }

  private boolean isValidMove(Square nextSquare, Square selectedSquare) {
    return nextSquare == null
        || selectedSquare == null
        || !selectedSquare.isOccupied()
        || !nextSquare.isHighlighted();
  }

  private void save(BoardBox boardBox) {
    boardBoxDao.save(boardBox);
  }

  public BoardBoxes findByIds(BoardBoxIds boardIds) {
    List<String> ids = new ArrayList<>(boardIds.size());
    ids.addAll(boardIds);
    List<BoardBox> boardBoxList = boardBoxDao.findByIds(ids)
        .stream()
        .map(this::updateBoardBox)
        .collect(Collectors.toList());
    BoardBoxes boardBoxs = new BoardBoxes();
    boardBoxs.addAll(boardBoxList);
    return boardBoxs;
  }

  public Optional<BoardBox> addDraught(BoardBox boardBox) {
    Square selectedSquare = boardBox.getBoard().getSelectedSquare();
    if (selectedSquare == null
        || !selectedSquare.isOccupied()) {
      return Optional.empty();
    }
    Draught draught = selectedSquare.getDraught();
    return findById(boardBox.getId())
        .map(updated -> {
          Board currentBoard = updated.getBoard();
          Square squareLink = BoardUtils.findSquareByLink(selectedSquare, currentBoard);
          if (squareLink == null) {
            throw new BoardServiceException("Unable to add a draught");
          }
          currentBoard = BoardService.getInstance().addDraught(currentBoard, squareLink.getNotation(), draught);
          updated.setBoardId(currentBoard.getId());
          updated.setBoard(currentBoard);
          save(updated);
          return updated;
        });
  }

  public Optional<BoardBox> undo(BoardBox boardBox) {
    return findById(boardBox.getId())
        .map(updated -> {
          Board currentBoard = updated.getBoard();
          BoardUtils.updateMoveSquaresHighlight(currentBoard, boardBox.getBoard());
          Optional<Board> undone = BoardService.getInstance().undo(currentBoard);
          if (undone.isPresent()) {
            undoRedoBoardAction(updated, undone.get());
            return updated;
          }
          return updated;
        });
  }

  public Optional<BoardBox> redo(BoardBox boardBox) {
    return findById(boardBox.getId())
        .map(updated -> {
          Board currentBoard = updated.getBoard();
          BoardUtils.updateMoveSquaresHighlight(currentBoard, boardBox.getBoard());
          Optional<Board> redone = BoardService.getInstance().redo(currentBoard);
          if (redone.isPresent()) {
            undoRedoBoardAction(updated, redone.get());
            return updated;
          }
          return updated;
        });
  }

  private void undoRedoBoardAction(BoardBox updated, Board redone) {
    updated.setBoard(redone);
    updated.setBoardId(redone.getId());
    updated.setBlackTurn(!updated.isBlackTurn());
    boardBoxDao.save(updated);
  }
}
