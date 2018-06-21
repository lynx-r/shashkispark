package com.workingbit.board.service;

import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.domain.impl.*;
import com.workingbit.share.exception.DaoException;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumAuthority;
import com.workingbit.share.model.enumarable.EnumEditBoardBoxMode;
import com.workingbit.share.util.Utils;
import net.percederberg.grammatica.parser.ParserCreationException;
import net.percederberg.grammatica.parser.ParserLogException;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.workingbit.board.BoardEmbedded.*;
import static com.workingbit.board.controller.util.BoardUtils.findSquareByLink;
import static com.workingbit.share.common.RequestConstants.PUBLIC_QUERY;
import static java.lang.String.format;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

/**
 * Created by Aleksey Popryaduhin on 07:00 22/09/2017.
 */
public class BoardBoxService {

  private final Logger logger = LoggerFactory.getLogger(BoardBoxService.class);

  @NotNull
  public BoardBox createBoardBox(@NotNull CreateBoardPayload createBoardPayload, @NotNull AuthUser authUser) {
    Board board = boardService.createBoard(createBoardPayload);

    BoardBox boardBox = new BoardBox(board);
    DomainId boardBoxId = createBoardPayload.getBoardBoxId();
    boardBox.setDomainId(boardBoxId);
    DomainId articleId = createBoardPayload.getArticleId();
    boardBox.setArticleId(articleId);
    DomainId userId = createBoardPayload.getUserId();
    boardBox.setUserId(userId);
    boardBox.setIdInArticle(createBoardPayload.getIdInArticle());
    boardBox.setEditMode(createBoardPayload.getEditMode());

    notationService.createNotationForBoardBox(boardBox);
    saveAndFillBoard(boardBox, authUser);
    return boardBox;
  }

  public BoardBox parsePdn(@NotNull ImportPdnPayload importPdnPayload, @NotNull AuthUser authUser) {
    try {
      Notation notation = notationParserService.parse(importPdnPayload.getPdn());
      notation.setRules(importPdnPayload.getRules());
      return createBoardBoxFromNotation(importPdnPayload.getArticleId(), importPdnPayload.getIdInArticle(), notation, authUser);
    } catch (@NotNull ParserLogException | ParserCreationException e) {
      logger.error("PARSE ERROR: " + e.getMessage(), e);
      throw RequestException.internalServerError(ErrorMessages.UNPARSABLE_PDN_CONTENT, e.getMessage());
    } catch (BoardServiceException be) {
      logger.error("PARSE ERROR: " + be.getMessage(), be);
      throw RequestException.internalServerError(be.getMessage());
    }
  }

  BoardBox createBoardBoxFromNotation(DomainId articleId, int idInArticle, @NotNull Notation parsedNotation, @NotNull AuthUser authUser) {
    BoardBox boardBox = new BoardBox();
    boardBox.setArticleId(articleId);
    boardBox.setUserId(authUser.getUserId());
    boardBox.setIdInArticle(idInArticle);
    Utils.setRandomIdAndCreatedAt(boardBox);

    Utils.setRandomIdAndCreatedAt(parsedNotation);
    boardService.fillNotation(boardBox.getDomainId(), parsedNotation.getNotationFen(), parsedNotation.getDomainId(),
        parsedNotation, parsedNotation.getRules());
    NotationHistory filledNotationHistory = parsedNotation.getNotationHistory();
    // switch boardBox to the first board
    return filledNotationHistory.getLastNotationBoardId()
        .map(firstBoardId -> {
          var firstBoard = boardDao.findById(firstBoardId);
          boardService.updateBoard(firstBoard);

          boardBox.setNotationId(parsedNotation.getDomainId());
          boardBox.setNotation(parsedNotation);
          boardBox.setBoardId(firstBoardId);
          boardBox.setBoard(firstBoard);
          boardBoxDao.save(boardBox);
          return boardBox;
        })
        .orElseThrow(() -> RequestException.internalServerError(ErrorMessages.UNABLE_TO_PARSE_PDN));
  }

  @NotNull
  BoardBox findAndFill(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
    BoardBox board = findBoardAndPutInStore(boardBox, authUser);
    return updateBoardBox(board, authUser);
  }

  public BoardBox findById(@NotNull DomainId boardBoxId, @NotNull AuthUser authUser, @NotNull String publicQuery) {
    if (publicQuery.equals(PUBLIC_QUERY)) {
      return findPublicById(boardBoxId, authUser);
    }
    if (!EnumAuthority.hasAuthorAuthorities(authUser)) {
      throw RequestException.forbidden();
    }
    return boardBoxStoreService.get(authUser.getUserSession(), boardBoxId)
        .map(boardBox -> updateBoardBox(boardBox, authUser))
        .orElseGet(() -> {
          var boardBox = boardBoxDao.findById(boardBoxId);
          updateBoardBox(boardBox, authUser);
          return boardBox;
        });
  }

  BoardBox findPublicById(@NotNull DomainId boardBoxId, @NotNull AuthUser authUser) {
    return boardBoxStoreService
        .get(authUser.getUserSession(), boardBoxId)
        .orElseGet(() -> {
          BoardBox publicById = boardBoxDao.findPublicById(boardBoxId);
          publicById = updatePublicBoardBox(authUser, publicById);
          boardBoxStoreService.put(authUser.getUserSession(), publicById);
          return publicById;
        });
  }

  public BoardBoxes findByArticleId(@NotNull DomainId articleId, @NotNull String queryValue, @NotNull AuthUser authUser) {
    if (queryValue.equals(PUBLIC_QUERY)) {
      return findPublicByArticleId(articleId, authUser);
    }
    if (!EnumAuthority.hasAuthorAuthorities(authUser)) {
      throw RequestException.forbidden();
    }
    try {
      BoardBoxes byUserAndIds = boardBoxDao.findByArticleId(articleId);
      return fillBoardBoxes(articleId, byUserAndIds, authUser);
    } catch (DaoException e) {
      logger.warn(e.getMessage());
      throw RequestException.noContent();
    }
  }

  private BoardBoxes findPublicByArticleId(@NotNull DomainId articleId, AuthUser authUser) {
    return boardBoxStoreService
        .getByArticleId(authUser.getUserSession(), articleId)
        .orElseGet(() -> {
          BoardBoxes boardBoxes = boardBoxDao.findPublicByArticleId(articleId);
          return fillBoardBoxes(articleId, boardBoxes, authUser);
        });
  }

  public BoardBox deleteBoardBox(DomainId boardBoxId, AuthUser authUser) {
    var boardBox = boardBoxDao.findById(boardBoxId);
    if (boardBox == null) {
      throw RequestException.notFound404();
    }
    boardDao.deleteByBoardBoxId(boardBoxId);
    notationHistoryService.deleteByNotationId(boardBox.getNotationId());
    notationService.deleteById(boardBox.getNotationId());
    boardBoxDao.delete(boardBox.getDomainId());
    boardBoxStoreService.remove(boardBox);
    DomainId articleId = boardBox.getArticleId();
    try {
      var byArticleId = boardBoxDao.findByArticleId(articleId);
      ListOrderedMap<String, BoardBox> boardBoxes = byArticleId.getBoardBoxes();
      BoardBoxes updatedBB = new BoardBoxes();
      List<BoardBox> valueList = boardBoxes.valueList();
      valueList.sort(Comparator.comparingInt(BoardBox::getIdInArticle));
      int j = 1;
      for (int i = 0; i < valueList.size(); i++) {
        BoardBox curBB = valueList.get(i);
        curBB.setIdInArticle(i + 1);
        if (curBB.isTask()) {
          curBB.setTaskIdInArticle(j);
          j++;
        }
        updatedBB.push(curBB);
      }
      boardBoxDao.batchSave(updatedBB.valueList());
      return updateBoardBox(updatedBB.getBoardBoxes().valueList().get(0), authUser);
    } catch (DaoException e) {
      return new BoardBox();
    }
  }

  public BoardBox highlight(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
    var serverBoardBox = findAndFill(boardBox, authUser);
    Board clientBoard = boardBox.getBoard();
    if (resetHighlightIfNotLastBoard(serverBoardBox)) {
      Board noHighlight = boardService.resetHighlightAndUpdate(clientBoard);
      boardBox.setBoard(noHighlight);
      return boardBox;
    }
    Board currentBoard = serverBoardBox.getBoard();
    Square selectedSquare = clientBoard.getSelectedSquare();
    if (!selectedSquare.isOccupied()) {
      return serverBoardBox;
    }
    try {
      var highlight = boardService.getHighlight(currentBoard, clientBoard);
      currentBoard = (Board) highlight.get("serverBoard");
    } catch (BoardServiceException e) {
      logger.error(e.getMessage());
      throw RequestException.badRequest(e.getMessage());
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw RequestException.badRequest();
    }
    serverBoardBox.setBoard(currentBoard);
    boardBoxStoreService.put(authUser.getUserSession(), serverBoardBox);
    return serverBoardBox;
  }

  @Nullable
  public BoardBox moveSmart(@NotNull BoardBox boardBox, @NotNull AuthUser token) {
    Board board = boardBox.getBoard();
    Square nextSquare = board.getNextSquare();
    if (nextSquare.isOccupied()) {
      return null;
    }
    Square selectedSquare = board.getSelectedSquare();
    if (selectedSquare != null) {
      boolean isSelectedSquareTurn = selectedSquare.getDraught().isBlack() == board.isBlackTurn();
      if (selectedSquare.isOccupied() && isSelectedSquareTurn) {
        return move(boardBox, token);
      }
      return null;
    }
    boardService.updateBoard(board);
    selectedSquare = boardService.getPredictedSelectedSquare(board);
    board.setSelectedSquare(selectedSquare);
    boardDao.save(board);
    return move(boardBox, token);
  }

  @Nullable
  public BoardBox move(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
    Board clientBoard = boardBox.getBoard();
    boardService.updateAssigned(clientBoard);
    if (resetHighlightIfNotLastBoard(boardBox)) {
      throw RequestException.badRequest(ErrorMessages.UNABLE_TO_MOVE_WHEN_POINTER_NOT_LAST);
    }

    if (isNotEditMode(boardBox)) {
      return null;
    }

    Notation notation = boardBox.getNotation();
    NotationHistory notationHistory = notation.getNotationHistory();
    try {
      clientBoard = boardService.move(clientBoard, notationHistory);
    } catch (BoardServiceException e) {
      logger.error("Error while moving " + e.getMessage());
      throw RequestException.badRequest(e.getMessage());
    }

    boardBox.setBoard(clientBoard);
    boardBox.setBoardId(clientBoard.getDomainId());
    notationHistory.getLast().setNotationFormat(notation.getFormat());
    notationHistory.getLast().setBoardDimension(notation.getRules().getDimension());
    notation.addForkedNotationHistory(notationHistory);
    notationHistoryService.save(notationHistory);
    notationService.syncSubVariants(notationHistory, notation);
    notationStoreService.putNotation(authUser.getUserSession(), notation);

    logger.info(format("Notation after move: %s", notation.getNotationHistory().debugPdnString()));

    boardBoxDao.save(boardBox);
    boardBoxStoreService.put(authUser.getUserSession(), boardBox);
    return boardBox;
  }

  @NotNull
  public BoardBox changeTurn(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
    var updatedBox = findAndFill(boardBox, authUser);
    Board inverted = updatedBox.getBoard();
    Board clientBoard = boardBox.getBoard();
    inverted.setBlackTurn(!clientBoard.isBlackTurn());
    boardService.save(inverted);

    updatedBox.setBoard(inverted);
    boardBoxDao.save(updatedBox);

    boardService.updateBoard(inverted);
    return updatedBox;
  }

  @NotNull
  public BoardBox changeBoardColor(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
    var updatedBox = findAndFill(boardBox, authUser);
    Board inverted = updatedBox.getBoard();
    Board clientBoard = boardBox.getBoard();
    boolean black = !clientBoard.isBlack();
    inverted.setBlack(black);
    inverted.setBlackTurn(black);
    boardService.save(inverted);

    Notation notation = updatedBox.getNotation();
    notation.getNotationFen().setBlackTurn(black);
    notationService.save(notation, true);

    updatedBox.setBoard(inverted);
    boardBoxDao.save(updatedBox);

    boardService.updateBoard(inverted);
    return updatedBox;
  }

  @NotNull
  public BoardBox initBoard(@NotNull BoardBox boardBox, AuthUser authUser) {
    throw404IfNotFound(boardBox);
    try {
      boardDao.deleteByBoardBoxId(boardBox.getDomainId());
    } catch (DaoException ignore) {
    }
    // reset board
    Board board = boardService.createBoard(boardBox.getBoard(), boardBox.getDomainId());
    board = boardService.initWithDraughtsOnBoard(board);
    boardBox.setBoardId(board.getDomainId());
    boardBox.setBoard(board);

    // clear notation
    notationService.clearNotationInBoardBox(boardBox);
    return boardBox;
  }

  @NotNull
  public BoardBox clearBoard(@NotNull BoardBox boardBox, AuthUser authUser) {
    throw404IfNotFound(boardBox);
    try {
      boardDao.deleteByBoardBoxId(boardBox.getDomainId());
    } catch (DaoException ignore) {
    }

    Board board = boardService.createBoard(boardBox.getBoard(), boardBox.getDomainId());
    boardBox.setBoardId(board.getDomainId());
    boardBox.setBoard(board);
    boardBoxDao.save(boardBox);

    notationService.clearNotationInBoardBox(boardBox);
    return boardBox;
  }

  @NotNull
  public BoardBox save(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
    throw404IfNotFound(boardBox);
    return saveAndFillBoard(boardBox, authUser);
  }

  public BoardBox markTask(BoardBox boardBox, AuthUser authUser) {
    BoardBoxes byArticleId = boardBoxDao.findByArticleId(boardBox.getArticleId());
    updateMarkTaskId(boardBox, byArticleId);
    return save(boardBox, authUser);
  }

  @NotNull
  public BoardBox loadPreviewBoard(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
    Board loadBoard = boardDao.findById(boardBox.getBoardId());
    loadBoard = boardService.resetHighlightAndUpdate(loadBoard);
    boardBox.setBoard(loadBoard);
    boardBoxDao.save(boardBox);
    Notation notation = boardBox.getNotation();
    notation.syncFormatAndRules();
    notationHistoryService.save(notation.getNotationHistory());
    notationStoreService.removeNotation(notation);
    boardBoxStoreService.remove(boardBox);
    boardBoxStoreService.removeByArticleId(boardBox.getArticleId());
    return boardBox;
  }

  @NotNull
  public BoardBox addDraught(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
    Square selectedSquare = boardBox.getBoard().getSelectedSquare();
    if (selectedSquare == null
        || !selectedSquare.isOccupied()
        || !boardBox.getEditMode().equals(EnumEditBoardBoxMode.PLACE)) {
      throw RequestException.badRequest(ErrorMessages.UNABLE_TO_ADD_DRAUGHT);
    }
    Draught draught = selectedSquare.getDraught();
    var updated = findAndFill(boardBox, authUser);
    Board currentBoard = updated.getBoard();
    Square squareLink = findSquareByLink(selectedSquare, currentBoard);
    try {
      currentBoard = boardService.addDraught(currentBoard, squareLink.getNotation(), draught);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw RequestException.badRequest(e.getMessage());
    }
    updated.setBoardId(currentBoard.getDomainId());
    updated.setBoard(currentBoard);
    saveAndFillBoard(updated, authUser);
    return updated;
  }

  @Nullable
  public BoardBox undo(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
    var filledBoardBox = findAndFill(boardBox, authUser);
    NotationHistory history = filledBoardBox.getNotation().getNotationHistory();
    int forkToDrive = history.size() - 1;
    return forkNotationForVariants(filledBoardBox, forkToDrive, authUser);
  }

  @Nullable
  public BoardBox redo(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
    NotationLine notationLine = boardBox.getNotation().getNotationHistory().getNotationLine();
    var filledBoardBox = findAndFill(boardBox, authUser);
    return switchNotationToVariant(notationLine, filledBoardBox, authUser);
  }

  @NotNull
  public BoardBoxes viewBranch(@NotNull BoardBox boardBox, @NotNull AuthUser token) {
    switchNotation(boardBox, token);
    BoardBoxes byArticleId = boardBoxDao.findByArticleId(boardBox.getArticleId());
    return fillBoardBoxes(boardBox.getArticleId(), byArticleId, token);
  }

  @NotNull
  public BoardBoxes forkNotation(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
    int currentNotationDrive = boardBox.getNotation().getNotationHistory().getCurrentIndex();
    var filledBoardBox = findAndFill(boardBox, authUser);
    forkNotationForVariants(filledBoardBox, currentNotationDrive, authUser);
    BoardBoxes byArticleId = boardBoxDao.findByArticleId(boardBox.getArticleId());
    return fillBoardBoxes(boardBox.getArticleId(), byArticleId, authUser);
  }

  @NotNull
  public BoardBoxes switchNotation(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
    NotationLine notationLine = boardBox.getNotation().getNotationHistory().getNotationLine();
    var filledBoardBox = findAndFill(boardBox, authUser);
    switchNotationToVariant(notationLine, filledBoardBox, authUser);
    BoardBoxes byArticleId = boardBoxDao.findByArticleId(boardBox.getArticleId());
    return fillBoardBoxes(boardBox.getArticleId(), byArticleId, authUser);
  }

  private BoardBox switchNotationToVariant(NotationLine notationLine, BoardBox boardBox, AuthUser authUser) {
    try {
      Notation switched = notationService.switchTo(notationLine, boardBox.getNotation());
      // switch to new board
      if (switched.getNotationHistory().size() == 1) {
        DomainId boardId = boardBox.getNotation().getNotationFen().getBoardId();
        return saveBoardBoxAfterSwitchFork(boardBox, boardId, authUser);
      }
      return switched.getNotationHistory()
          .getLastNotationBoardId()
          .map(boardId ->
              saveBoardBoxAfterSwitchFork(boardBox, boardId, authUser))
          .orElse(null);
    } catch (DaoException e) {
      throw RequestException.notFound404(ErrorMessages.UNABLE_TO_SWITCH);
    }
  }

  private BoardBox forkNotationForVariants(BoardBox boardBox, int forkFromNotationDrive, AuthUser authUser) {
    Board board = boardBox.getBoard().deepClone();
    Utils.setRandomIdAndCreatedAt(board);
    boardService.save(board);
    Notation notation = boardBox.getNotation();
    Notation forked = notationService.forkAt(forkFromNotationDrive, notation);
    // switch to new board
    if (forked != null && forked.getNotationHistory().size() != 1) {
      return forked.getNotationHistory()
          .getLastNotationBoardIdInVariants()
          .map(boardId -> saveBoardBoxAfterSwitchFork(boardBox, boardId, authUser))
          .orElse(null);
    }
    DomainId boardId = notation.getNotationFen().getBoardId();
    return saveBoardBoxAfterSwitchFork(boardBox, boardId, authUser);
  }

  @NotNull
  private BoardBox saveBoardBoxAfterSwitchFork(BoardBox boardBox, DomainId boardId, @Nullable AuthUser authUser) {
    var board = boardDao.findById(boardId);
    board = boardService.resetHighlightAndUpdate(board);
    boardDao.save(board);
    boardBox.setBoard(board);
    boardBox.setBoardId(board.getDomainId());
    if (boardBox.isReadonly() && authUser != null) {
      boardBoxStoreService.put(authUser.getUserSession(), boardBox);
    } else {
      boardBoxDao.save(boardBox);
    }
    notationService.save(boardBox.getNotation(), true);
    return boardBox;
  }

  private boolean isNotEditMode(BoardBox boardBox) {
    return !boardBox.getEditMode().equals(EnumEditBoardBoxMode.EDIT);
  }

  private boolean resetHighlightIfNotLastBoard(BoardBox boardBox) {
    NotationDrives notationDrives = boardBox.getNotation().getNotationHistory().getNotation();
    boolean isLastSelected = notationDrives.getLast().isSelected();
    return !isLastSelected;
  }

  @NotNull
  private BoardBox updateBoardBox(BoardBox boardBox, @NotNull AuthUser authUser) {
    Board board = boardService.findById(boardBox.getBoardId());
    Notation notation = notationService.findById(boardBox.getNotationId(), authUser);
    board = boardService.resetHighlightAndUpdate(board);
    board.setReadonly(boardBox.isReadonly());
    boardBox.setBoard(board);
    notation.setRules(board.getRules());
    boardBox.setNotation(notation);
    boardBox.setNotationId(notation.getDomainId());
    boardBoxStoreService.put(authUser.getUserSession(), boardBox);
    return boardBox;
  }

  @NotNull
  private BoardBox updatePublicBoardBox(@NotNull AuthUser authUser, @NotNull BoardBox boardBox) {
    fillWithBoards(new BoardBoxes(List.of(boardBox)));
    Notation notation = notationService.findById(boardBox.getNotationId(), authUser);
    boardBox.setNotation(notation);
    boardBox.setNotationId(notation.getDomainId());
    boardBoxStoreService.put(authUser.getUserSession(), boardBox);
    return boardBox;
  }

  @NotNull
  private BoardBox saveAndFillBoard(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
    boardBoxDao.save(boardBox);
    Board board = boardBox.getBoard();
    board.setBoardBoxId(boardBox.getDomainId());
    boardService.save(board);
    Notation notation = boardBox.getNotation();
    if (boardBox.getEditMode().equals(EnumEditBoardBoxMode.EDIT)
        && notation.getNotationHistory().isEmpty()) {
      notationService.setNotationFenFromBoard(notation, board);
    }
    notationHistoryService.save(notation.getNotationHistory());
    notationService.save(notation, true);
    boardBox = updateBoardBox(boardBox, authUser);
    boardBoxStoreService.remove(boardBox);
    boardBoxStoreService.removeByArticleId(boardBox.getArticleId());
    return boardBox;
  }

  private BoardBox findBoardAndPutInStore(BoardBox boardBox, AuthUser authUser) {
    Supplier<BoardBox> fromDb = () -> {
      BoardBox resultBoardBox = boardBoxDao.find(boardBox);
      boardBoxStoreService.put(authUser.getUserSession(), resultBoardBox);
      return resultBoardBox;
    };
    return boardBoxStoreService.get(authUser.getUserSession(), boardBox.getDomainId())
        .orElseGet(fromDb);
  }

  @NotNull
  private BoardBoxes fillBoardBoxes(@NotNull DomainId articleId, BoardBoxes byUserAndIds, @NotNull AuthUser authUser) {
    DomainIds domainIds = byUserAndIds
        .valueList()
        .stream()
        .map(BoardBox::getNotationId)
        .collect(collectingAndThen(toCollection(LinkedList::new), DomainIds::new));
    fillNotations(byUserAndIds, domainIds);
    fillWithBoards(byUserAndIds);

    boardBoxStoreService.putByArticleId(authUser.getUserSession(), articleId, byUserAndIds);
    return byUserAndIds;
  }

  private void throw404IfNotFound(@NotNull BoardBox boardBox) {
    boardBoxDao.find(boardBox);
  }

  private void fillNotations(BoardBoxes byUserAndIds, DomainIds domainIds) {
    List<Notation> notationByIds = notationService.findByIds(domainIds);
    Map<String, Notation> notationMap = notationByIds
        .stream()
        .collect(Collectors.toMap(o -> o.getBoardBoxId().getId(), o -> o));

    // fill BoardBox
    for (BoardBox boardBox : byUserAndIds.valueList()) {
      Notation notation = notationMap.get(boardBox.getId());
      boardBox.setNotation(notation);
    }
  }

  private void fillWithBoards(BoardBoxes boardBoxIds) {
    DomainIds domainIds = boardBoxIds
        .valueList()
        .stream()
        .map(BoardBox::getDomainId)
        .collect(collectingAndThen(toCollection(LinkedList::new), DomainIds::new));

    Map<DomainId, BoardBox> boardBoxByDomainId = boardBoxIds
        .entrySet()
        .stream()
        .collect(Collectors.toMap(o -> o.getValue().getDomainId(), Map.Entry::getValue));

    List<Board> boards = boardDao.findByBoardBoxIds(domainIds);
    for (Board board : boards) {
      board = boardService.resetHighlightAndUpdate(board);
      BoardBox boardBox = boardBoxByDomainId.get(board.getBoardBoxId());
      board.setReadonly(boardBox.isReadonly());
      boardBox.getPublicBoards().add(board);
      if (boardBox.getBoardId().getId().equals(board.getId())) {
        boardBox.setBoard(board);
      }
    }
  }

  @NotNull
  public BoardBox removeVariant(@NotNull BoardBox boardBox, AuthUser authUser) {
    NotationLine switchLine = notationService.removeVariant(boardBox.getNotation());
    if (switchLine != null) {
      switchNotationToVariant(switchLine, boardBox, authUser);
    }
    return boardBox;
  }

  private void updateMarkTaskId(BoardBox boardBox, BoardBoxes byArticleId) {
    List<BoardBox> valueList = byArticleId.valueList();
    valueList.sort(Comparator.comparingInt(BoardBox::getIdInArticle));
    int j = 1;
    for (BoardBox box : valueList) {
      if (box.isTask() && !box.getId().equals(boardBox.getId())) {
        box.setTaskIdInArticle(j);
        j++;
      } else if (boardBox.isTask() && box.getId().equals(boardBox.getId())) {
        box.setTaskIdInArticle(j);
        boardBox.setTaskIdInArticle(j);
        j++;
      } else {
        box.setTaskIdInArticle(0);
      }
    }
    boardBoxDao.batchSave(valueList);
    boardBoxStoreService.removeByArticleId(boardBox.getArticleId());
  }
}
