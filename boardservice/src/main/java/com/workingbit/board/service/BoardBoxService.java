package com.workingbit.board.service;

import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.board.repo.ReactiveBoardBoxRepository;
import com.workingbit.board.repo.ReactiveBoardRepository;
import com.workingbit.board.repo.ReactiveNotationHistoryRepository;
import com.workingbit.board.repo.ReactiveNotationRepository;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.domain.impl.Notation;
import com.workingbit.share.domain.impl.NotationHistory;
import com.workingbit.share.exception.DaoException;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumEditBoardBoxMode;
import com.workingbit.share.util.Utils;
import net.percederberg.grammatica.parser.ParserCreationException;
import net.percederberg.grammatica.parser.ParserLogException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.workingbit.share.common.RequestConstants.PUBLIC_QUERY;

/**
 * Created by Aleksey Popryaduhin on 07:00 22/09/2017.
 */
@Service
public class BoardBoxService {

  private final Logger logger = LoggerFactory.getLogger(BoardBoxService.class);

  private BoardService boardService;
  private NotationService notationService;
  private NotationParserService notationParserService;
  private NotationHistoryService notationHistoryService;
  private ReactiveBoardBoxRepository boardBoxRepository;
  private ReactiveBoardRepository boardRepository;
  private ReactiveNotationRepository notationRepository;
  private ReactiveNotationHistoryRepository notationHistoryRepository;

  public BoardBoxService(BoardService boardService,
                         NotationService notationService,
                         NotationParserService notationParserService,
                         NotationHistoryService notationHistoryService) {

    this.boardService = boardService;
    this.notationService = notationService;
    this.notationParserService = notationParserService;
    this.notationHistoryService = notationHistoryService;
  }

  @NotNull
  public Mono<BoardBox> createBoardBox(@NotNull CreateBoardPayload createBoardPayload, @NotNull AuthUser authUser) {
    return boardService.createBoard(createBoardPayload)
        .map(board -> {
          BoardBox boardBox = new BoardBox(board);
          DomainId boardBoxId = createBoardPayload.getBoardBoxId();
          boardBox.setDomainId(boardBoxId);
          DomainId articleId = createBoardPayload.getArticleId();
          boardBox.setArticle(articleId);
          DomainId userId = createBoardPayload.getUserId();
          boardBox.setUserId(userId);
          boardBox.setIdInArticle(createBoardPayload.getIdInArticle());
          boardBox.setEditMode(createBoardPayload.getEditMode());

          notationService.createNotationForBoardBox(boardBox);
          saveAndFillBoard(boardBox, authUser);
          return boardBox;
        });
  }

  public Mono<BoardBox> parsePdn(@NotNull ImportPdnPayload importPdnPayload, @NotNull AuthUser authUser) {
    try {
      Notation notation = notationParserService.parse(importPdnPayload.getPdn());
      if (notation.getRules() == null) {
        notation.setRules(importPdnPayload.getRules());
      }
      return createBoardBoxFromNotation(importPdnPayload.getArticleId(),
          importPdnPayload.getIdInArticle(), notation, authUser);
    } catch (@NotNull ParserLogException | ParserCreationException e) {
      logger.error("PARSE ERROR: " + e.getMessage(), e);
      throw RequestException.internalServerError(ErrorMessages.UNPARSABLE_PDN_CONTENT, e.getMessage());
    } catch (BoardServiceException be) {
      logger.error("PARSE ERROR: " + be.getMessage(), be);
      throw RequestException.internalServerError(be.getMessage());
    }
  }

  Mono<BoardBox> createBoardBoxFromNotation(DomainId articleId, int idInArticle, @NotNull Notation parsedNotation, @NotNull AuthUser authUser) {
    BoardBox boardBox = new BoardBox();
    boardBox.setArticle(articleId);
    boardBox.setUserId(authUser.getUserId());
    boardBox.setIdInArticle(idInArticle);
    Utils.setRandomIdAndCreatedAt(boardBox);

    Utils.setRandomIdAndCreatedAt(parsedNotation);
    boardService.fillNotation(boardBox.getDomainId(), parsedNotation.getNotationFen(), parsedNotation.getDomainId(),
        parsedNotation, parsedNotation.getRules());
    NotationHistory filledNotationHistory = parsedNotation.getNotationHistory();
    // switch boardBox to the first board
    DomainId lastNotationBoardId = filledNotationHistory.getLastNotationBoardId();
    if (lastNotationBoardId != null) {
      var firstBoard = boardRepository.findById(lastNotationBoardId);
      return firstBoard.map((board -> {
        board = boardService.updateBoard(board);
        boardBox.setNotationId(parsedNotation.getDomainId());
        boardBox.setNotation(parsedNotation);
        boardBox.setBoard(lastNotationBoardId);
        boardBox.setBoard(board);
        boardBoxRepository.save(boardBox);
        return boardBox;
      }));
    }
    return Mono.empty();
  }

//  @NotNull
//  BoardBox findAndFill(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
//    BoardBox board = findBoardAndPutInStore(boardBox, authUser);
//    return updateBoardBox(board);
//  }

  public Mono<BoardBox> findById(@NotNull DomainId boardBoxId, @NotNull AuthUser authUser, @NotNull String publicQuery) {
    try {
      if (publicQuery.equals(PUBLIC_QUERY)) {
        return boardBoxRepository
            .findById(boardBoxId);
//            .map(this::updatePublicBoardBox);
      }
//      if (!EnumAuthority.hasAuthorAuthorities(authUser)) {
//        throw RequestException.forbidden();
//      }
      return boardBoxRepository.findById(boardBoxId);
//          .map(this::updateBoardBox);
    } catch (DaoException e) {
      throw RequestException.notFound404(e.getMessage());
    }
  }

//  public Mono<BoardBoxes> findByArticleId(@NotNull DomainId article, @NotNull String queryValue, @NotNull AuthUser authUser) {
////    if (queryValue.equals(PUBLIC_QUERY)) {
////      return findPublicByArticleId(article);
////    }
////    if (!EnumAuthority.hasAuthorAuthorities(authUser)) {
////      throw RequestException.forbidden();
////    }
//    return boardBoxRepository
//        .findByArticleId_Id(article.getId())
//        .collectList()
//        .flatMap(boardBoxes -> fillBoardBoxes(article, boardBoxes));
//  }

//  private Mono<List<BoardBox>> findPublicByArticleId(@NotNull DomainId article) {
//    return boardBoxRepository.findByArticleId_Id(article.getId())
//        .collectList()
//        .flatMap(boardBox -> fillBoardBoxes(article, boardBox));
//  }

//  public BoardBox deleteBoardBox(DomainId boardBox, AuthUser authUser) {
//    var boardBox = boardBoxRepository.findById(boardBox);
//    if (boardBox == null) {
//      throw RequestException.notFound404();
//    }
//    boardRepository.deleteByBoardBoxId(boardBox);
//    notationHistoryService.deleteByNotationId(boardBox.getNotationId());
//    notationService.deleteById(boardBox.getNotationId());
//    boardBoxRepository.delete(boardBox.getDomainId());
////    boardBoxStoreService.remove(boardBox);
//    DomainId article = boardBox.getArticle();
//    try {
//      var byArticleId = boardBoxRepository.findByArticleId(article);
//      ListOrderedMap<String, BoardBox> boardBoxes = byArticleId.getBoardBoxes();
//      BoardBoxes updatedBB = new BoardBoxes();
//      List<BoardBox> valueList = boardBoxes.valueList();
//      valueList.sort(Comparator.comparingInt(BoardBox::getIdInArticle));
//      int j = 1;
//      for (int i = 0; i < valueList.size(); i++) {
//        BoardBox curBB = valueList.get(i);
//        curBB.setIdInArticle(i + 1);
//        if (curBB.isTask()) {
//          curBB.setTaskIdInArticle(j);
//          j++;
//        }
//        updatedBB.push(curBB);
//      }
//      boardBoxRepository.batchSave(updatedBB.valueList());
//      return updateBoardBox(updatedBB.getBoardBoxes().valueList().get(0), authUser);
//    } catch (DaoException e) {
//      return new BoardBox();
//    }
//  }

//  public Payload deleteBoardBoxByArticleId(DomainId article, AuthUser token) {
//    try {
//      BoardBoxes byArticleId = boardBoxRepository.findByArticleId(article);
//      byArticleId.getBoardBoxes().valueList().forEach(boardBox -> deleteBoardBox(boardBox.getDomainId(), token));
//    } catch (Exception e) {
//      logger.error(e.getMessage(), e);
//      return new ResultPayload(false);
//    }
//    return new ResultPayload(true);
//  }

//  public BoardBox highlight(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
//    var serverBoardBox = findAndFill(boardBox, authUser);
//    Board clientBoard = boardBox.getBoard();
//    if (resetHighlightIfNotLastBoard(serverBoardBox)) {
//      Board noHighlight = boardService.resetHighlightAndUpdate(clientBoard);
//      boardBox.setBoard(noHighlight);
//      return boardBox;
//    }
//    Board currentBoard = serverBoardBox.getBoard();
//    Square selectedSquare = clientBoard.getSelectedSquare();
//    if (!selectedSquare.isOccupied()) {
//      return serverBoardBox;
//    }
//    try {
//      var highlight = boardService.getHighlight(currentBoard, clientBoard);
//      currentBoard = (Board) highlight.get("serverBoard");
//    } catch (BoardServiceException e) {
//      logger.error(e.getMessage());
//      throw RequestException.badRequest(e.getMessage());
//    } catch (Exception e) {
//      logger.error(e.getMessage(), e);
//      throw RequestException.badRequest();
//    }
//    serverBoardBox.setBoard(currentBoard);
////    boardBoxStoreService.put(authUser.getUserSession(), serverBoardBox);
//    return serverBoardBox;
//  }

//  @Nullable
//  public BoardBox moveSmart(@NotNull BoardBox boardBox, @NotNull AuthUser token) {
//    Board board = boardBox.getBoard();
//    Square nextSquare = board.getNextSquare();
//    if (nextSquare.isOccupied()) {
//      return null;
//    }
//    Square selectedSquare = board.getSelectedSquare();
//    if (selectedSquare != null) {
//      boolean isSelectedSquareTurn = selectedSquare.getDraught().isBlack() == board.isBlackTurn();
//      if (selectedSquare.isOccupied() && isSelectedSquareTurn) {
//        return move(boardBox, token);
//      }
//      return null;
//    }
//    boardService.updateBoard(board);
//    selectedSquare = boardService.getPredictedSelectedSquare(board);
//    board.setSelectedSquare(selectedSquare);
//    boardRepository.save(board);
//    return move(boardBox, token);
//  }

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
    if (notationHistory.size() == 1) {
      notationHistory.get(0).setBoardDimension(notation.getRules().getDimension());
      notationHistory.get(0).setNotationFormat(notation.getFormat());
    }
    try {
      clientBoard = boardService.move(clientBoard, notationHistory);
    } catch (BoardServiceException e) {
      logger.error("Error while moving " + e.getMessage());
      throw RequestException.badRequest(e.getMessage());
    }

    boardBox.setBoard(clientBoard);
    boardBox.setBoard(clientBoard.getDomainId());
    notation.addForkedNotationHistory(notationHistory);
    notationHistoryService.save(notationHistory);
    notationService.syncSubVariants(notationHistory, notation);
//    notationStoreService.putNotation(authUser.getUserSession(), notation);

    boardBoxRepository.save(boardBox);
//    boardBoxStoreService.put(authUser.getUserSession(), boardBox);
    return boardBox;
  }

//  @NotNull
//  public BoardBox changeTurn(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
//    var updatedBox = findAndFill(boardBox, authUser);
//    Board inverted = updatedBox.getBoard();
//    Board clientBoard = boardBox.getBoard();
//    inverted.setBlackTurn(!clientBoard.isBlackTurn());
//    boardService.save(inverted);
//
//    updatedBox.setBoard(inverted);
//    boardBoxRepository.save(updatedBox);
//
//    boardService.updateBoard(inverted);
//    return updatedBox;
//  }

//  @NotNull
//  public BoardBox changeBoardColor(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
//    var updatedBox = findAndFill(boardBox, authUser);
//    Board inverted = updatedBox.getBoard();
//    Board clientBoard = boardBox.getBoard();
//    boolean black = !clientBoard.isBlack();
//    inverted.setBlack(black);
//    inverted.setBlackTurn(black);
//    boardService.save(inverted);
//
//    Notation notation = updatedBox.getNotationDrives();
//    notation.getNotationFen().setBlackTurn(black);
//    notationService.save(notation, true);
//
//    updatedBox.setBoard(inverted);
//    boardBoxRepository.save(updatedBox);
//
//    boardService.updateBoard(inverted);
//    return updatedBox;
//  }

//  @NotNull
//  public BoardBox initBoard(@NotNull BoardBox boardBox, AuthUser authUser) {
//    throw404IfNotFound(boardBox);
//    try {
//      boardRepository.deleteByBoardBoxId(boardBox.getDomainId());
//    } catch (DaoException ignore) {
//    }
//    // reset board
//    Board board = boardService.createBoard(boardBox.getBoard(), boardBox.getDomainId());
//    board = boardService.initWithDraughtsOnBoard(board);
//    boardBox.setBoard(board.getDomainId());
//    boardBox.setBoard(board);
//
//    // clear notation
//    notationService.clearNotationInBoardBox(boardBox);
//    return boardBox;
//  }

//  @NotNull
//  public BoardBox clearBoard(@NotNull BoardBox boardBox, AuthUser authUser) {
//    throw404IfNotFound(boardBox);
//    try {
//      boardRepository.deleteByBoardBoxId(boardBox.getDomainId());
//    } catch (DaoException ignore) {
//    }
//
//    Board board = boardService.createBoard(boardBox.getBoard(), boardBox.getDomainId());
//    boardBox.setBoard(board.getDomainId());
//    boardBox.setBoard(board);
//    boardBoxRepository.save(boardBox);
//
//    notationService.clearNotationInBoardBox(boardBox);
//    return boardBox;
//  }

  @NotNull
  public BoardBox save(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
    return saveAndFillBoard(boardBox, authUser);
  }

//  public BoardBox markTask(BoardBox boardBoxIn, AuthUser authUser) {
//    return boardBoxRepository.findByArticleId_Id(boardBoxIn.getArticle().getId())
//    .map(boardBox -> {
//      updateMarkTaskId(boardBoxIn, boardBox);
//      return save(boardBoxIn, authUser);
//    });
//  }

//  @NotNull
//  public BoardBox loadPreviewBoard(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
//    Mono<Board> loadBoard = boardRepository.findById(boardBox.getBoard());
//    loadBoard = boardService.resetHighlightAndUpdate(loadBoard);
//    boardBox.setBoard(loadBoard);
//    boardBoxRepository.save(boardBox);
//    Notation notation = boardBox.getNotationDrives();
//    notation.syncFormatAndRules();
//    notationHistoryService.save(notation.getNotationHistory());
////    notationStoreService.removeNotation(notation);
////    boardBoxStoreService.remove(boardBox);
////    boardBoxStoreService.removeByArticleId(boardBox.getArticle());
//    return boardBox;
//  }

//  @NotNull
//  public BoardBox addDraught(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
//    Square selectedSquare = boardBox.getBoard().getSelectedSquare();
//    if (selectedSquare == null
//        || !selectedSquare.isOccupied()
//        || !boardBox.getEditMode().equals(EnumEditBoardBoxMode.PLACE)) {
//      throw RequestException.badRequest(ErrorMessages.UNABLE_TO_ADD_DRAUGHT);
//    }
//    Draught draught = selectedSquare.getDraught();
//    var updated = findAndFill(boardBox, authUser);
//    Board currentBoard = updated.getBoard();
//    Square squareLink = findSquareByLink(selectedSquare, currentBoard);
//    try {
//      currentBoard = boardService.addDraught(currentBoard, squareLink.getNotationDrives(), draught);
//    } catch (Exception e) {
//      logger.error(e.getMessage(), e);
//      throw RequestException.badRequest(e.getMessage());
//    }
//    updated.setBoard(currentBoard.getDomainId());
//    updated.setBoard(currentBoard);
//    saveAndFillBoard(updated, authUser);
//    return updated;
//  }

//  @Nullable
//  public BoardBox undo(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
//    var filledBoardBox = findAndFill(boardBox, authUser);
//    NotationHistory history = filledBoardBox.getNotationDrives().getNotationHistory();
//    int forkToDrive = history.size() - 1;
//    return forkNotationForVariants(filledBoardBox, forkToDrive, authUser);
//  }

//  @Nullable
//  public BoardBox redo(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
//    NotationLine notationLine = boardBox.getNotationDrives().getNotationHistory().getNotationLine();
//    var filledBoardBox = findAndFill(boardBox, authUser);
//    return switchNotationToVariant(notationLine, filledBoardBox, authUser);
//  }

//  @NotNull
//  public BoardBoxes viewBranch(@NotNull BoardBox boardBox, @NotNull AuthUser token) {
//    switchNotation(boardBox, token);
//    BoardBoxes byArticleId = boardBoxRepository.findByArticleId(boardBox.getArticle());
//    return fillBoardBoxes(boardBox.getArticle(), byArticleId);
//  }

//  @NotNull
//  public BoardBoxes forkNotation(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
//    int currentNotationDrive = boardBox.getNotationDrives().getNotationHistory().getCurrentIndex();
//    var filledBoardBox = findAndFill(boardBox, authUser);
//    forkNotationForVariants(filledBoardBox, currentNotationDrive, authUser);
//    BoardBoxes byArticleId = boardBoxRepository.findByArticleId(boardBox.getArticle());
//    return fillBoardBoxes(boardBox.getArticle(), byArticleId);
//  }

//  @NotNull
//  public BoardBoxes switchNotation(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
//    NotationLine notationLine = boardBox.getNotationDrives().getNotationHistory().getNotationLine();
//    var filledBoardBox = findAndFill(boardBox, authUser);
//    switchNotationToVariant(notationLine, filledBoardBox, authUser);
//    BoardBoxes byArticleId = boardBoxRepository.findByArticleId(boardBox.getArticle());
//    return fillBoardBoxes(boardBox.getArticle(), byArticleId);
//  }

//  private BoardBox switchNotationToVariant(NotationLine notationLine, BoardBox boardBox, AuthUser authUser) {
//    try {
//      Notation switched = notationService.switchTo(notationLine, boardBox.getNotationDrives());
//      // switch to new board
//      if (switched.getNotationHistory().size() == 1) {
//        DomainId board = boardBox.getNotationDrives().getNotationFen().getBoard();
//        return saveBoardBoxAfterSwitchFork(boardBox, board, authUser);
//      }
//      return switched.getNotationHistory()
//          .getLastNotationBoardId()
//          .map(board ->
//              saveBoardBoxAfterSwitchFork(boardBox, board, authUser))
//          .orElse(null);
//    } catch (DaoException e) {
//      logger.error(e.getMessage(), e);
//      throw RequestException.notFound404(ErrorMessages.UNABLE_TO_SWITCH);
//    }
//  }

//  private BoardBox forkNotationForVariants(BoardBox boardBox, int forkFromNotationDrive, AuthUser authUser) {
//    Board board = boardBox.getBoard().deepClone();
//    Utils.setRandomIdAndCreatedAt(board);
//    boardService.save(board);
//    Notation notation = boardBox.getNotationDrives();
//    Notation forked = notationService.forkAt(forkFromNotationDrive, notation);
//    // switch to new board
//    if (forked != null && forked.getNotationHistory().size() != 1) {
//      return forked.getNotationHistory()
//          .getLastNotationBoardIdInVariants()
//          .map(board -> saveBoardBoxAfterSwitchFork(boardBox, board, authUser))
//          .orElse(null);
//    }
//    DomainId board = notation.getNotationFen().getBoard();
//    return saveBoardBoxAfterSwitchFork(boardBox, board, authUser);
//  }

//  @NotNull
//  private BoardBox saveBoardBoxAfterSwitchFork(BoardBox boardBox, DomainId board, @Nullable AuthUser authUser) {
//    var board = boardRepository.findById(board);
//    board = boardService.updateBoard(board);
//    boardRepository.save(board);
//    boardBox.setBoard(board);
//    boardBox.setBoard(board.getDomainId());
//    boardBoxRepository.save(boardBox);
//    notationService.save(boardBox.getNotationDrives(), true);
//    return boardBox;
//  }

  private boolean isNotEditMode(BoardBox boardBox) {
    return !boardBox.getEditMode().equals(EnumEditBoardBoxMode.EDIT);
  }

  private boolean resetHighlightIfNotLastBoard(BoardBox boardBox) {
    NotationDrives notationDrives = boardBox.getNotation().getNotationHistory().getNotationDrives();
    boolean isLastSelected = notationDrives.getLast().isSelected();
    return !isLastSelected;
  }

//  @NotNull
//  private BoardBox updateBoardBox(BoardBox boardBox) {
//    Flux.concat(boardService.findById(boardBox.getBoard()), notationService.findById(boardBox.getNotationId()))
//        .map(baseDomain -> {
//
//        })
//    Notation notation = ;
//    board = boardService.updateBoard(board);
//    board.setReadonly(boardBox.isReadonly());
//    boardBox.setBoard(board);
//    notation.setRules(board.getRules());
//    boardBox.setNotationDrives(notation);
//    boardBox.setNotationId(notation.getDomainId());
////    boardBoxStoreService.put(authUser.getUserSession(), boardBox);
//    return boardBox;
//  }

//  @NotNull
//  private BoardBox updatePublicBoardBox(@NotNull BoardBox boardBox) {
//    fillWithBoards(new BoardBoxes(List.of(boardBox)));
//    return notationService
//        .findById(boardBox.getNotationId())
//        .map(notation -> {
//          boardBox.setNotationDrives(notation);
//          boardBox.setNotationId(notation.getDomainId());
////    boardBoxStoreService.put(authUser.getUserSession(), boardBox);
//          return boardBox;
//        });
//  }

  @NotNull
  private BoardBox saveAndFillBoard(@NotNull BoardBox boardBox, @NotNull AuthUser authUser) {
    boardBoxRepository.save(boardBox);
    Board board = boardBox.getBoard();
    board.setBoardBox(boardBox.getDomainId());
    boardService.save(board);
    Notation notation = boardBox.getNotation();
    if (boardBox.getEditMode().equals(EnumEditBoardBoxMode.EDIT)
        && notation.getNotationHistory().isEmpty()) {
      notationService.setNotationFenFromBoard(notation, board);
    }
    notationHistoryService.save(notation.getNotationHistory());
    notationService.save(notation, true);
//    boardBox = updateBoardBox(boardBox);
//    boardBoxStoreService.remove(boardBox);
//    boardBoxStoreService.removeByArticleId(boardBox.getArticle());
    return boardBox;
  }

//  private BoardBox findBoardAndPutInStore(BoardBox boardBox, AuthUser authUser) {
//    return boardBoxRepository.find(boardBox);
//  }

//  @NotNull
//  private Mono<BoardBoxes> fillBoardBoxes(@NotNull DomainId article, List<BoardBox> byUserAndIds) {
//    DomainIds domainIds = byUserAndIds
//        .valueList()
//        .stream()
//        .map(BoardBox::getNotationId)
//        .collect(collectingAndThen(toCollection(LinkedList::new), DomainIds::new));
//    fillNotations(byUserAndIds, domainIds);
//    fillWithBoards(byUserAndIds);
//
////    boardBoxStoreService.putByArticleId(authUser.getUserSession(), article, byUserAndIds);
//    return byUserAndIds;
//  }

//  private void throw404IfNotFound(@NotNull BoardBox boardBox) {
//    boardBoxRepository.find(boardBox);
//  }

//  private void fillNotations(BoardBoxes byUserAndIds, DomainIds domainIds) {
//    List<Notation> notationByIds = notationService.findByIds(domainIds);
//    Map<String, Notation> notationMap = notationByIds
//        .stream()
//        .collect(Collectors.toMap(o -> o.getBoardBox().getId(), o -> o));
//
//    // fill BoardBox
//    for (BoardBox boardBox : byUserAndIds.valueList()) {
//      Notation notation = notationMap.get(boardBox.getId());
//      boardBox.setNotationDrives(notation);
//    }
//  }

  private Flux<BoardBox> fillWithBoards(BoardBoxes boardBoxIds) {
    List<DomainId> domainIds = boardBoxIds
        .valueList()
        .stream()
        .map(BoardBox::getDomainId)
        .collect(Collectors.toList());

    Map<DomainId, BoardBox> boardBoxByDomainId = boardBoxIds
        .entrySet()
        .stream()
        .collect(Collectors.toMap(o -> o.getValue().getDomainId(), Map.Entry::getValue));

    Flux<Board> boards = boardRepository.findByBoardBoxIdIn(domainIds);
    return boards.map(board -> {
//      board = boardService.updateBoard(board);
      BoardBox boardBox = boardBoxByDomainId.get(board.getBoardBox());
      board.setReadonly(boardBox.isReadonly());
      boardBox.getPublicBoards().add(board);
      if (boardBox.getBoard().getId().equals(board.getId())) {
        boardBox.setBoard(board);
      }
      return boardBox;
    });
  }

//  @NotNull
//  public BoardBox removeVariant(@NotNull BoardBox boardBox, AuthUser authUser) {
//    Optional<NotationLine> notationLine = notationService.removeVariant(boardBox.getNotationDrives());
//    if (notationLine.isPresent()) {
//      return switchNotationToVariant(notationLine.get(), boardBox, authUser);
//    }
//    Notation byId = notationService.findById(boardBox.getNotationId());
//    boardBox.setNotationDrives(byId);
//    byId.getNotationHistory().setLastSelected(true);
//    byId.getNotationHistory().getNotationDrives().setLastMoveCursor();
//    notationService.save(byId, false);
//    return byId.getNotationHistory().getLastNotationBoardId()
//        .map(board -> {
//          Board board = boardService.findById(board);
//          boardBox.setBoard(board);
//          boardBox.setBoard(board);
//          boardBoxRepository.save(boardBox);
//          return boardBox;
//        })
//        .orElseThrow();
//  }

  private Flux<BoardBox> updateMarkTaskId(BoardBox boardBox, BoardBoxes byArticleId) {
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
    return boardBoxRepository.saveAll(valueList);
//    boardBoxStoreService.removeByArticleId(boardBox.getArticle());
  }
}
