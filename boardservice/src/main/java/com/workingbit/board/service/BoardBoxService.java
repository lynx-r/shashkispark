package com.workingbit.board.service;

import com.workingbit.board.controller.util.BoardUtils;
import com.workingbit.board.exception.BoardServiceException;
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

  public Optional<BoardBox> createBoardBoxFromNotation(String articleId, String boardBoxId, Notation fromNotation) {
    BoardBox boardBox = new BoardBox();
    boardBox.setArticleId(articleId);
    Utils.setBoardBoxIdAndCreatedAt(boardBox, articleId, boardBoxId);

    Board board = boardService.createBoardFromNotation(fromNotation, articleId, boardBoxId);
    Notation notation = new Notation(fromNotation.getTags(), fromNotation.getRules(), board.getNotationDrives());
    boardBox.setNotation(notation);

    LinkedList<BoardIdNotation> previousBoards = board.getPreviousBoards();
    String firstBoardId = previousBoards.getLast().getBoardId();
    return boardDao.findByKey(firstBoardId)
        .map(firstBoard -> {
          String f_boardId = firstBoard.getId();
          firstBoard.setNotationDrives(notation.getNotationDrives());
          boardDao.save(firstBoard);
          boardBox.setBoardId(f_boardId);
          boardBox.setBoard(firstBoard);
          return boardBox;
        })
        .map(this::saveAndFillBoard)
        .orElseThrow(BoardServiceException::new);
  }

  public Optional<BoardBox> find(BoardBox boardBox) {
    return boardBoxDao.find(boardBox)
        .map(this::updateBoardBox);
  }

  public Optional<BoardBox> findById(String boardBoxId) {
    return boardBoxDao.findByKey(boardBoxId)
        .map(this::updateBoardBox);
  }

  private BoardBox updateBoardBox(BoardBox boardBox) {
    Optional<Board> boardOptional = boardService.findById(boardBox.getBoardId());
    return boardOptional.map(board -> {
      boardBox.setBoard(board);
      updateVariantsInBoard(boardBox, board);
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
//          updateVariantsInBoard(updated, currentBoard);
          return updated;
        });
  }

  public Optional<BoardBox> move(BoardBox boardBox) {
    return find(boardBox)
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
          String articleId = boardBox.getArticleId();
          NotationDrives notationDrives = boardBox.getNotation().getNotationDrives();
          boardUpdated = boardService.move(selectedSquare, nextSquare, boardUpdated, articleId, notationDrives);
          updatedBox.setBoard(boardUpdated);
          updatedBox.setBoardId(boardUpdated.getId());

          updateVariantsInBoard(updatedBox, boardUpdated);
          boardBoxDao.save(updatedBox);
          return updatedBox;
        });
  }

  private void updateVariantsInBoard(BoardBox updatedBox, Board boardUpdated) {
//    NotationDrives notationDrives = updatedBox.getNotation().getNotationDrives();
//    NotationDrives boardNotationDrives = boardUpdated.getNotationDrives();
//    boolean isAtStartStroke = notationDrives.size() == 1 && !boardNotationDrives.isEmpty();
//    if (notationDrives.size() > 1) {
//      NotationDrive lastDrive = notationDrives.getLast();
//      lastDrive.getVariants().forEach(move -> move.getMoves().forEach(m -> m.setCursor(false)));
//      boardNotationDrives
//          .stream()
//          .filter(lastDrive::equals)
//          .findFirst()
//          .ifPresent(drive -> drive.setVariants(lastDrive.getVariants()));
//    } else if (isAtStartStroke) {
//      NotationDrive lastNotation = notationDrives.getLast();
//      NotationDrive lastBoardStroke = boardNotationDrives.getLast();
//      boolean isAddedStrokeInBoard = lastBoardStroke.getNotationNumberInt() > notationDrives.size();
//      if (isAddedStrokeInBoard) { // update variants on prev board stroke
//        boardNotationDrives.get(boardNotationDrives.size() - 2).setVariants(lastNotation.getVariants());
//      } else { // update variants on last stroke in board
//        boardNotationDrives.getLast().setVariants(lastNotation.getVariants());
//      }
//    }
//    if (!boardNotationDrives.isEmpty()) {
//      boolean isFirstDrivesNumberMoreThenSecond = boardNotationDrives.size() >= 2
//          && boardNotationDrives.getFirst().getNotationNumberInt() > boardNotationDrives.getLast().getNotationNumberInt();
//      if (isFirstDrivesNumberMoreThenSecond) {
//        Collections.reverse(boardNotationDrives);
//      }
//      updatedBox.getNotation().setNotationDrives(boardNotationDrives);
//    }
  }

  public Optional<BoardBox> makeWhiteStroke(BoardBox boardBox) {
    return find(boardBox)
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
//    boardBoxDao.save(boardBox);
    return Optional.of(boardBox);
  }

  public Optional<BoardBox> loadBoard(BoardBox boardBox) {
    Board board = boardBox.getBoard();
    updateVariantsInBoard(boardBox, board);
    boardBoxDao.save(boardBox);
    updateBoardBox(boardBox);
    return Optional.of(boardBox);
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
          undone.ifPresent((b) -> undoRedoBoardActionAndSave(updated, b));
          return updated;
        });
  }

  public Optional<BoardBox> forkBoardBox(BoardBox boardBox, NotationDrive forkFromNotationDrive) {
    return find(boardBox)
        .map(bb -> forkNotationForVariants(bb, forkFromNotationDrive))
        .map(this::saveAndFillBoard)
        .orElseThrow(BoardServiceException::new);
  }

  public Optional<BoardBox> switchToNotationDrive(BoardBox boardBox, NotationDrive switchToNotationDrive) {
    return find(boardBox)
        .map(bb -> switchNotationToVariant(bb, switchToNotationDrive))
        .map(this::saveAndFillBoard)
        .orElseThrow(BoardServiceException::new);
  }

  private BoardBox switchNotationToVariant(BoardBox boardBox, NotationDrive switchToNotationDrive) {
    Notation notation = boardBox.getNotation();
    NotationDrives notationDrives = notation.getNotationDrives();
    NotationDrives lastVariants = notationDrives.getLast().getVariants();

    // fork current notation
    int indexFork = notationDrives.indexOf(switchToNotationDrive);
    List<NotationDrive> forked = notationDrives.subList(indexFork, notationDrives.size());
    NotationDrives forkedNotationDrives;
    // add current notation drive after indexFork to variants
    if (forked.size() > 1) {
      forkedNotationDrives = NotationDrives.Builder.getInstance()
          .addAll(forked)
          .build();

      notationDrives.removeAll(forkedNotationDrives);

      NotationDrive variant = forkedNotationDrives.getFirst().deepClone();
      variant.setVariants(forkedNotationDrives);
      lastVariants.add(variant);
    }

    // find drive to switch
    Optional<NotationDrive> variantToSwitch = lastVariants
        .stream()
        .filter(nd -> nd.getMoves().equals(switchToNotationDrive.getVariants().get(0).getMoves()))
        .findFirst();

    // add its variants to main notation drives
    variantToSwitch.ifPresent(v-> notationDrives.addAll(v.getVariants()));

    return boardBox;
  }

  private BoardBox forkNotationForVariants(BoardBox boardBox, NotationDrive forkFromNotationDrive) {
    Notation notation = boardBox.getNotation();
    NotationDrives notationDrives = notation.getNotationDrives();

    int indexFork = notationDrives.indexOf(forkFromNotationDrive);
    List<NotationDrive> forked = notationDrives.subList(indexFork, notationDrives.size());
    NotationDrives forkedNotationDrives = NotationDrives.Builder.getInstance()
        .addAll(forked)
        .build();

    notationDrives.removeAll(forkedNotationDrives);

    NotationDrive variant = forkedNotationDrives.getFirst().deepClone();
    variant.setVariants(forkedNotationDrives);
    variant.setFork(true);

    notationDrives.getLast().getVariants().add(variant);
    return boardBox;
  }

  public Optional<BoardBox> redo(BoardBox boardBox) {
    return find(boardBox)
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
            updateVariantsInBoard(updated, b);
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
