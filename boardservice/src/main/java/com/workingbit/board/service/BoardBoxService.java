package com.workingbit.board.service;

import com.workingbit.board.controller.util.BoardUtils;
import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.domain.impl.*;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumAuthority;
import com.workingbit.share.model.enumarable.EnumEditBoardBoxMode;
import com.workingbit.share.util.Utils;
import net.percederberg.grammatica.parser.ParserCreationException;
import net.percederberg.grammatica.parser.ParserLogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.workingbit.board.BoardEmbedded.*;

/**
 * Created by Aleksey Popryaduhin on 07:00 22/09/2017.
 */
public class BoardBoxService {

  private final Logger logger = LoggerFactory.getLogger(BoardBoxService.class);
  private final static BoardService boardService = new BoardService();
  private final static NotationService notationService = new NotationService();

  public Optional<BoardBox> createBoardBox(CreateBoardPayload createBoardPayload, AuthUser authUser) {
    Board board = boardService.createBoard(createBoardPayload);

    BoardBox boardBox = new BoardBox(board);
    String boardBoxId = createBoardPayload.getBoardBoxId();
    boardBox.setId(boardBoxId);
    String articleId = createBoardPayload.getArticleId();
    boardBox.setArticleId(articleId);
    String userId = createBoardPayload.getUserId();
    boardBox.setUserId(userId);
    boardBox.setCreatedAt(LocalDateTime.now());

    createNewNotation(boardBox, authUser);
    saveAndFillBoard(authUser, boardBox);

    return Optional.of(boardBox);
  }

  public Optional<BoardBox> parsePdn(ImportPdnPayload importPdnPayload, AuthUser authUser) {
    try {
      Notation notation = notationParserService.parse(importPdnPayload.getPdn());
      notation.setRules(importPdnPayload.getRules());
      Optional<BoardBox> boardBoxFromNotation = createBoardBoxFromNotation(importPdnPayload.getArticleId(), notation, authUser);
      return boardBoxFromNotation.map(boardBox ->
          boardBox.getNotation().getNotationHistory().getLastNotationBoardId()
              .map(s -> {
                boardBox.setBoardId(s);
                boardBoxDao.save(boardBox);
                return updateBoardBox(authUser, boardBox);
              })
              .orElseThrow(BoardServiceException::new)
      );
    } catch (ParserLogException | ParserCreationException e) {
      logger.error("PARSE ERROR: " + e.getMessage(), e);
      throw new BoardServiceException(ErrorMessages.UNPARSABLE_PDN_CONTENT);
    }
  }

  Optional<BoardBox> createBoardBoxFromNotation(String articleId, Notation fromNotation, AuthUser authUser) {
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
          .map((b) -> saveAndFillBoard(authUser, b));
    }
    return Optional.empty();
  }

  Optional<BoardBox> find(BoardBox boardBox, AuthUser authUser) {
    Optional<BoardBox> board = findBoardAndPutInStore(authUser, boardBox);
    boolean secure = EnumAuthority.hasAuthorAuthorities(authUser);
    if (secure) {
      return board.map(b -> b.readonly(false))
          .map(bb -> updateBoardBox(authUser, bb));
    } else {
      return board.map(b -> b.readonly(true))
          .map(bb -> updateBoardBox(authUser, bb));
    }
  }

  public Optional<BoardBox> findById(String boardBoxId, AuthUser authUser) {
    BoardBox boardBoxResult = boardStoreService.get(authUser.getUserSession(), boardBoxId)
        .map(boardBox -> updateBoardBox(authUser, boardBox))
        .orElseGet(() ->
            boardBoxDao.findById(boardBoxId)
                .map(boardBox -> updateBoardBox(authUser, boardBox))
                .orElseThrow(BoardServiceException::new)
        );
    return Optional.of(boardBoxResult);
  }

  void delete(String boardBoxId) {
    boardBoxDao.findById(boardBoxId)
        .map(boardBox -> {
          boardService.delete(boardBox.getBoardId());
          boardBoxDao.delete(boardBox.getId());
          return null;
        });
  }

  public Optional<BoardBox> highlight(BoardBox boardBox, AuthUser authUser) {
    return find(boardBox, authUser)
        .map(updatedBoardBox -> {
              BoardBox userBoardBox = updatedBoardBox.deepClone();
              Board clientBoard = boardBox.getBoard();
              if (resetHighlightIfNotLastBoard(userBoardBox)) {
                Board noHighlight = boardService.resetHighlightAndUpdate(clientBoard);
                boardBox.setBoard(noHighlight);
                return boardBox;
              }
              Board currentBoard = userBoardBox.getBoard();
              Square selectedSquare = clientBoard.getSelectedSquare();
              if (!selectedSquare.isOccupied()) {
                return userBoardBox;
              }
              try {
                var highlight = boardService.getHighlight(currentBoard, clientBoard);
                currentBoard = (Board) highlight.get("serverBoard");
              } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return null;
              }
              userBoardBox.setBoard(currentBoard);
              boardStoreService.put(authUser.getUserSession(), userBoardBox);
              return userBoardBox;
            }
        );
  }

  public Optional<BoardBox> move(BoardBox boardBox, AuthUser authUser) {
    return find(boardBox, authUser)
        .map(updatedBoxOrig -> {
              BoardBox userBoardBox = updatedBoxOrig.deepClone();
              if (isNotEditMode(boardBox)) {
                return null;
              }

              Board serverBoard = userBoardBox.getBoard();
              Notation notation = userBoardBox.getNotation();
              NotationHistory notationBoardBox = notation.getNotationHistory();
              serverBoard.setDriveCount(notationBoardBox.size() - 1);
              try {
                Board clientBoard = boardBox.getBoard();
                serverBoard = boardService.move(serverBoard, clientBoard, notationBoardBox);
              } catch (BoardServiceException e) {
                logger.error("Error while moving", e);
                return null;
              }

              userBoardBox.setBoard(serverBoard);
              userBoardBox.setBoardId(serverBoard.getId());
              notationService.save(authUser, notation);

              logger.info("Notation after move: " + notation.getNotationHistory().pdnString());

              boardBoxDao.save(userBoardBox);
              return userBoardBox;
            }
        );
  }

  private void createNewNotation(BoardBox boardBox, AuthUser authUser) {
    Notation notation = new Notation();
    Utils.setRandomIdAndCreatedAt(notation);
    notation.setBoardBoxId(boardBox.getId());
    notation.setRules(boardBox.getBoard().getRules());
    boardBox.setNotationId(notation.getId());
    boardBox.setNotation(notation);
  }

  public Optional<BoardBox> changeTurn(BoardBox boardBox, AuthUser authUser) {
    return find(boardBox, authUser)
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

  public Optional<BoardBox> save(BoardBox boardBox, AuthUser authUser) {
    return Optional.of(saveAndFillBoard(authUser, boardBox));
  }

  public Optional<BoardBox> loadPreviewBoard(BoardBox boardBox, AuthUser authUser) {
    notationService.save(authUser, boardBox.getNotation());
    updateBoardBox(authUser, boardBox);
    Board noHighlight = boardService.resetHighlightAndUpdate(boardBox.getBoard());
    boardBox.setBoard(noHighlight);
    return Optional.of(boardBox);
  }

  public Optional<BoardBox> addDraught(BoardBox boardBox, AuthUser authUser) {
    Square selectedSquare = boardBox.getBoard().getSelectedSquare();
    if (selectedSquare == null
        || !selectedSquare.isOccupied()
        || !boardBox.getEditMode().equals(EnumEditBoardBoxMode.PLACE)) {
      return Optional.empty();
    }
    Draught draught = selectedSquare.getDraught();
    return find(boardBox, authUser)
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
          saveAndFillBoard(authUser, updated);
          return updated;
        });
  }

  public Optional<BoardBox> undo(BoardBox boardBox, AuthUser authUser) {
    return find(boardBox, authUser)
        .map(filledBoardBox -> {
          NotationHistory history = filledBoardBox.getNotation().getNotationHistory();
          NotationDrive forkToDrive = history.getLast().deepClone();
          return forkNotationFor(authUser, filledBoardBox, forkToDrive);
        })
        .orElse(null);
  }

  public Optional<BoardBox> redo(BoardBox boardBox, AuthUser authUser) {
    return
        find(boardBox, authUser)
            .map(filledBoardBox -> {
              NotationHistory history = filledBoardBox.getNotation().getNotationHistory();
              NotationDrive switchToDrive = history.getLast().deepClone();
              return switchNotationTo(authUser, filledBoardBox, switchToDrive, null);
            })
            .orElse(null);
  }

  public Optional<BoardBox> forkNotation(BoardBox boardBox, AuthUser authUser) {
    NotationDrive currentNotationDrive = boardBox.getNotation().getNotationHistory().getCurrentNotationDrive();
    return forkNotationFor(authUser, boardBox, currentNotationDrive);
  }

  public Optional<BoardBox> viewBranch(BoardBox boardBox, AuthUser token) {
    return switchNotation(boardBox, token);
  }

  public Optional<BoardBox> switchNotation(BoardBox boardBox, AuthUser authUser) {
    NotationDrive currentNotationDrive = boardBox.getNotation().getNotationHistory().getCurrentNotationDrive();
    NotationDrive variantNotationDrive = boardBox.getNotation().getNotationHistory().getVariantNotationDrive();
    return switchNotationTo(authUser, boardBox, currentNotationDrive, variantNotationDrive);
  }

  public Optional<BoardBoxes> findByIds(DomainIds boardIds, AuthUser authUser) {
    List<BoardBox> byUserAndIds = boardBoxDao.findByIds(boardIds);
    return Optional.of(BoardBoxes.create(byUserAndIds
        .stream()
        .map(boardBox -> updateBoardBox(authUser, boardBox))
        .collect(Collectors.toList())));
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
    boardBoxDao.save(boardBox);
    Board board = boardBox.getBoard();
    board.setBoardBoxId(boardBox.getId());
    boardService.save(board);
    Notation notation = boardBox.getNotation();
    notationService.save(authUser, notation);
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
    boolean secure = EnumAuthority.hasAuthorAuthorities(authUser);
    if (secure) {
      return Optional.ofNullable(fromDb.get());
    } else {
      return Optional.of(fromStore.orElseGet(fromDb));
    }
  }
}
