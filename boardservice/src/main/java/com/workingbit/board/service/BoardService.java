package com.workingbit.board.service;

import com.workingbit.board.controller.util.BoardUtils;
import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.Draught;
import com.workingbit.share.domain.impl.Square;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumRules;
import com.workingbit.share.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.workingbit.board.BoardEmbedded.boardDao;
import static com.workingbit.board.controller.util.BoardUtils.*;

/**
 * Created by Aleksey Popryaduhin on 13:45 09/08/2017.
 */
public class BoardService {

  private Logger logger = LoggerFactory.getLogger(BoardService.class);

  Board createBoard(CreateBoardPayload newBoardRequest) {
    Board board = initBoard(newBoardRequest.isFillBoard(), newBoardRequest.isBlack(),
        newBoardRequest.getRules());
    board.setBoardBoxId(board.getBoardBoxId());
    Utils.setRandomIdAndCreatedAt(board);
    save(board);
    return board;
  }

  Board initWithDraughtsOnBoard(Board board) {
    Board newBoard = initBoard(true, board.isBlack(), board.getRules());
    board.setBoardBoxId(board.getBoardBoxId());
    newBoard.setDomainId(board.getDomainId());
    save(newBoard);
    return newBoard;
  }

  /**
   * Create temp board and use it to emulate moves to populate notation
   *  @param boardBoxId
   * @param genNotationHistory
   * @param fillNotationHistory
   * @param rules
   */
  void fillNotation(DomainId boardBoxId, NotationHistory genNotationHistory, NotationHistory fillNotationHistory, EnumRules rules) {
    Board board = initBoard(true, false, rules);
    board.setBoardBoxId(board.getBoardBoxId());
    List<Board> batchBoards = new ArrayList<>();
    board.setBoardBoxId(boardBoxId);
    populateBoardWithNotation(board, genNotationHistory, fillNotationHistory, batchBoards);
    fillNotationHistory.setRules(board.getRules());
    if (!genNotationHistory.isEqual(fillNotationHistory)) {
      throw new BoardServiceException(ErrorMessages.UNABLE_TO_PARSE_PDN);
    }
    boardDao.batchSave(batchBoards);
  }

  Board findById(DomainId boardId) {
    Board board = boardDao.findById(boardId);
    updateBoard(board);
    return board;
  }

  Board resetHighlightAndUpdate(Board board) {
    updateBoard(board);
    resetBoardHighlight(board);
    return board;
  }


  void delete(DomainId boardId) {
    boardDao.delete(boardId);
  }

  /**
   * @return map of {allowed, captured}
   */
  Map getHighlight(Board serverBoard, Board clientBoard) {
    BoardUtils.updateMoveSquaresHighlightAndDraught(serverBoard, clientBoard);
    Square selectedSquare = serverBoard.getSelectedSquare();
    if (isValidHighlight(selectedSquare)) {
      throw new BoardServiceException(ErrorMessages.INVALID_HIGHLIGHT);
    }
    MovesList movesList = getHighlightedBoard(serverBoard.isBlackTurn(), serverBoard);
    return Map.of("serverBoard", serverBoard, "movesList", movesList);
  }

  private boolean isValidHighlight(Square selectedSquare) {
    return selectedSquare == null
        || !selectedSquare.isOccupied();
  }

  public Board move(Board serverBoard, Board clientBoard, NotationHistory notationHistory) {
    return move(serverBoard, clientBoard, notationHistory, true);
  }

  /**
   * @param serverBoard map of {boardId: String, selectedSquare: Square, targetSquare: Square, allowed: List<Square>, captured: List<Square>}  @return Move info:
   *                    {v, h, targetSquare, queen} v - distance for moving vertical (minus up),
   *                    h - distance for move horizontal (minus left), targetSquare is a new square with
   * @param clientBoard
   */
  public Board move(Board serverBoard, Board clientBoard, NotationHistory notationHistory, boolean save) {
    Map highlight = getHighlight(serverBoard, clientBoard);
    serverBoard = (Board) highlight.get("serverBoard");
    MovesList movesList = (MovesList) highlight.get("movesList");
    Set<Square> allowed = movesList.getAllowed();
    Set<Square> captured = movesList.getCaptured();
    if (allowed.isEmpty()) {
      throw new BoardServiceException(ErrorMessages.UNABLE_TO_MOVE);
    }
    boolean isEmulate = serverBoard.getId().equals(clientBoard.getId());
    if (isEmulate) {
      serverBoard.getNextSquare().setHighlight(clientBoard.getNextSquare().isHighlight());
    }
    serverBoard.getSelectedSquare().getDraught().setHighlight(false);
    if (save) {
      boardDao.save(serverBoard);
    }

    Board nextBoard = serverBoard.deepClone();
    Utils.setRandomIdAndCreatedAt(nextBoard);
    DomainId boardId = nextBoard.getDomainId();
    // MOVE DRAUGHT
    nextBoard = BoardUtils.moveDraught(nextBoard, captured, boardId, notationHistory);

    if (save) {
      boardDao.save(nextBoard);
    }
    return nextBoard;
  }

  void save(Board board) {
    boardDao.save(board);
  }

  Board addDraught(Board currentBoard, String notation, Draught draught) {
    Board deepClone = currentBoard.deepClone();
    deepClone.setBoardBoxId(currentBoard.getBoardBoxId());
    Utils.setRandomIdAndCreatedAt(deepClone);
    BoardUtils.addDraught(deepClone, notation, draught);
    boardDao.save(deepClone);
    return deepClone;
  }

//  Optional<Board> undo(Board currentBoard) {
//    String previousId = currentBoard.popPreviousBoard();
//    if (StringUtils.isBlank(previousId)) {
//      return Optional.empty();
//    }
//    boardDao.save(currentBoard);
//    return findPublicById(previousId).map(undoneBoard -> {
//      String selectedMove = currentBoard.getPreviousSquare().getNotation();
//      String possibleMove = currentBoard.getSelectedSquare().getNotation();
//      undoneBoard.pushNextBoard(currentBoard.getId(), selectedMove, possibleMove);
//
//      // reset highlights
//      undoneBoard.getSelectedSquare().getDraught().setHighlight(false);
//      undoneBoard.getNextSquare().setHighlight(false);
//
//      return undoneBoard;
//    });
//  }

//  Optional<Board> redo(Board currentBoard) {
//    String nextId = currentBoard.popNextBoard();
//    if (StringUtils.isBlank(nextId)) {
//      return Optional.empty();
//    }
//    boardDao.save(currentBoard);
//    return findPublicById(nextId).map(redoneBoard -> {
//      Square square = getNextOrPrevSquare(currentBoard);
//      String notation = square != null ? square.getNotation() : null;
//      redoneBoard.pushPreviousBoard(currentBoard.getId(),
//          notation,
//          currentBoard.getSelectedSquare().getNotation());
//      return redoneBoard;
//    });
//  }

//  private Square getNextOrPrevSquare(Board currentBoard) {
//    return currentBoard.getNextSquare() == null
//        ? currentBoard.getPreviousSquare() : currentBoard.getNextSquare();
//  }

  void updateBoard(Board board) {
    Board boardUpdated = BoardUtils.updateBoard(board);
    board.setSquares(boardUpdated.getSquares());
    board.setPreviousSquare(boardUpdated.getPreviousSquare());
    board.setNextSquare(boardUpdated.getNextSquare());
    board.setSelectedSquare(boardUpdated.getSelectedSquare());
    board.setAssignedSquares(boardUpdated.getAssignedSquares());
  }

  private void populateBoardWithNotation(Board board, NotationHistory genNotationHistory,
                                         NotationHistory fillNotationHistory,
                                         List<Board> batchBoards) {
    NotationHistory recursiveFillNotationHistory = NotationHistory.createWithRoot();
    populateBoardWithNotation(board, genNotationHistory, recursiveFillNotationHistory, fillNotationHistory, batchBoards, 0);
    fillNotationHistory.setHistory(fillNotationHistory.getNotation());
  }

  private void populateBoardWithNotation(Board board,
                                         NotationHistory genNotationHistory,
                                         NotationHistory recursiveNotationHistory,
                                         NotationHistory fillNotationHistory,
                                         List<Board> batchBoards,
                                         int deep
  ) {
    ++deep;
    Board recursiveBoard = board;
    if (deep == 1) {
      recursiveBoard = board.deepClone();
    }
    NotationDrives innerNotations = new NotationDrives();
    for (NotationDrive notationDrive : genNotationHistory.getNotation()) {
      NotationMoves drives = notationDrive.getMoves();
      for (NotationMove drive : drives) {
        logger.trace("EMULATE MOVE: " + drive.asString());
        recursiveBoard = emulateMove(drive, recursiveBoard, recursiveNotationHistory, batchBoards);
      }
      if (!notationDrive.getVariants().isEmpty()) {
        NotationDrives variants = notationDrive.getVariants();
        NotationDrives recurNotations = new NotationDrives();
        for (NotationDrive vDrive : variants) {
          // create history from current drive's variant
          NotationHistory variantsNotationHistory = NotationHistory.create();
          variantsNotationHistory.setNotation(vDrive.getVariants());

          // sync populate notation
          NotationHistory tempNotationHistory = NotationHistory.createWithRoot();
          populateBoardWithNotation(recursiveBoard, variantsNotationHistory, tempNotationHistory, fillNotationHistory, batchBoards, deep);
          NotationDrives tNotation = tempNotationHistory.getNotation();
          tNotation.removeFirst();
          if (tNotation.isEmpty()) {
            NotationDrive topDrive = recursiveNotationHistory.getNotation().getLast();
            NotationDrive topVariant = new NotationDrive();
            topVariant.getVariants().addAll(fillNotationHistory.getNotation());
            topDrive.getVariants().add(topVariant);
            innerNotations.add(topDrive);
            fillNotationHistory.getNotation().clear();
            continue;
          }
          NotationHistory tNotationHistory = NotationHistory.create();
          tNotationHistory.setNotation(tNotation);
          NotationDrive tDrive = vDrive.deepClone();
          tDrive.setMoves(tNotation.getFirst().getMoves());
          tDrive.setVariants(tNotation);
          recurNotations.add(tDrive);
        }
        if (recurNotations.isEmpty()) {
          continue;
        }
        NotationDrive tvDrive = notationDrive.deepClone();
        if (!notationDrive.getMoves().isEmpty()) {
          tvDrive.setMoves(recursiveNotationHistory.getLast().getMoves());
        }
        tvDrive.setVariants(recurNotations);
        innerNotations.add(tvDrive);
      } else if (deep == 1) {
        innerNotations.add(recursiveNotationHistory.getLast());
      }
    }
    fillNotationHistory.getNotation().addAll(innerNotations);
  }

  private Board emulateMove(NotationMove notationMove, Board serverBoard, NotationHistory notationHistory, List<Board> batchBoards) {
    if (notationMove == null) {
      return serverBoard;
    }
    serverBoard = serverBoard.deepClone();
    List<String> moves = notationMove.getMoveNotationsPdn();
    Iterator<String> iterator = moves.iterator();
    Utils.setRandomIdAndCreatedAt(serverBoard);
    batchBoards.add(serverBoard);
    for (String move = iterator.next(); iterator.hasNext(); ) {
      Square selected = findSquareByNotation(move, serverBoard).deepClone();
      serverBoard.setSelectedSquare(selected.deepClone());
      move = iterator.next();
      Square next = findSquareByNotation(move, serverBoard).deepClone();
      next.setHighlight(true);
      serverBoard.setNextSquare(next);
      Board clientBoard = serverBoard.deepClone();
      serverBoard = move(serverBoard, clientBoard, notationHistory, false);
      batchBoards.add(serverBoard);

      selected = findSquareByNotation(selected.getNotation(), serverBoard).deepClone();
      next = findSquareByNotation(next.getNotation(), serverBoard).deepClone();

      if (selected.isOccupied()) {
        System.out.println(selected);
      }
      if (!next.isOccupied()) {
        System.out.println(next);
      }

      if (!iterator.hasNext()) {
        break;
      }
    }
//    notationHistory.setFormat(EnumNotationFormat.ЧИСЛОВОЙ);
    return serverBoard;
  }

  public Board clearBoard(Board board) {
    board.setNextSquare(null);
    board.setSelectedSquare(null);
    board.setPreviousSquare(null);
    board.setWhiteDraughts(new HashMap<>());
    board.setBlackDraughts(new HashMap<>());
    board.setDriveCount(0);
    board.setBlackTurn(false);
    board.setSquares(new ArrayList<>());

    save(board);
    updateBoard(board);
    return board;
  }
}
