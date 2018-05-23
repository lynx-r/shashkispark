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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.workingbit.board.BoardEmbedded.*;
import static com.workingbit.board.controller.util.BoardUtils.findSquareByLink;
import static com.workingbit.share.common.RequestConstants.IS_PUBLIC_QUERY;

/**
 * Created by Aleksey Popryaduhin on 07:00 22/09/2017.
 */
public class BoardBoxService {

  private final Logger logger = LoggerFactory.getLogger(BoardBoxService.class);
  private final static BoardService boardService = new BoardService();
  private final static NotationService notationService = new NotationService();

  public BoardBox createBoardBox(CreateBoardPayload createBoardPayload, AuthUser authUser) {
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

    saveAndFillBoard(authUser, boardBox, true);

    return boardBox;
  }

  public BoardBox parsePdn(ImportPdnPayload importPdnPayload, AuthUser authUser) {
    try {
      Notation notation = notationParserService.parse(importPdnPayload.getPdn());
      notation.setRules(importPdnPayload.getRules());
      return createBoardBoxFromNotation(importPdnPayload.getArticleId(), importPdnPayload.getIdInArticle(), notation, authUser);
    } catch (ParserLogException | ParserCreationException e) {
      logger.error("PARSE ERROR: " + e.getMessage(), e);
      throw RequestException.internalServerError(ErrorMessages.UNPARSABLE_PDN_CONTENT, e.getMessage());
    } catch (BoardServiceException be) {
      logger.error("PARSE ERROR: " + be.getMessage(), be);
      throw RequestException.internalServerError(be.getMessage());
    }
  }

  BoardBox createBoardBoxFromNotation(DomainId articleId, int idInArticle, Notation parsedNotation, AuthUser authUser) {
    BoardBox boardBox = new BoardBox();
    boardBox.setArticleId(articleId);
    boardBox.setUserId(authUser.getUserId());
    boardBox.setIdInArticle(idInArticle);
    Utils.setRandomIdAndCreatedAt(boardBox);

    NotationHistory toFillNotationHistory = NotationHistory.create();
    boardService.fillNotation(boardBox.getDomainId(), parsedNotation.getNotationHistory(), toFillNotationHistory, parsedNotation.getRules());
    parsedNotation.setNotationHistory(toFillNotationHistory);
    Utils.setRandomIdAndCreatedAt(parsedNotation);
    notationService.save(authUser, parsedNotation, true);

    // switch boardBox to the first board
    return toFillNotationHistory.getLastNotationBoardId()
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

  BoardBox find(BoardBox boardBox, AuthUser authUser) {
    BoardBox board = findBoardAndPutInStore(authUser, boardBox);
    boolean secure = EnumAuthority.hasAuthorAuthorities(authUser);
    if (secure) {
      board.setReadonly(false);
    } else {
      board.setReadonly(true);
    }
    return updateBoardBox(authUser, board);
  }

  public BoardBox findById(DomainId boardBoxId, AuthUser authUser, String publicQuery) {
    if (publicQuery.equals(IS_PUBLIC_QUERY)) {
      return findPublicById(boardBoxId, authUser);
    }
    if (!EnumAuthority.hasAuthorAuthorities(authUser)) {
      throw RequestException.forbidden();
    }
    return boardBoxStoreService.get(authUser.getUserSession(), boardBoxId)
        .map(boardBox -> updateBoardBox(authUser, boardBox))
        .orElseGet(() -> {
          var boardBox = boardBoxDao.findById(boardBoxId);
          updateBoardBox(authUser, boardBox);
          return boardBox;
        });
  }

  BoardBox findPublicById(DomainId boardBoxId, AuthUser authUser) {
    return boardBoxStoreService
        .get(authUser.getUserSession(), boardBoxId)
        .orElseGet(() -> {
          BoardBox publicById = boardBoxDao.findPublicById(boardBoxId);
          publicById = updatePublicBoardBox(authUser, publicById);
          boardBoxStoreService.put(authUser.getUserSession(), publicById);
          return publicById;
        });
  }

  public BoardBoxes findByArticleId(DomainId articleId, AuthUser authUser, String queryValue) {
    if (queryValue.equals(IS_PUBLIC_QUERY)) {
      return findPublicByArticleId(articleId, authUser);
    }
    if (!EnumAuthority.hasAuthorAuthorities(authUser)) {
      throw RequestException.forbidden();
    }
    try {
      BoardBoxes byUserAndIds = new BoardBoxes(boardBoxDao.findByArticleId(articleId));
      return fillBoardBoxes(authUser, articleId, byUserAndIds);
    } catch (DaoException e) {
      logger.error(e.getMessage());
      throw RequestException.noContent();
    }
  }

  private BoardBoxes findPublicByArticleId(DomainId articleId, AuthUser authUser) {
    return boardBoxStoreService
        .getByArticleId(authUser.getUserSession(), articleId)
        .orElseGet(() -> {
          List<BoardBox> publicByArticleId = boardBoxDao.findPublicByArticleId(articleId);
          BoardBoxes boardBoxes = new BoardBoxes(publicByArticleId);
          return fillBoardBoxes(authUser, articleId, boardBoxes);
        });
  }

  void delete(DomainId boardBoxId) {
    var boardBox = boardBoxDao.findById(boardBoxId);
    boardService.delete(boardBox.getBoardId());
    boardBoxDao.delete(boardBox.getDomainId());
  }

  public BoardBox highlight(BoardBox boardBox, AuthUser authUser) {
    var updatedBoardBox = find(boardBox, authUser);
    BoardBox serverBoardBox = updatedBoardBox.deepClone();
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

  public BoardBox moveSmart(BoardBox boardBox, AuthUser token) {
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
    selectedSquare = getPredictedSelectedSquare(board);
    board.setSelectedSquare(selectedSquare);
    boardDao.save(board);
    return move(boardBox, token);
  }

  public BoardBox move(BoardBox boardBox, AuthUser authUser) {
    var updatedBoxOrig = find(boardBox, authUser);
    BoardBox userBoardBox = updatedBoxOrig.deepClone();
    if (isNotEditMode(updatedBoxOrig)) {
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
      throw RequestException.badRequest(e.getMessage());
    }

    userBoardBox.setBoard(serverBoard);
    userBoardBox.setBoardId(serverBoard.getDomainId());
    notationService.save(authUser, notation, false);

    logger.info("Notation after move: " + notation.getNotationHistory().debugPdnString());

    boardBoxDao.save(userBoardBox);
    return userBoardBox;
  }

  public BoardBox changeTurn(BoardBox boardBox, AuthUser authUser) {
    var updatedBox = find(boardBox, authUser);
    Board inverted = updatedBox.getBoard();
    Board clientBoard = boardBox.getBoard();
    inverted.setBlackTurn(!clientBoard.isBlackTurn());
    boardService.save(inverted);

    updatedBox.setBoard(inverted);
    boardBoxDao.save(updatedBox);

    boardService.updateBoard(inverted);
    return updatedBox;
  }

  public BoardBox changeBoardColor(BoardBox boardBox, AuthUser authUser) {
    var updatedBox = find(boardBox, authUser);
    Board inverted = updatedBox.getBoard();
    Board clientBoard = boardBox.getBoard();
    inverted.setBlack(!clientBoard.isBlack());
    boardService.save(inverted);

    updatedBox.setBoard(inverted);
    boardBoxDao.save(updatedBox);

    boardService.updateBoard(inverted);
    return updatedBox;
  }

  public BoardBox initBoard(BoardBox boardBox, AuthUser authUser) {
    throw404IfNotFound(boardBox);
    // reset board
    DomainId boardId = boardBox.getBoardId();
    var board = boardDao.findById(boardId);
    board = boardService.initWithDraughtsOnBoard(board);
    boardBox.setBoardId(board.getDomainId());
    boardBox.setBoard(board);

    // clear notation
    return notationService.clearNotationInBoardBox(boardBox);
  }

  public BoardBox clearBoard(BoardBox boardBox, AuthUser authUser) {
    throw404IfNotFound(boardBox);
    // clear board
    DomainId boardId = boardBox.getBoardId();
    var board = boardDao.findById(boardId);
    board = boardService.clearBoard(board);
    boardBox.setBoardId(board.getDomainId());
    boardBox.setBoard(board);

    // clear notation
    return notationService.clearNotationInBoardBox(boardBox);
  }

  public BoardBox save(BoardBox boardBox, AuthUser authUser) {
    throw404IfNotFound(boardBox);
    return saveAndFillBoard(authUser, boardBox, true);
  }

  public Optional<BoardBox> loadPreviewBoard(BoardBox boardBox, AuthUser authUser) {
    notationService.save(authUser, boardBox.getNotation(), false);
    updateBoardBox(authUser, boardBox);
    return Optional.of(boardBox);
  }

  public BoardBox addDraught(BoardBox boardBox, AuthUser authUser) {
    Square selectedSquare = boardBox.getBoard().getSelectedSquare();
    if (selectedSquare == null
        || !selectedSquare.isOccupied()
        || !boardBox.getEditMode().equals(EnumEditBoardBoxMode.PLACE)) {
      throw RequestException.badRequest(ErrorMessages.UNABLE_TO_ADD_DRAUGHT);
    }
    Draught draught = selectedSquare.getDraught();
    var updated = find(boardBox, authUser);
    Board currentBoard = updated.getBoard();
    Square squareLink = findSquareByLink(selectedSquare, currentBoard);
    if (squareLink == null) {
      logger.error("Unable to add a draught");
      throw RequestException.badRequest(ErrorMessages.UNABLE_TO_ADD_DRAUGHT);
    }
    try {
      currentBoard = boardService.addDraught(currentBoard, squareLink.getNotation(), draught);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw RequestException.badRequest(e.getMessage());
    }
    updated.setBoardId(currentBoard.getDomainId());
    updated.setBoard(currentBoard);
    saveAndFillBoard(authUser, updated, false);
    return updated;
  }

  public BoardBox undo(BoardBox boardBox, AuthUser authUser) {
    var filledBoardBox = find(boardBox, authUser);
    NotationHistory history = filledBoardBox.getNotation().getNotationHistory();
    NotationDrive forkToDrive = history.getLast().deepClone();
    return forkNotationForVariants(filledBoardBox, forkToDrive, authUser);
  }

  public BoardBox redo(BoardBox boardBox, AuthUser authUser) {
    var filledBoardBox = find(boardBox, authUser);
    NotationHistory history = filledBoardBox.getNotation().getNotationHistory();
    NotationDrive switchToDrive = history.getLast().deepClone();
    return switchNotationToVariant(authUser, filledBoardBox, switchToDrive, null);
  }

  public BoardBoxes viewBranch(BoardBox boardBox, AuthUser token) {
    switchNotation(boardBox, token);
    BoardBoxes byArticleId = new BoardBoxes(boardBoxDao.findByArticleId(boardBox.getArticleId()));
    return fillBoardBoxes(token, boardBox.getArticleId(), byArticleId);
  }

  public BoardBoxes forkNotation(BoardBox boardBox, AuthUser authUser) {
    NotationDrive currentNotationDrive = boardBox.getNotation().getNotationHistory().getCurrentNotationDrive();
    var filledBoardBox = find(boardBox, authUser);
    forkNotationForVariants(filledBoardBox, currentNotationDrive, authUser);
    BoardBoxes byArticleId = new BoardBoxes(boardBoxDao.findByArticleId(boardBox.getArticleId()));
    return fillBoardBoxes(authUser, boardBox.getArticleId(), byArticleId);
  }

  public BoardBoxes switchNotation(BoardBox boardBox, AuthUser authUser) {
    NotationDrive currentNotationDrive = boardBox.getNotation().getNotationHistory().getCurrentNotationDrive();
    NotationDrive variantNotationDrive = boardBox.getNotation().getNotationHistory().getVariantNotationDrive();
    var filledBoardBox = find(boardBox, authUser);
    switchNotationToVariant(authUser, filledBoardBox, currentNotationDrive, variantNotationDrive);
    BoardBoxes byArticleId = new BoardBoxes(boardBoxDao.findByArticleId(boardBox.getArticleId()));
    return fillBoardBoxes(authUser, boardBox.getArticleId(), byArticleId);
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

  private BoardBox forkNotationForVariants(BoardBox boardBox, NotationDrive forkFromNotationDrive, AuthUser authUser) {
    Notation notation = boardBox.getNotation();
    NotationHistory notationDrives = notation.getNotationHistory();
    boolean success = notationDrives.forkAt(forkFromNotationDrive);
    // switch to new board
    if (success) {
      return notationDrives
          .getLastNotationBoardId()
          .map(boardId -> saveBoardBoxAfterSwitchFork(authUser, boardBox, boardId))
          .orElse(null);
    }
    return null;
  }

  private BoardBox saveBoardBoxAfterSwitchFork(AuthUser authUser, BoardBox boardBox, DomainId boardId) {
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
    notationService.save(authUser, boardBox.getNotation(), false);
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

  private BoardBox updateBoardBox(AuthUser authUser, BoardBox boardBox) {
    Board board = boardService.findById(boardBox.getBoardId());
    Notation notation = notationService.findById(authUser, boardBox.getNotationId());
    board = boardService.resetHighlightAndUpdate(board);
    board.setReadonly(boardBox.isReadonly());
    boardBox.setBoard(board);
    boardBox.setNotation(notation);
    boardBox.setNotationId(notation.getDomainId());
    boardBoxStoreService.put(authUser.getUserSession(), boardBox);
    return boardBox;
  }

  private BoardBox updatePublicBoardBox(AuthUser authUser, BoardBox boardBox) {
    fillWithBoards(new BoardBoxes(List.of(boardBox)));
    Notation notation = notationService.findById(authUser, boardBox.getNotationId());
    boardBox.setNotation(notation);
    boardBox.setNotationId(notation.getDomainId());
    boardBoxStoreService.put(authUser.getUserSession(), boardBox);
    return boardBox;
  }

  private BoardBox saveAndFillBoard(AuthUser authUser, BoardBox boardBox, boolean publish) {
    boardBoxDao.save(boardBox);
    Board board = boardBox.getBoard();
    board.setBoardBoxId(boardBox.getDomainId());
    boardService.save(board);
    Notation notation = boardBox.getNotation();
    notationService.save(authUser, notation, publish);
    boardBox = updateBoardBox(authUser, boardBox);
    boardBoxStoreService.remove(boardBox);
    boardBoxStoreService.removeByArticleId(boardBox.getArticleId());
    return boardBox;
  }

  private BoardBox findBoardAndPutInStore(AuthUser authUser, BoardBox boardBox) {
    Optional<BoardBox> fromStore = boardBoxStoreService.get(authUser.getUserSession(), boardBox.getDomainId());
    Supplier<BoardBox> fromDb = () -> {
      BoardBox resultBoardBox = boardBoxDao.find(boardBox);
      boardBoxStoreService.put(authUser.getUserSession(), resultBoardBox);
      return resultBoardBox;
    };
    boolean secure = EnumAuthority.hasAuthorAuthorities(authUser);
    if (secure) {
      return fromDb.get();
    } else {
      return fromStore.orElseGet(fromDb);
    }
  }

  private BoardBoxes fillBoardBoxes(AuthUser authUser, DomainId articleId, BoardBoxes byUserAndIds) {
    LinkedList<DomainId> collect = byUserAndIds
        .getBoardBoxes()
        .valueList()
        .stream()
        .map(BoardBox::getNotationId)
        .collect(Collectors.toCollection(LinkedList::new));
    DomainIds domainIds = new DomainIds(collect);
    List<Notation> notationByIds = notationDao.findByIds(domainIds);
    Map<String, Notation> notationMap = notationByIds
        .stream()
        .collect(Collectors.toMap(o -> o.getBoardBoxId().getId(), o -> o));

    // fill BoardBox
    for (BoardBox boardBox : byUserAndIds.getBoardBoxes().valueList()) {
      Notation notation = notationMap.get(boardBox.getId());
      boardBox.setNotation(notation);
    }
    fillWithBoards(byUserAndIds);

    boardBoxStoreService.putByArticleId(authUser.getUserSession(), articleId, byUserAndIds);
    return byUserAndIds;
  }

  private void throw404IfNotFound(BoardBox boardBox) {
    boardBoxDao.find(boardBox);
  }

  private Square findSmartOnDiagonal(Square nextSquare, List<Square> diagonals, boolean black, boolean down) {
    int nextI = diagonals.indexOf(nextSquare);
    int tries = 0;
    int i = nextI;
    while (down ? i >= 0 : i < diagonals.size()) {
      Square square = diagonals.get(i);
      if (square.isOccupied() && square.getDraught().isBlack() == black) {
        if (!square.getDraught().isQueen() && tries > 1) {
          if (down) {
            i--;
          } else {
            i++;
          }
          continue;
        }
        return square;
      }
      if (down) {
        i--;
      } else {
        i++;
      }
      if (!square.isOccupied()) {
        tries++;
      }
    }
    return null;
  }

  private Square getPredictedSelectedSquare(Board board) {
    Square nextSquare;
    nextSquare = board.getNextSquare();
    boolean black = board.isBlackTurn();
    List<Square> found = new ArrayList<>();
    for (List<Square> diagonals : nextSquare.getDiagonals()) {
      Square selected = findSmartOnDiagonal(nextSquare, diagonals, black, false);
      if (selected != null) {
        found.add(selected);
      }
      selected = findSmartOnDiagonal(nextSquare, diagonals, black, true);
      if (selected != null) {
        found.add(selected);
      }
    }
    if (found.size() != 1) {
      Board serverBoard = board.deepClone();
      List<Square> allowed = new ArrayList<>();
      Set<Square> captured = new HashSet<>();
      for (Square square : found) {
        serverBoard.setSelectedSquare(square);
        Map highlight = boardService.getHighlight(serverBoard, board);
        MovesList movesList = (MovesList) highlight.get("movesList");
        List<Square> moveCaptured = movesList.getCaptured().flatTree();
        if (!movesList.getAllowed().isEmpty() && !captured.containsAll(moveCaptured)) {
          captured.addAll(moveCaptured);
          allowed.add(square);
        }
      }
      if (allowed.size() != 1) {
        throw RequestException.badRequest(ErrorMessages.UNABLE_TO_MOVE);
      }
      return allowed.get(0);
    }
    return found.get(0);
  }

  private void fillWithBoards(BoardBoxes boardBoxIds) {
    LinkedList<DomainId> collect = boardBoxIds
        .getBoardBoxes()
        .valueList()
        .stream()
        .map(BoardBox::getDomainId)
        .collect(Collectors.toCollection(LinkedList::new));
    DomainIds domainIds = new DomainIds(collect);

    Map<DomainId, BoardBox> boardBoxByDomainId = boardBoxIds.getBoardBoxes()
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
}
