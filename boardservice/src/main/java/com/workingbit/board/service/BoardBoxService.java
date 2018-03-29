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
import static com.workingbit.board.BoardApplication.boardDao;

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
      updateAlternativesInBoard(boardBox, board);
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
          Board board = boardBox.getBoard();
          BoardUtils.updateMoveSquaresHighlightAndDraught(boardUpdated, board);
          Square nextSquare = boardUpdated.getNextSquare();
          Square selectedSquare = boardUpdated.getSelectedSquare();
          if (isValidMove(nextSquare, selectedSquare)) {
            logger.error(String.format("Invalid move Next: %s, Selected: %s", nextSquare, selectedSquare));
            return null;
          }
          try {
            String articleId = boardBox.getArticleId();
            NotationStrokes notationStrokes = boardBox.getNotation().getNotationStrokes();
            boardUpdated = boardService.move(selectedSquare, nextSquare, boardUpdated, articleId, notationStrokes);
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
    boolean isAtStartStroke = notationStrokes.size() == 1 && !boardNotationStrokes.isEmpty();
    if (notationStrokes.size() > 1) {
      NotationStroke lastNotation = notationStrokes.getLast();
      lastNotation.getVariants().forEach(notationStroke -> {
        resetNotationAtomStrokeCursor(notationStroke.getFirst());
        resetNotationAtomStrokeCursor(notationStroke.getSecond());
      });
      boardNotationStrokes
          .stream()
          .filter(lastNotation::equals)
          .findFirst()
          .ifPresent(notationStroke -> notationStroke.setVariants(lastNotation.getVariants()));
    } else if (isAtStartStroke) {
      NotationStroke lastNotation = notationStrokes.getLast();
      NotationStroke lastBoardStroke = boardNotationStrokes.getLast();
      boolean isAddedStrokeInBoard = lastBoardStroke.getMoveNumberInt() > notationStrokes.size();
      if (isAddedStrokeInBoard) { // update variants on prev board stroke
        boardNotationStrokes.get(boardNotationStrokes.size() - 2).setVariants(lastNotation.getVariants());
      } else { // update variants on last stroke in board
        boardNotationStrokes.getLast().setVariants(lastNotation.getVariants());
      }
    }
    if (!boardNotationStrokes.isEmpty()) {
      boolean isFirstCountMoreSecond = boardNotationStrokes.size() >= 2
          && boardNotationStrokes.getFirst().getMoveNumberInt() > boardNotationStrokes.getLast().getMoveNumberInt();
      if (isFirstCountMoreSecond) {
        Collections.reverse(boardNotationStrokes);
      }
      updatedBox.getNotation().setNotationStrokes(boardNotationStrokes);
    }
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
    boardBox = updateBoardBox(boardBox);
    boardBoxDao.save(boardBox);
    return Optional.of(boardBox);
  }

  public Optional<BoardBox> loadBoard(BoardBox boardBox) {
    return saveAndFillBoard(boardBox);
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
            currentBoard = boardService.addDraught(boardBox.getArticleId(), currentBoard, squareLink.getPdnNotation(), draught);
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
          Board board = boardBox.getBoard();
          BoardUtils.updateMoveSquaresHighlightAndDraught(currentBoard, board);
          Optional<Board> undone;
          try {
            undone = boardService.undo(currentBoard);
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
          }
          undone.ifPresent((b) -> {
            forkNotation(updated, b);
            undoRedoBoardActionAndSave(updated, b);
          });
          return updated;
        });
  }

  private void forkNotation(BoardBox updated, Board board) {
    Notation notation = updated.getNotation();
    NotationStrokes notationStrokes = notation.getNotationStrokes();

    NotationStroke undoneStrokes = null;
    if (notationStrokes.size() >= 2) { // if strokes more then two
      undoneStrokes = notationStrokes.removeLast();
    } else if (notationStrokes.isEmpty()) {
      return;
    }

    NotationStroke lastStroke = notationStrokes.getLast();
    NotationStrokes boardNotationStrokes = board.getNotationStrokes();
    if (!boardNotationStrokes.isEmpty()) { // if user doesn't undone all strokes
      NotationStroke prevStroke = boardNotationStrokes.get(boardNotationStrokes.size() - 1);
      boolean isAddedStroke = prevStroke.getMoveNumberInt() > notationStrokes.size();
      if (isAddedStroke) { // if user does redo
        notationStrokes.add(prevStroke);
      } else { // if user does undo
        lastStroke.setFirst(prevStroke.getFirst());
        lastStroke.setSecond(prevStroke.getSecond());
      }
    } else { // first stroke in notation e.g. ( 1. c3-b4 )
      lastStroke.getFirst().setCursor(false);
      lastStroke.getVariants().add(lastStroke.deepClone());
      lastStroke.setMoveNumber(null);
      lastStroke.setFirst(null);
    }

    boolean isNotFirstStroke = undoneStrokes != null && boardNotationStrokes.size() != 1;
    if (isNotFirstStroke) { // if user is on his first stroke
      lastStroke.getVariants().add(undoneStrokes);
    }
  }

  public Optional<BoardBox> redo(BoardBox boardBox) {
    return findById(boardBox.getId())
        .map(updated -> {
          Board currentBoard = updated.getBoard();
          Board board = boardBox.getBoard();
          BoardUtils.updateMoveSquaresHighlightAndDraught(currentBoard, board);
          Optional<Board> redone;
          try {
            redone = boardService.redo(currentBoard);
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
          }
          redone.ifPresent((b) -> {
            updateAlternativesInBoard(updated, b);
            undoRedoBoardActionAndSave(updated, b);
          });
          return updated;
        });
  }

  private void undoRedoBoardActionAndSave(BoardBox updated, Board redone) {
    updated.setBoard(redone);
    updated.setBoardId(redone.getId());
    boardDao.save(redone);
    boardBoxDao.save(updated);
  }
}
