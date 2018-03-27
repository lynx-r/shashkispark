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
        .map(this::updateBoardBox)
        .map(this::updateBoardNotation);
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
            boardUpdated = boardService.move(selectedSquare, nextSquare, boardUpdated, boardBox.getArticleId());
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
          }
          updatedBox.setBoard(boardUpdated);
          updatedBox.setBoardId(boardUpdated.getId());
//          NotationStrokes notationStrokes = branchNotation(boardUpdated.getNotationStrokes(),
//              updatedBox.getNotation().getNotationStrokes(),
//              boardUpdated);
          NotationStrokes reversed = BoardUtils.reverseBoardNotation(boardUpdated.getNotationStrokes());
          System.out.println(BoardUtils.printBoardNotation(reversed));
          updatedBox.getNotation().setNotationStrokes(reversed);
          boardBoxDao.save(updatedBox);
          return updatedBox;
        });
  }

  private NotationStrokes branchNotation(NotationStrokes boardNotation, NotationStrokes boardBoxNotation, Board board) {
    if (board.isUndo()) {
      NotationStroke firstBoard = boardNotation.getFirst();
      System.out.println("CURRENT COUNT " + firstBoard.getCount());
      NotationStrokes notationStrokes = boardBoxNotation
          .stream()
          .filter(ns -> ns.getCount() >= firstBoard.getCount())
          .collect(Collectors.toCollection(NotationStrokes::new));
      if (notationStrokes.isEmpty()) {
        return boardNotation;
      }
      System.out.println(BoardUtils.printBoardNotation(boardBoxNotation));
      NotationStroke firstStroke = notationStrokes.getFirst();
      if (board.isBlackTurn()) {
        addAlternatives(firstBoard.getFirst(), firstStroke, notationStrokes);
      } else {
        addAlternatives(firstBoard.getSecond(), firstStroke, notationStrokes);
      }
      System.out.println(BoardUtils.printBoardNotation(boardNotation));
      board.setUndo(false);
      boardService.save(board);
      return boardNotation;
    }
    return boardNotation;
  }

  private void addAlternatives(NotationAtomStroke stroke, NotationStroke firstStroke, NotationStrokes notationStrokes) {
    NotationStrokes alternative = firstStroke.getFirst().getAlternative();
    if (alternative.isEmpty()) {
      stroke.getAlternative().addAll(notationStrokes);
    } else {
      NotationStrokes alternativeOfAlternative = notationStrokes
          .stream()
          .flatMap(ns -> ns.getFirst().getAlternative().stream())
          .collect(Collectors.toCollection(NotationStrokes::new));
      NotationStrokes strokesWithoutAlternatives = notationStrokes
          .stream()
          .peek(ns -> ns.getFirst().setAlternative(new NotationStrokes()))
          .collect(Collectors.toCollection(NotationStrokes::new));
      alternative.addAll(alternativeOfAlternative);
      alternative.addAll(strokesWithoutAlternatives);
      stroke.setAlternative(alternative);
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
    BoardBox updated = updateBoardNotation(boardBox);
    updated = updateBoardBox(updated);
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
          BoardUtils.updateMoveSquaresHighlightAndDraught(currentBoard, boardBox.getBoard());
          Optional<Board> redone;
          try {
            redone = boardService.redo(currentBoard);
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
          }
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
    boardBoxDao.save(updated);
  }

  private BoardBox updateBoardNotation(BoardBox boardBox) {
    int bbNotationSize = boardBox.getNotation().getNotationStrokes().size();
    Board board = boardBox.getBoard();
    int currentBoardNotationSize = board.getNotationStrokes().size();
    if (currentBoardNotationSize >= bbNotationSize) {
      NotationStrokes notationStrokes = BoardUtils.reverseBoardNotation(board.getNotationStrokes());
      boardBox.getNotation().setNotationStrokes(notationStrokes);
    }
    BoardUtils.assignBoardNotationCursor(boardBox.getNotation().getNotationStrokes(), boardBox.getBoardId());
    return boardBox;
  }
}
