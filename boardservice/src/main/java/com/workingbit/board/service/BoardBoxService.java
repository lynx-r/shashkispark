package com.workingbit.board.service;

import com.workingbit.board.controller.util.BoardUtils;
import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.share.domain.impl.*;
import com.workingbit.share.model.*;
import com.workingbit.share.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.workingbit.board.BoardApplication.*;

/**
 * Created by Aleksey Popryaduhin on 07:00 22/09/2017.
 */
public class BoardBoxService {

  private final Logger logger = LoggerFactory.getLogger(BoardBoxService.class);
  private final static BoardService boardService = new BoardService();
  private final static NotationService notationService = new NotationService();

  public Optional<BoardBox> createBoardBox(CreateBoardPayload createBoardPayload) {
    Board board = boardService.createBoard(createBoardPayload);

    BoardBox boardBox = new BoardBox(board);
    String articleId = createBoardPayload.getArticleId();
    boardBox.setArticleId(articleId);
    Utils.setRandomIdAndCreatedAt(boardBox);

    createNewNotation(boardBox, board);
    saveAndFillBoard(boardBox);

    board.setBoardBoxId(boardBox.getId());
    boardService.save(board);
    return Optional.of(boardBox);
  }

  Optional<BoardBox> createBoardBoxFromNotation(String articleId, Notation fromNotation) {
    BoardBox boardBox = new BoardBox();
    boardBox.setArticleId(articleId);
    Utils.setRandomIdAndCreatedAt(boardBox);

    NotationHistory notationHistory = new NotationHistory();
    boardService.createBoardFromNotation(fromNotation.getNotationHistory(),
        notationHistory, fromNotation.getRules());
    fromNotation.setNotationHistory(notationHistory);

    // switch boardBox to the first board
    if (notationHistory.size() > 1
        && !notationHistory.get(1).getMoves().isEmpty()) {
      String firstBoardId = notationHistory.get(1).getMoves().getFirst().getMove().getFirst().getBoardId();
      return boardDao.findById(firstBoardId)
          .map(firstBoard -> {
            Utils.setRandomIdAndCreatedAt(fromNotation);
            boardBox.setNotationId(fromNotation.getId());
            boardBox.setNotation(fromNotation.deepClone());
            boardBox.setBoardId(firstBoardId);
            boardBox.setBoard(firstBoard);
            return boardBox;
          })
          .map(this::saveAndFillBoard);
    }
    return Optional.empty();
  }

  Optional<BoardBox> find(BoardBox boardBox) {
    return boardBoxDao.find(boardBox)
        .map(this::updateBoardBox);
  }

  public Optional<BoardBox> findById(String boardBoxId) {
    return boardBoxDao.findById(boardBoxId)
        .map(this::updateBoardBox);
  }

  void delete(String boardBoxId) {
    boardBoxDao.findById(boardBoxId)
        .map(boardBox -> {
          boardService.delete(boardBox.getBoardId());
          boardBoxDao.delete(boardBox.getId());
          return null;
        });
  }

  public Optional<BoardBox> highlight(BoardBox boardBox) {
    return find(boardBox)
        .map(updated -> {
          if (resetHighlightIfNotLastBoard(updated)) {
            Board noHighlight = boardService.resetHighlightAndUpdate(boardBox.getBoard());
            boardBox.setBoard(noHighlight);
            return boardBox;
          }
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
          if (isNotEditMode(boardBox)) {
            return null;
          }
          boardUpdated.setSelectedSquare(board.getSelectedSquare());
          boardUpdated.setNextSquare(board.getNextSquare());

          Notation notation = updatedBox.getNotation();
          NotationHistory notationBoardBox = notation.getNotationHistory();
          boardUpdated.setDriveCount(notationBoardBox.size() - 1);

          try {
            boardUpdated = boardService.move(boardUpdated, notationBoardBox);
          } catch (BoardServiceException e) {
            logger.error("Error while moving", e);
            return null;
          }
          updatedBox.setBoard(boardUpdated);
          updatedBox.setBoardId(boardUpdated.getId());
          notationService.save(notation);

          logger.info("Notation after move: " + notation.getNotationHistory().pdnString());

          boardBoxDao.save(updatedBox);
          return updatedBox;
        });
  }

  private void createNewNotation(BoardBox boardBox, Board board) {
    Notation notation = new Notation();
    Utils.setRandomIdAndCreatedAt(notation);
    notation.getNotationHistory().getLast().setSelected(true);
    notation.setBoardBoxId(boardBox.getId());
    notation.setRules(board.getRules());
    boardBox.setNotationId(notation.getId());
    boardBox.setNotation(notation);
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
    notationService.save(boardBox.getNotation());
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
            currentBoard = boardService.addDraught(currentBoard, squareLink.getNotation(), draught);
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
    return boardBoxService.find(boardBox)
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
            saveBoardBoxAfterSwitchFork(boardBox, boardId))
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
          .map(boardId -> saveBoardBoxAfterSwitchFork(boardBox, boardId))
          .orElse(null);
    }
    return null;
  }

  private BoardBox saveBoardBoxAfterSwitchFork(BoardBox boardBox, String boardId) {
    return boardDao.findById(boardId)
        .map(board -> {
          board = boardService.resetHighlightAndUpdate(board);
          boardDao.save(board);
          boardBox.setBoard(board);
          boardBox.setBoardId(board.getId());
          boardBoxDao.save(boardBox);
          notationService.save(boardBox.getNotation());
          return boardBox;
        })
        .orElse(null);
  }

  private boolean isNotEditMode(BoardBox boardBox) {
    return !boardBox.getEditMode().equals(EnumEditBoardBoxMode.EDIT);
  }

  private boolean resetHighlightIfNotLastBoard(BoardBox boardBox) {
    NotationDrives notationDrives = boardBox.getNotation().getNotationHistory().getNotation();
    boolean isLastSelected = notationDrives.getLast().isSelected();
    return !isLastSelected;
  }

  private BoardBox updateBoardBox(BoardBox boardBox) {
    Optional<Board> boardOptional = boardService.findById(boardBox.getBoardId());
    Optional<Notation> notationOptional = notationService.findById(boardBox.getNotationId());
    return boardOptional
        .map(board -> {
          boardBox.setBoard(board);
          notationOptional.ifPresent(notation -> {
            boardBox.setNotation(notation);
            boardBox.setNotationId(notation.getId());
          });
          return boardBox;
        })
        .orElse(null);
  }

  private BoardBox saveAndFillBoard(BoardBox boardBox) {
    boardBoxDao.save(boardBox);
    notationService.save(boardBox.getNotation());
    boardBox = updateBoardBox(boardBox);
    return boardBox;
  }
}
