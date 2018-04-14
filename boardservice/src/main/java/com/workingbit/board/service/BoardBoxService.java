package com.workingbit.board.service;

import com.workingbit.board.controller.util.BoardUtils;
import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Draught;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.*;
import com.workingbit.share.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Optional;

import static com.workingbit.board.BoardApplication.boardBoxDao;
import static com.workingbit.board.BoardApplication.boardDao;

/**
 * Created by Aleksey Popryaduhin on 07:00 22/09/2017.
 */
public class BoardBoxService {

  private final Logger logger = LoggerFactory.getLogger(BoardBoxService.class);
  private final static BoardService boardService = new BoardService();

  public Optional<BoardBox> createBoardBox(CreateBoardPayload createBoardPayload) {
    Board board = boardService.createBoard(createBoardPayload);

    BoardBox boardBox = new BoardBox(board);
    boardBox.setArticleId(createBoardPayload.getArticleId());
    Utils.setBoardBoxIdAndCreatedAt(boardBox);
    boardBox.setCreatedAt(LocalDateTime.now());
    saveAndFillBoard(boardBox);

    board.setBoardBoxId(boardBox.getId());
    boardService.save(board);
    return Optional.of(boardBox);
  }

  public Optional<BoardBox> createBoardBoxFromNotation(String articleId, String boardBoxId, Notation fromNotation) {
    BoardBox boardBox = new BoardBox();
    boardBox.setArticleId(articleId);
    Utils.setBoardBoxIdAndCreatedAt(boardBox);

    Board board = boardService.createBoardFromNotation(fromNotation, boardBoxId);
    Notation notation = new Notation(fromNotation.getTags(), fromNotation.getRules(), fromNotation.getNotationHistory());
    boardBox.setNotation(notation);

    // switch boardBox to the first board
    LinkedList<BoardIdNotation> previousBoards = board.getPreviousBoards();
    String firstBoardId = previousBoards.getLast().getBoardId();
    return boardDao.findByKey(firstBoardId)
        .map(firstBoard -> {
          String f_boardId = firstBoard.getId();
          boardBox.setNotation(notation);
          boardBox.setBoardId(f_boardId);
          boardBox.setBoard(firstBoard);
          return boardBox;
        })
        .map(this::saveAndFillBoard);
  }

  public Optional<BoardBox> find(BoardBox boardBox) {
    return boardBoxDao.find(boardBox)
        .map(this::updateBoardBox);
  }

  public Optional<BoardBox> findById(String boardBoxId) {
    return boardBoxDao.findByKey(boardBoxId)
        .map(this::updateBoardBox);
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
    if (resetHighlightIfNotLastBoard(boardBox)) {
      return Optional.of(boardBox);
    }
    return find(boardBox)
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
    return find(boardBox)
        .map(updatedBox -> {
          Board boardUpdated = updatedBox.getBoard();
          Board board = boardBox.getBoard();
          NotationHistory boardHistory = boardBox.getNotation().getNotationHistory();
          boolean hasFirstMoveInDrive = boardHistory.size() > 0
              && boardHistory.getLast().getMoves().size() == 1;
          boolean hasSecondMoveInDrive = boardHistory.size() > 0
              && boardHistory.getLast().getMoves().size() == 2;
          boolean isWhiteTurn = !board.isBlackTurn();
          boolean isBlackTurn = board.isBlackTurn();
          boolean hasWhiteMoves = isWhiteTurn && hasFirstMoveInDrive;
          boolean hasBlackMoves = isBlackTurn && hasSecondMoveInDrive;
          boolean isInUndo = board.getNextBoards().size() > 0;
          if (hasWhiteMoves ||
              hasBlackMoves ||
              isInUndo && isNotEditMode(boardBox)) {
            return null;
          }
          BoardUtils.updateMoveSquaresHighlightAndDraught(boardUpdated, board);
          Square nextSquare = boardUpdated.getNextSquare();
          Square selectedSquare = boardUpdated.getSelectedSquare();
          if (isValidMove(nextSquare, selectedSquare)) {
            logger.error(String.format("Invalid move Next: %s, Selected: %s", nextSquare, selectedSquare));
            return null;
          }
          NotationHistory notationBoardBox = updatedBox.getNotation().getNotationHistory();
          boardUpdated.setDriveCount(notationBoardBox.size() - 1);

          try {
            boardUpdated = boardService.move(selectedSquare, nextSquare, boardUpdated, notationBoardBox);
          } catch (BoardServiceException e) {
            logger.error("Error while moving", e);
            return null;
          }
          updatedBox.setBoard(boardUpdated);
          updatedBox.setBoardId(boardUpdated.getId());

//          updatedBox.getNotation().setNotationHistory(boardUpdated.getNotationHistory());
          logger.info("Notation after move: " + updatedBox.getNotation().getNotationHistory().pdnString());

          boardBoxDao.save(updatedBox);
          return updatedBox;
        });
  }

  public Optional<BoardBox> changeTurn(BoardBox boardBox) {
    return find(boardBox)
        .map(updatedBox -> {
          Board inverted = boardBox.getBoard();
          inverted.setSelectedSquare(null);
          inverted.setBlackTurn(!inverted.isBlackTurn());
          boardService.save(inverted);

          Board board = boardService.updateBoard(inverted);
          updatedBox.setBoard(board);
          boardBoxDao.save(updatedBox);
          return updatedBox;
        });
  }

  public  Optional<BoardBox> save(BoardBox boardBox) {
    return Optional.of(saveAndFillBoard(boardBox));
  }

  public Optional<BoardBox> loadPreviewBoard(BoardBox boardBox) {
    updateBoardBox(boardBox);
    Board noHighlight = boardService.resetHighlightAndUpdate(boardBox.getBoard());
    boardBox.setBoard(noHighlight);
    return Optional.of(boardBox);
  }

  public Optional<BoardBox> addDraught(BoardBox boardBox) {
    Square selectedSquare = boardBox.getBoard().getSelectedSquare();
    if (selectedSquare == null
        || !selectedSquare.isOccupied()
        || !boardBox.getEditMode().equals(EnumEditBoardBoxMode.PLACE)) {
      return Optional.empty();
    }
    Draught draught = selectedSquare.getDraught();
    return find(boardBox)
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
    return find(boardBox)
        .map(filledBoardBox -> {
          NotationHistory history = filledBoardBox.getNotation().getNotationHistory();
          NotationDrive forkToDrive = history.getLast().deepClone();
          return forkNotationFor(filledBoardBox, forkToDrive);
        })
        .orElse(null);
  }

  public Optional<BoardBox> redo(BoardBox boardBox) {
    return find(boardBox)
        .map(filledBoardBox -> {
          NotationHistory history = filledBoardBox.getNotation().getNotationHistory();
          NotationDrive switchToDrive = history.getLast().deepClone();
          return switchNotationTo(filledBoardBox, switchToDrive, null);
        })
        .orElse(null);
  }

  public Optional<BoardBox> forkNotation(BoardBox boardBox) {
    NotationDrive currentNotationDrive = boardBox.getNotation().getNotationHistory().getCurrentNotationDrive();
    return forkNotationFor(boardBox, currentNotationDrive);
  }

  public Optional<BoardBox> switchNotation(BoardBox boardBox) {
    NotationDrive currentNotationDrive = boardBox.getNotation().getNotationHistory().getCurrentNotationDrive();
    NotationDrive variantNotationDrive = boardBox.getNotation().getNotationHistory().getVariantNotationDrive();
    return switchNotationTo(boardBox, currentNotationDrive, variantNotationDrive);
  }

  private Optional<BoardBox> forkNotationFor(BoardBox boardBox, NotationDrive forkFromNotationDrive) {
    return find(boardBox)
        .map(bb -> forkNotationForVariants(bb, forkFromNotationDrive));
  }

  private Optional<BoardBox> switchNotationTo(BoardBox boardBox, NotationDrive currentNotationDrive, NotationDrive variantNotationDrive) {
    return boardBoxDao.find(boardBox)
        .map(bb -> switchNotationToVariant(bb, currentNotationDrive, variantNotationDrive));
  }

  private BoardBox switchNotationToVariant(BoardBox boardBox,
                                           NotationDrive currentNotationDrive,
                                           NotationDrive variantNotationDrive) {
    NotationHistory notationDrives = boardBox.getNotation().getNotationHistory();
    boolean success = notationDrives.switchTo(currentNotationDrive, variantNotationDrive);
    if (!success) {
      return null;
    }
    // switch to new board
    return notationDrives
        .getLastNotationBoardId()
        .map(boardId ->
            setBoardForBoardBox(boardBox, boardId))
        .orElse(null);
  }

  private BoardBox forkNotationForVariants(BoardBox boardBox, NotationDrive forkFromNotationDrive) {
    Notation notation = boardBox.getNotation();
    NotationHistory notationDrives = notation.getNotationHistory();
    boolean success = notationDrives.forkAt(forkFromNotationDrive);
    // switch to new board
    if (success) {
      return notationDrives
          .getLastNotationBoardId()
          .map(boardId -> setBoardForBoardBox(boardBox, boardId))
          .orElse(null);
    }
    return null;
  }

  private BoardBox setBoardForBoardBox(BoardBox boardBox, String boardId) {
    return boardDao.findByKey(boardId)
        .map(board -> {
          board = boardService.resetHighlightAndUpdate(board);
          boardDao.save(board);
          boardBox.setBoard(board);
          boardBox.setBoardId(board.getId());
          boardBoxDao.save(boardBox);
          return boardBox;
        })
        .orElse(null);
  }

  private boolean isNotEditMode(BoardBox boardBox) {
    return !boardBox.getEditMode().equals(EnumEditBoardBoxMode.EDIT);
  }

  private boolean isMoveMode(BoardBox boardBox) {
    return boardBox.getEditMode().equals(EnumEditBoardBoxMode.MOVE);
  }

  private boolean isValidMove(Square nextSquare, Square selectedSquare) {
    return nextSquare == null
        || selectedSquare == null
        || !selectedSquare.isOccupied()
        || !nextSquare.isHighlight();
  }

  private boolean resetHighlightIfNotLastBoard(BoardBox boardBox) {
    NotationDrives variants = boardBox.getNotation().getNotationHistory().getNotation();
    NotationMoves moves = variants.getLast().getMoves();
    boolean isMovesEmpty = moves.isEmpty();
    boolean isHighlightLastMove = !isMovesEmpty && !moves.getLast().getLastMoveBoardId().equals(boardBox.getBoardId());
    if (isHighlightLastMove) {
      Board noHighlight = boardService.resetHighlightAndUpdate(boardBox.getBoard());
      boardBox.setBoard(noHighlight);
      return true;
    }
    return false;
  }

  private BoardBox updateBoardBox(BoardBox boardBox) {
    Optional<Board> boardOptional = boardService.findById(boardBox.getBoardId());
    return boardOptional
        .map(board -> {
          boardBox.setBoard(board);
          return boardBox;
        })
        .orElse(null);
  }

  private BoardBox saveAndFillBoard(BoardBox boardBox) {
    boardBoxDao.save(boardBox);
    boardBox = updateBoardBox(boardBox);
    return boardBox;
  }

  private BoardBox saveInCacheAndFillBoard(BoardBox boardBox) {
    return null;
  }
}
