package com.workingbit.board.service;

import com.workingbit.board.controller.util.BoardUtils;
import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.domain.impl.*;
import com.workingbit.share.model.*;
import com.workingbit.share.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import static com.workingbit.board.BoardEmbedded.*;

/**
 * Created by Aleksey Popryaduhin on 07:00 22/09/2017.
 */
public class BoardBoxService {

  private final Logger logger = LoggerFactory.getLogger(BoardBoxService.class);
  private final static BoardService boardService = new BoardService();
  private final static NotationService notationService = new NotationService();

  public Optional<BoardBox> createBoardBox(CreateBoardPayload createBoardPayload, Optional<AuthUser> token) {
    if (!token.isPresent()) {
      return Optional.empty();
    }
    AuthUser authUser = token.get();
    Board board = boardService.createBoard(createBoardPayload);

    BoardBox boardBox = new BoardBox(board);
    String boardBoxId = createBoardPayload.getBoardBoxId();
    boardBox.setId(boardBoxId);
    String articleId = createBoardPayload.getArticleId();
    boardBox.setArticleId(articleId);
    String userId = authUser.getUserId();
    boardBox.setUserId(userId);
    boardBox.setCreatedAt(LocalDateTime.now());

    createNewNotation(boardBox, board);
    saveAndFillBoard(authUser, boardBox);

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
          .map((b) -> saveAndFillBoard(null, b));
    }
    return Optional.empty();
  }

  Optional<BoardBox> find(BoardBox boardBox, AuthUser authUser) {
    Optional<BoardBox> board = findBoardAndPutInStore(authUser, boardBox);
    boolean secure = authUser.getRoles().containsAll(Arrays.asList(EnumSecureRole.ADMIN, EnumSecureRole.AUTHOR));
    if (secure) {
      return board.map(b -> b.readonly(false))
          .map(bb -> updateBoardBox(authUser, bb));
    } else {
      return board.map(b -> b.readonly(true))
          .map(bb -> updateBoardBox(authUser, bb));
    }
  }

  public Optional<BoardBox> findById(String boardBoxId, Optional<AuthUser> token) {
    return token.map(authUser ->
        boardStoreService.get(authUser.getUserSession(), boardBoxId)
            .map(boardBox -> updateBoardBox(authUser, boardBox))
            .orElseGet(() ->
                boardBoxDao.findById(boardBoxId)
                    .map(boardBox -> updateBoardBox(authUser, boardBox))
                    .orElseThrow(BoardServiceException::new)
            )
    );
  }

  void delete(String boardBoxId) {
    boardBoxDao.findById(boardBoxId)
        .map(boardBox -> {
          boardService.delete(boardBox.getBoardId());
          boardBoxDao.delete(boardBox.getId());
          return null;
        });
  }

  public Optional<BoardBox> highlight(BoardBox boardBox, Optional<AuthUser> token) {
    return token.map(authUser ->
        find(boardBox, authUser)
            .map(updatedBoardBox -> {
                  BoardBox userBoardBox = updatedBoardBox.deepClone();
                  if (resetHighlightIfNotLastBoard(userBoardBox)) {
                    Board noHighlight = boardService.resetHighlightAndUpdate(boardBox.getBoard());
                    boardBox.setBoard(noHighlight);
                    return boardBox;
                  }
                  Board currentBoard = userBoardBox.getBoard();
                  Square selectedSquare = boardBox.getBoard().getSelectedSquare();
                  if (!selectedSquare.isOccupied()) {
                    return userBoardBox;
                  }
                  BoardUtils.updateMoveSquaresHighlightAndDraught(currentBoard, boardBox.getBoard());
                  try {
                    currentBoard = boardService.highlight(currentBoard);
                  } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return null;
                  }
                  userBoardBox.setBoard(currentBoard);
                  boardStoreService.put(authUser.getUserSession(), userBoardBox);
                  return userBoardBox;
                }
            )
    ).orElse(null);
  }

  public Optional<BoardBox> move(BoardBox boardBox, Optional<AuthUser> token) {
    return token.map(authUser -> find(boardBox, authUser)
        .map(updatedBoxOrig -> {
              isOwn(authUser, boardBox);
              if (!isOwn(authUser, boardBox)) {
                logger.error(ErrorMessages.NOT_OWNER);
                return null;
              }
              BoardBox userBoardBox = updatedBoxOrig.deepClone();
              Board boardUpdated = userBoardBox.getBoard();
              Board board = boardBox.getBoard();
              if (isNotEditMode(boardBox)) {
                return null;
              }
              boardUpdated.setSelectedSquare(board.getSelectedSquare());
              boardUpdated.setNextSquare(board.getNextSquare());

              Notation notation = userBoardBox.getNotation();
              NotationHistory notationBoardBox = notation.getNotationHistory();
              boardUpdated.setDriveCount(notationBoardBox.size() - 1);

              try {
                boardUpdated = boardService.move(boardUpdated, notationBoardBox);
              } catch (BoardServiceException e) {
                logger.error("Error while moving", e);
                return null;
              }
              userBoardBox.setBoard(boardUpdated);
              userBoardBox.setBoardId(boardUpdated.getId());
              notationService.save(authUser, notation);

              logger.info("Notation after move: " + notation.getNotationHistory().pdnString());

              boardBoxDao.save(userBoardBox);
              return userBoardBox;
            }
        )
    ).orElse(null);
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

  public Optional<BoardBox> changeTurn(BoardBox boardBox, Optional<AuthUser> token) {
    return token.map(authUser ->
        find(boardBox, authUser)
            .map(updatedBox -> {
              if (!isOwn(authUser, boardBox)) {
                logger.error(ErrorMessages.NOT_OWNER);
                return null;
              }

              Board inverted = boardBox.getBoard();
              inverted.setSelectedSquare(null);
              inverted.setBlackTurn(!inverted.isBlackTurn());
              boardService.save(inverted);

              Board board = boardService.updateBoard(inverted);
              updatedBox.setBoard(board);
              boardBoxDao.save(updatedBox);
              return updatedBox;
            })
    ).orElse(null);
  }

  public Optional<BoardBox> save(BoardBox boardBox, Optional<AuthUser> token) {
    return token.map(authUser -> saveAndFillBoard(authUser, boardBox));
  }

  public Optional<BoardBox> loadPreviewBoard(BoardBox boardBox, Optional<AuthUser> token) {
    return token.map(authUser -> {
      notationService.save(authUser, boardBox.getNotation());
      updateBoardBox(authUser, boardBox);
      Board noHighlight = boardService.resetHighlightAndUpdate(boardBox.getBoard());
      boardBox.setBoard(noHighlight);
      return boardBox;
    });
  }

  public Optional<BoardBox> addDraught(BoardBox boardBox, Optional<AuthUser> token) {
    Square selectedSquare = boardBox.getBoard().getSelectedSquare();
    if (selectedSquare == null
        || !selectedSquare.isOccupied()
        || !boardBox.getEditMode().equals(EnumEditBoardBoxMode.PLACE)) {
      return Optional.empty();
    }
    Draught draught = selectedSquare.getDraught();
    return token.map(authUser -> find(boardBox, authUser)
        .map(updated -> {
          if (!isOwn(authUser, boardBox)) {
            logger.error(ErrorMessages.NOT_OWNER);
            return null;
          }

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
          saveAndFillBoard(authUser, updated);
          return updated;
        })
    ).orElse(null);
  }

  public Optional<BoardBox> undo(BoardBox boardBox, Optional<AuthUser> token) {
    return token.map(authUser -> find(boardBox, authUser)
        .map(filledBoardBox -> {
          NotationHistory history = filledBoardBox.getNotation().getNotationHistory();
          NotationDrive forkToDrive = history.getLast().deepClone();
          return forkNotationFor(authUser, filledBoardBox, forkToDrive);
        })
        .orElse(null)
    ).orElse(null);
  }

  public Optional<BoardBox> redo(BoardBox boardBox, Optional<AuthUser> token) {
    return token.map(authUser ->
        find(boardBox, authUser)
            .map(filledBoardBox -> {
              NotationHistory history = filledBoardBox.getNotation().getNotationHistory();
              NotationDrive switchToDrive = history.getLast().deepClone();
              return switchNotationTo(authUser, filledBoardBox, switchToDrive, null);
            })
            .orElse(null)
    ).orElse(null);
  }

  public Optional<BoardBox> forkNotation(BoardBox boardBox, Optional<AuthUser> token) {
    NotationDrive currentNotationDrive = boardBox.getNotation().getNotationHistory().getCurrentNotationDrive();
    return token.map(authUser -> forkNotationFor(authUser, boardBox, currentNotationDrive))
        .orElse(null);
  }

  public Optional<BoardBox> viewBranch(BoardBox boardBox, Optional<AuthUser> token) {
    return switchNotation(boardBox, token);
  }

  public Optional<BoardBox> switchNotation(BoardBox boardBox, Optional<AuthUser> token) {
    return token
        .map(authUser -> {
              NotationDrive currentNotationDrive = boardBox.getNotation().getNotationHistory().getCurrentNotationDrive();
              NotationDrive variantNotationDrive = boardBox.getNotation().getNotationHistory().getVariantNotationDrive();
              return switchNotationTo(authUser, boardBox, currentNotationDrive, variantNotationDrive);
            }
        )
        .orElse(null);
  }

  private Optional<BoardBox> forkNotationFor(AuthUser token, BoardBox boardBox, NotationDrive forkFromNotationDrive) {
    return find(boardBox, token)
        .map(bb -> forkNotationForVariants(token, bb, forkFromNotationDrive));
  }

  private Optional<BoardBox> switchNotationTo(AuthUser authUser, BoardBox boardBox, NotationDrive currentNotationDrive, NotationDrive variantNotationDrive) {
    return find(boardBox, authUser)
        .map(bb -> switchNotationToVariant(authUser, bb, currentNotationDrive, variantNotationDrive));
  }

  private BoardBox switchNotationToVariant(AuthUser authUser, BoardBox boardBox,
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
            saveBoardBoxAfterSwitchFork(authUser, boardBox, boardId))
        .orElse(null);
  }

  private BoardBox forkNotationForVariants(AuthUser token, BoardBox boardBox, NotationDrive forkFromNotationDrive) {
    Notation notation = boardBox.getNotation();
    NotationHistory notationDrives = notation.getNotationHistory();
    boolean success = notationDrives.forkAt(forkFromNotationDrive);
    // switch to new board
    if (success) {
      return notationDrives
          .getLastNotationBoardId()
          .map(boardId -> saveBoardBoxAfterSwitchFork(token, boardBox, boardId))
          .orElse(null);
    }
    return null;
  }

  private BoardBox saveBoardBoxAfterSwitchFork(AuthUser authUser, BoardBox boardBox, String boardId) {
    return boardDao.findById(boardId)
        .map(board -> {
          board = boardService.resetHighlightAndUpdate(board);
          boardDao.save(board);
          boardBox.setBoard(board);
          boardBox.setBoardId(board.getId());
          if (boardBox.isReadonly() && authUser != null) {
            boardStoreService.put(authUser.getUserSession(), boardBox);
          } else {
            if (!isOwn(authUser, boardBox)) {
              logger.error(ErrorMessages.NOT_OWNER);
              return null;
            }
            boardBoxDao.save(boardBox);
          }
          notationService.save(authUser, boardBox.getNotation());
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

  private BoardBox updateBoardBox(AuthUser authUser, BoardBox boardBox) {
    Optional<Board> boardOptional = boardService.findById(boardBox.getBoardId());
    Optional<Notation> notationOptional = notationService.findById(authUser, boardBox.getNotationId());
    return boardOptional
        .map(board -> {
          board.setReadonly(boardBox.isReadonly());
          boardBox.setBoard(board);
          notationOptional.ifPresent(notation -> {
            boardBox.setNotation(notation);
            boardBox.setNotationId(notation.getId());
          });
          boardStoreService.put(authUser.getUserSession(), boardBox);
          return boardBox;
        })
        .orElse(null);
  }

  private BoardBox saveAndFillBoard(AuthUser authUser, BoardBox boardBox) {
    if (!isOwn(authUser, boardBox)) {
      logger.error(ErrorMessages.NOT_OWNER);
      return null;
    }

    boardBox.setReadonly(false);
    boardBoxDao.save(boardBox);
    notationService.save(authUser, boardBox.getNotation());
    boardBox = updateBoardBox(authUser, boardBox);
    return boardBox;
  }

  private Optional<BoardBox> findBoardAndPutInStore(AuthUser authUser, BoardBox boardBox) {
    Optional<BoardBox> fromStore = boardStoreService.get(authUser.getUserSession(), boardBox.getId());
    Supplier<BoardBox> fromDb = () -> {
      Optional<BoardBox> boardBoxOptional = boardBoxDao.find(boardBox);
      if (boardBoxOptional.isPresent()) {
        boardStoreService.put(authUser.getUserSession(), boardBoxOptional.get());
        return boardBoxOptional.get();
      }
      return null;
    };
    boolean secure = authUser.getRoles().containsAll(Arrays.asList(EnumSecureRole.ADMIN, EnumSecureRole.AUTHOR));
    if (secure) {
      return Optional.ofNullable(fromDb.get());
    } else {
      return Optional.of(fromStore.orElseGet(fromDb));
    }
  }

  private boolean isOwn(AuthUser authUser, BoardBox boardBox) {
    // authUser == null when board is created from notation pdn
    return authUser == null || authUser.getUserId().equals(boardBox.getUserId());
  }
}
