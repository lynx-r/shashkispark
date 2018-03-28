package com.workingbit.board.service;

import com.workingbit.board.controller.util.BoardUtils;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Draught;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.*;
import com.workingbit.share.util.Utils;
import org.apache.log4j.Logger;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.workingbit.board.BoardApplication.boardBoxDao;

/**
 * Created by Aleksey Popryaduhin on 07:00 22/09/2017.
 */
public class BoardBoxService {

  private final Logger logger = Logger.getLogger(BoardBoxService.class);
  private final static BoardService boardService = new BoardService();

  public Optional<BoardBox> createBoardBox(CreateBoardPayload createBoardPayload) {
    Board board = boardService.createBoard(createBoardPayload);

    BoardBox boardBox = new BoardBox(board);
    boardBox.setArticleId(createBoardPayload.getArticleId());
    Utils.setBoardBoxIdAndCreatedAt(boardBox, createBoardPayload);
    boardBox.setCreatedAt(LocalDateTime.now());
    saveAndFillBoard(boardBox);

    board.setBoardBoxId(boardBox.getId());
    boardService.save(board);
    return Optional.of(boardBox);
  }

  public Optional<BoardBox> findById(String boardBoxId) {
    return boardBoxDao.findByKey(boardBoxId)
        .map(this::updateBoardBox);
//        .map(this::updateBoardNotation);
  }

  private BoardBox updateBoardBox(BoardBox boardBox) {
    Optional<Board> boardOptional = boardService.findById(boardBox.getBoardId());
    return boardOptional.map(board -> {
      boardBox.setBoard(board);
      return boardBox;
    }).orElse(null);
  }

  void delete(String boardBoxId) {
    boardBoxDao.findByKey(boardBoxId)
        .map(boardBox -> {
          boardService.delete(boardBox.getBoardId());
          boardBoxDao.delete(boardBox.getId());
          return null;
        });
  }

  public Optional<BoardBox> highlight(BoardBox boardBox) {
    return findById(boardBox.getId())
        .map(updated -> {
          Board currentBoard = updated.getBoard();
          Square selectedSquare = boardBox.getBoard().getSelectedSquare();
          if (!selectedSquare.isOccupied()) {
            return updated;
          }
          BoardUtils.updateMoveSquaresHighlightAndDraught(currentBoard, boardBox.getBoard());
          try {
            currentBoard = boardService.highlight(currentBoard);
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
          }
          updated.setBoard(currentBoard);
          return updated;
        });
  }

  public Optional<BoardBox> move(BoardBox boardBox) {
    return findById(boardBox.getId())
        .map(updatedBox -> {
          Board boardUpdated = updatedBox.getBoard();
          BoardUtils.updateMoveSquaresHighlightAndDraught(boardUpdated, boardBox.getBoard());
          Square nextSquare = boardUpdated.getNextSquare();
          Square selectedSquare = boardUpdated.getSelectedSquare();
          if (isValidMove(nextSquare, selectedSquare)) {
            logger.error(String.format("Invalid move Next: %s, Selected: %s", nextSquare, selectedSquare));
            return null;
          }
          try {
            boardUpdated = boardService.move(selectedSquare, nextSquare, boardUpdated, boardBox.getArticleId(),
                boardBox.getNotation().getNotationStrokes());
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
          }
          updatedBox.setBoard(boardUpdated);
          updatedBox.setBoardId(boardUpdated.getId());

          updateAlternativesInBoard(updatedBox, boardUpdated);
          boardBoxDao.save(updatedBox);
          return updatedBox;
        });
  }

  private void updateAlternativesInBoard(BoardBox updatedBox, Board boardUpdated) {
    NotationStrokes notationStrokes = updatedBox.getNotation().getNotationStrokes();
    NotationStrokes boardNotationStrokes = boardUpdated.getNotationStrokes();
    if (notationStrokes.size() > 0) {
      NotationStroke lastNotation = notationStrokes.getLast();
      lastNotation.getAlternative().forEach(notationStroke -> {
        resetNotationAtomStrokeCursor(notationStroke.getFirst());
        resetNotationAtomStrokeCursor(notationStroke.getSecond());
      });
      boardNotationStrokes
          .stream()
          .filter(lastNotation::equals)
          .findFirst()
          .ifPresent(notationStroke -> notationStroke.setAlternative(lastNotation.getAlternative()));
    }
    Collections.reverse(boardNotationStrokes);
    updatedBox.getNotation().setNotationStrokes(boardNotationStrokes);
  }

  private void resetNotationAtomStrokeCursor(NotationAtomStroke atomStroke) {
    if (atomStroke != null) {
      atomStroke.setCursor(false);
    }
  }

  public Optional<BoardBox> makeWhiteStroke(BoardBox boardBox) {
    return findById(boardBox.getId())
        .map(updatedBox -> {
          Board inverted = boardBox.getBoard();
          inverted.setBlackTurn(!inverted.isBlackTurn());
          boardService.save(inverted);

          Board board = boardService.updateBoard(inverted);
          updatedBox.setBoard(board);
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

  public Optional<BoardBox> saveAndFillBoard(BoardBox boardBox) {
    boardBoxDao.save(boardBox);
    boardBox = updateBoardBox(boardBox);
    return Optional.of(boardBox);
  }

  public Optional<BoardBox> loadBoard(BoardBox boardBox) {
    BoardBox updated = updateBoardBox(boardBox);
    return Optional.of(updated);
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
            logger.error("Unable to add a draught");
            return null;
          }
          try {
            currentBoard = boardService.addDraught(boardBox.getArticleId(), currentBoard, squareLink.getNotation(), draught);
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
          }
          updated.setBoardId(currentBoard.getId());
          updated.setBoard(currentBoard);
          saveAndFillBoard(updated);
          return updated;
        });
  }

  public Optional<BoardBox> undo(BoardBox boardBox) {
    return findById(boardBox.getId())
        .map(updated -> {
          Board currentBoard = updated.getBoard();
          BoardUtils.updateMoveSquaresHighlightAndDraught(currentBoard, boardBox.getBoard());
          Optional<Board> undone;
          try {
            undone = boardService.undo(currentBoard);
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
          }
          undone.ifPresent((board -> {
            forkNotation(updated, board);
            undoRedoBoardActionAndSave(updated, board);
          }));
          return updated;
        });
  }

  private void forkNotation(BoardBox updated, Board board) {
    Notation notation = updated.getNotation();
    NotationStrokes notationStrokes = notation.getNotationStrokes();
    if (notationStrokes.size() < 2) {
      return;
    }

    addAlternative(notationStrokes);
//    addAlternative(board.getNotationStrokes());
  }

  private void addAlternative(NotationStrokes notationStrokes) {
    NotationStroke undoneStrokes = notationStrokes.removeLast();
    NotationStroke lastStroke = notationStrokes.getLast();

    lastStroke.getAlternative().add(undoneStrokes);
  }

  public Optional<BoardBox> redo(BoardBox boardBox) {
    return findById(boardBox.getId())
        .map(updated -> {
          Board currentBoard = updated.getBoard();
          BoardUtils.updateMoveSquaresHighlightAndDraught(currentBoard, boardBox.getBoard());
          Optional<Board> redone;
          try {
            redone = boardService.redo(currentBoard);
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
          }
          redone.ifPresent((board) -> undoRedoBoardActionAndSave(updated, board));
          return updated;
        });
  }

  private void undoRedoBoardActionAndSave(BoardBox updated, Board redone) {
    updated.setBoard(redone);
    updated.setBoardId(redone.getId());
    boardBoxDao.save(updated);
  }

  private BoardBox updateBoardNotation(BoardBox boardBox) {
    int bbNotationSize = boardBox.getNotation().getNotationStrokes().size();
    Board board = boardBox.getBoard();
    int currentBoardNotationSize = board.getNotationStrokes().size();
    // add a new notation strokes from Board to BoardBox
    if (currentBoardNotationSize >= bbNotationSize) {
//      NotationStrokes notationStrokes = BoardUtils.reverseBoardNotation(notationStrokes1);
      NotationStrokes notationStrokes = board.getNotationStrokes();
      Collections.reverse(notationStrokes);
      boardBox.getNotation().setNotationStrokes(notationStrokes);
    }
    BoardUtils.assignBoardNotationCursor(boardBox.getNotation().getNotationStrokes(), boardBox.getBoardId());
    return boardBox;
  }
}
