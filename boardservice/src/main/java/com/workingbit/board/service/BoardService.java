package com.workingbit.board.service;

import com.workingbit.board.controller.util.BoardUtils;
import com.workingbit.board.exception.BoardServiceException;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.domain.ICoordinates;
import com.workingbit.share.domain.impl.*;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumRules;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.workingbit.board.BoardEmbedded.*;
import static com.workingbit.board.controller.util.BoardUtils.*;

/**
 * Created by Aleksey Popryaduhin on 13:45 09/08/2017.
 */
public class BoardService {

  private Logger logger = LoggerFactory.getLogger(BoardService.class);

  Board createBoard(@NotNull CreateBoardPayload newBoardRequest) {
    Board board = initBoard(newBoardRequest.isFillBoard(), newBoardRequest.isBlack(),
        newBoardRequest.getRules());
    board.setBoardBoxId(board.getBoardBoxId());
    Utils.setRandomIdAndCreatedAt(board);
    save(board);
    return board;
  }

  Board createBoard(@NotNull Board newBoardRequest, DomainId boardBoxId) {
    Board board = initBoard(!newBoardRequest.getAssignedSquares().isEmpty(), newBoardRequest.isBlack(),
        newBoardRequest.getRules());
    board.setBoardBoxId(boardBoxId);
    Utils.setRandomIdAndCreatedAt(board);
    save(board);
    return board;
  }

  Board initWithDraughtsOnBoard(@NotNull Board board) {
    Board newBoard = initBoard(true, board.isBlack(), board.getRules());
    newBoard.setBoardBoxId(board.getBoardBoxId());
    newBoard.setDomainId(board.getDomainId());
    save(newBoard);
    return newBoard;
  }

  /**
   * Create temp board and use it to emulate moves to populate notation
   */
  void fillNotation(DomainId boardBoxId, @Nullable NotationFen notationFen, DomainId notationId,
                    @NotNull Notation genNotation, EnumRules rules) {
    Board board;
    if (notationFen == null) {
      board = initBoard(true, false, rules);
    } else {
      board = initBoard(false, notationFen.isBlackTurn(), rules);
      for (NotationFen.Square square : notationFen.getWhite().getSquares()) {
        board = addDraughtFen(board, square, false);
      }
      for (NotationFen.Square square : notationFen.getBlack().getSquares()) {
        board = addDraughtFen(board, square, true);
      }
      board.setBoardBoxId(boardBoxId);
      board.setBlackTurn(notationFen.isBlackTurn());
      Utils.setRandomIdAndCreatedAt(board);
      boardDao.save(board);
      genNotation.getNotationFen().setBoardId(board.getDomainId());
    }
    List<Board> batchBoards = new ArrayList<>();
    if (!genNotation.getNotationHistory().isEmpty()) {
      board.setBoardBoxId(boardBoxId);
      populateBoardWithNotation(notationId, board, genNotation, batchBoards);
      if (!genNotation.getNotationHistory().isEqual(genNotation.getNotationHistory())) {
        throw new BoardServiceException(ErrorMessages.UNABLE_TO_PARSE_PDN);
      }
      genNotation.setBoardBoxId(boardBoxId);
      notationDao.save(genNotation);
      boardDao.batchSave(batchBoards);
    }
  }

  Board findById(DomainId boardId) {
    Board board = boardDao.findById(boardId);
    updateBoard(board);
    return board;
  }

  @NotNull
  Board resetHighlightAndUpdate(@NotNull Board board) {
    updateBoard(board);
    resetBoardHighlight(board);
    return board;
  }

  /**
   * @return map of {allowed, captured}
   */
  @NotNull
  Map getHighlight(@NotNull Board serverBoard, @NotNull Board clientBoard) {
    BoardUtils.updateMoveSquaresHighlightAndDraught(serverBoard, clientBoard);
    Square selectedSquare = serverBoard.getSelectedSquare();
    if (isInvalidHighlight(selectedSquare)) {
      throw new BoardServiceException(ErrorMessages.INVALID_HIGHLIGHT);
    }
    MovesList movesList = getHighlightedBoard(serverBoard.isBlackTurn(), serverBoard);
    return Map.of(SERVER_BOARD, serverBoard, MOVES_LIST, movesList);
  }

  public Board move(@NotNull Board clientBoard, @NotNull NotationHistory notationHistory) {
    return move(clientBoard, notationHistory, true);
  }

  public Board move(@NotNull Board clientBoard, @NotNull NotationHistory notationHistory, boolean save) {
    MovesList movesList = getHighlightedBoard(clientBoard.isBlackTurn(), clientBoard);
    Set<Square> allowed = movesList.getAllowed();
    TreeSquare captured = movesList.getCaptured();
    if (allowed.isEmpty()) {
      throw new BoardServiceException(ErrorMessages.UNABLE_TO_MOVE);
    }
    Square nextSquare = clientBoard.getNextSquare();
    for (Square allow : allowed) {
      if (allow.equals(nextSquare)) {
        nextSquare.setHighlight(allow.isHighlight());
        break;
      }
    }
    clientBoard.getSelectedSquare().getDraught().setHighlight(false);
    Map<String, List<Square>> capturedMapped = captured.flatTree()
        .stream()
        .collect(Collectors.groupingBy(ICoordinates::getNotation));
    updateBoardDraughts(capturedMapped, clientBoard.getWhiteDraughts(), clientBoard.getRules().getDimension());
    updateBoardDraughts(capturedMapped, clientBoard.getBlackDraughts(), clientBoard.getRules().getDimension());

    Board nextBoard = clientBoard.deepClone();
    boolean prevBlackTurn = nextBoard.isBlackTurn();
    // MOVE DRAUGHT
    nextBoard = moveDraught(nextBoard, captured, notationHistory);

    if (prevBlackTurn != nextBoard.isBlackTurn()) {
      nextBoard.setSelectedSquare(null);
    }
    if (!nextBoard.getId().equals(clientBoard.getId()) && save) {
      boardDao.save(nextBoard);
    }
    return nextBoard;
  }

  private void updateBoardDraughts(@NotNull Map<String, List<Square>> capturedMapped, @NotNull Map<String, Draught> whiteDraughts, int dimension) {
    whiteDraughts
        .values()
        .forEach(draught -> {
          draught.setDim(dimension);
          List<Square> squares = capturedMapped.get(draught.getNotation());
          if (squares != null && !squares.isEmpty()) {
            Draught capturedDraught = squares.get(0).getDraught();
            draught.setCaptured(capturedDraught.isCaptured());
//            draught.setMarkCaptured(capturedDraught.getMarkCaptured());
          }
        });
  }

  void save(Board board) {
    boardDao.save(board);
  }

  Board addDraught(@NotNull Board currentBoard, String notation, Draught draught) {
    return addDraught(currentBoard, notation, draught, true);
  }

  private Board addDraught(@NotNull Board currentBoard, String notation, Draught draught, boolean save) {
    Board deepClone = currentBoard;
    if (save) {
      deepClone = currentBoard.deepClone();
      deepClone.setBoardBoxId(currentBoard.getBoardBoxId());
      Utils.setRandomIdAndCreatedAt(deepClone);
      boardDao.save(deepClone);
    }
    BoardUtils.addDraught(deepClone, notation, draught);
    return deepClone;
  }

  public void updateBoard(@NotNull Board board) {
    Board boardUpdated = BoardUtils.updateBoard(board);
    board.setWhiteDraughts(boardUpdated.getWhiteDraughts());
    board.setBlackDraughts(boardUpdated.getBlackDraughts());
    board.setSquares(boardUpdated.getSquares());
    board.setPreviousSquare(boardUpdated.getPreviousSquare());
    board.setNextSquare(boardUpdated.getNextSquare());
    board.setSelectedSquare(boardUpdated.getSelectedSquare());
    board.setAssignedSquares(boardUpdated.getAssignedSquares());
  }

  private void populateBoardWithNotation(DomainId notationId, @NotNull Board board,
                                         @NotNull Notation notation,
                                         @NotNull List<Board> batchBoards) {
    NotationHistory recursiveFillNotationHistory = NotationHistory.createWithRoot();
    populateBoardWithNotation(notationId, board, notation, recursiveFillNotationHistory, batchBoards);
    NotationDrive lastDrive = recursiveFillNotationHistory.getLast();
    lastDrive.setSelected(true);
    NotationDrives curNotation = recursiveFillNotationHistory.getNotation();
    NotationDrive lastCurrentDrive = getNotationHistoryColors(curNotation);
    NotationHistory syncNotationHist = recursiveFillNotationHistory.deepClone();
    for (int i = 0; i < syncNotationHist.getNotation().size(); i++) {
      syncNotationHist.setCurrentIndex(i);
      notationService.syncVariants(syncNotationHist, notation);
    }
    setNotationHistoryForNotation(notation, lastCurrentDrive, recursiveFillNotationHistory);
    notationHistoryDao.save(recursiveFillNotationHistory);
    notation.setNotationHistory(recursiveFillNotationHistory);
  }

  private NotationDrive getNotationHistoryColors(NotationDrives curNotation) {
    NotationDrive lastCurrentDrive = null;
    for (NotationDrive curDrive : curNotation) {
      NotationDrives variants = curDrive.getVariants();
      if (!variants.isEmpty()) {
        lastCurrentDrive = curDrive;
        if (curDrive.getVariantsSize() > 0) {
          variants.getFirst().setParentId(0);
          variants.getFirst().setParentColor("purple");
          variants.getFirst().setDriveColor(Utils.getRandomColor());
          for (int i = 1; i < variants.size(); i++) {
            NotationDrive notationDrive = variants.get(i);
            notationDrive.setParentColor(variants.get(i - 1).getDriveColor());
            notationDrive.setDriveColor(Utils.getRandomColor());
          }
          if (curDrive.getVariantsSize() - 2 >= variants.size()) {
            variants.get(curDrive.getVariantsSize() - 2).setPrevious(true);
          }
        }
//        variants.getFirst().setCurrent(true);
      }
    }
    return lastCurrentDrive;
  }

  private void setNotationHistoryForNotation(@NotNull Notation notation, NotationDrive lastCurrentDrive, NotationHistory notationHistory) {
    NotationDrives curNotation = notationHistory.getNotation();
    if (lastCurrentDrive == null) {
      lastCurrentDrive = curNotation.getLast();
    }
//    lastCurrentDrive.setCurrent(true);
    int currentIndex = curNotation.indexOf(lastCurrentDrive);
    int variantIndex = lastCurrentDrive.getVariantsSize() == 0 ? 0 : lastCurrentDrive.getVariantsSize() - 1;
    NotationLine line = new NotationLine(currentIndex, variantIndex);
    notation.findNotationHistoryByLine(line)
        .ifPresentOrElse(nh -> {
          notation.setNotationHistory(nh);
          notation.setNotationHistoryId(nh.getDomainId());
        }, () -> {
          notation.setNotationHistory(notationHistory);
          notation.setNotationHistoryId(notationHistory.getDomainId());
        });
  }

  private void populateBoardWithNotation(DomainId notationId, @NotNull Board board,
                                         @NotNull Notation notation,
                                         @NotNull NotationHistory recursiveNotationHistory,
                                         @NotNull List<Board> batchBoards
  ) {
    Board recursiveBoard = board.deepClone();
    Utils.setRandomIdAndCreatedAt(recursiveNotationHistory);
    recursiveNotationHistory.setNotationId(notationId);
    recursiveNotationHistory.setCurrentIndex(0);
    recursiveNotationHistory.setVariantIndex(0);
    NotationDrives notationMoves = notation.getNotationHistory().getNotation();
    for (NotationDrive notationDrive : notationMoves) {
      NotationDrives variantsPopulated = new NotationDrives();
      if (!notationDrive.getVariants().isEmpty()) {
        NotationDrives variants = notationDrive.getVariants();
        int idInVariants = 0;
        for (NotationDrive vDrive : variants) {
          NotationHistory vHistory = recursiveNotationHistory.deepClone();
          Utils.setRandomIdAndCreatedAt(vHistory);
          vDrive.setIdInVariants(idInVariants);
          vDrive.setNotationHistoryId(vHistory.getDomainId());
          NotationDrive popVDrive = vHistory.getLast();
          vDrive.setMoves(popVDrive.getMoves());
          vHistory.setCurrentIndex(recursiveNotationHistory.size() - 1);
          vHistory.setVariantIndex(idInVariants);
          idInVariants++;
          vHistory.setNotationId(notationId);
          NotationDrives curVariants = vDrive.getVariants();
          if (curVariants.size() > 1) {
            NotationHistory subRecursiveNotationHistory = NotationHistory.createWithRoot();
            subRecursiveNotationHistory.setNotationLine(new NotationLine(0, 0));
            Board subBoard = recursiveBoard.deepClone();
            for (NotationDrive subDrive : curVariants) {
              NotationMoves vDrives = subDrive.getMoves();
              for (NotationMove drive : vDrives) {
                subBoard = emulateMove(drive, subBoard, subRecursiveNotationHistory, batchBoards);
              }
              subDrive.setMoves(subRecursiveNotationHistory.getLast().getMoves());
            }
            NotationDrives subNotation = subRecursiveNotationHistory.getNotation();
            subNotation.removeFirst();
            vHistory.addAll(subNotation);
          } else {
            NotationDrive first = curVariants.getFirst();
            first.setNotationHistoryId(vHistory.getDomainId());
            first.setMoves(vDrive.getMoves());
          }
          notation.addForkedNotationHistory(vHistory);
          variantsPopulated.add(vDrive);
        }
      }
      int i = notationMoves.indexOf(notationDrive);
      NotationMoves drives = notationDrive.getMoves();
      for (NotationMove drive : drives) {
        recursiveBoard = emulateMove(drive, recursiveBoard, recursiveNotationHistory, batchBoards);
      }
      setMetaInformation(recursiveNotationHistory, notationDrive);
      if (!variantsPopulated.isEmpty()) {
        for (NotationDrive drive : variantsPopulated) {
          if (recursiveNotationHistory.get(i).getMoves().equals(drive.getVariants().getFirst().getMoves())) {
            drive.setCurrent(true);
            break;
          }
        }
        recursiveNotationHistory.get(i - 1).addAllVariants(variantsPopulated);
      }
    }
    notation.addForkedNotationHistory(recursiveNotationHistory);
  }

  private Board emulateMove(@Nullable NotationMove notationMove, Board serverBoard, @NotNull NotationHistory notationHistory, @NotNull List<Board> batchBoards) {
    if (notationMove == null) {
      return serverBoard;
    }
    serverBoard = serverBoard.deepClone();
    List<String> moves = notationMove.getMoveNotations();
    String move = moves.get(0);
    for (int i = 1; i < moves.size(); i++) {
      Square selected = findSquareByNotationWithHint(move, moves.subList(i - 1, moves.size()), serverBoard, notationMove.getNotationFormat());
      serverBoard.setSelectedSquare(selected);
      move = moves.get(i);
      Square next = findSquareByNotationWithHint(move, moves.subList(i, moves.size()), serverBoard, notationMove.getNotationFormat());
      next.setHighlight(true);
      serverBoard.setNextSquare(next);
      Board clientBoard = serverBoard.deepClone();
      serverBoard = move(clientBoard, notationHistory, false);
      move = moves.get(i);
    }
    batchBoards.add(serverBoard);
    return serverBoard;
  }

  private Board moveDraught(@NotNull Board board, @NotNull TreeSquare capturedSquares,
                            NotationHistory notationHistory) {
    notationHistory.getNotation().getLast().setSelected(false);
    performMoveDraught(board, capturedSquares);
    Board newBoard = board.deepClone();
    boolean blackTurn = board.isBlackTurn();
    MovesList nextHighlight = getHighlightedBoard(blackTurn, newBoard);
    boolean previousCaptured = !capturedSquares.isEmpty();
    boolean hasNextCapture = isNextCapture(board, nextHighlight);
    if (previousCaptured && hasNextCapture) {
      updateNotationMiddle(newBoard, notationHistory);
      return newBoard;
    }
    updateNotationEnd(board, notationHistory, previousCaptured);
    resetBoardHighlight(board);
    resetCaptured(board);
    return board;
  }

  @NotNull
  private Board addDraughtFen(@NotNull Board board, NotationFen.Square square, boolean black) {
    Square sq = findSquareByNotation(square.getNumber(), board);
    Draught draught = new Draught(sq.getV(), sq.getH(), sq.getDim(), black, square.isK());
    board = addDraught(board, sq.getNotation(), draught, false);
    return board;
  }

  private boolean isNextCapture(@NotNull Board board, MovesList nextHighlight) {
    List<Draught> captured = board.getAssignedSquares()
        .stream()
        .filter(Square::isOccupied)
        .map(Square::getDraught)
        .filter(draught -> draught.getMarkCaptured() != 0)
        .collect(Collectors.toList());
    return !captured.isEmpty()
        && nextHighlight.getCaptured()
        .flatTree()
        .stream()
        .map(Square::getDraught)
        .collect(Collectors.toList())
        .containsAll(captured);
  }

  Square getPredictedSelectedSquare(Board board) {
    return BoardUtils.getPredictedSelectedSquare(board);
  }

  void updateAssigned(Board board) {
    List<Square> prevSquares = board.getSquares();
    List<Square> prevAssignedSquares = prevSquares
        .stream()
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    board.setAssignedSquares(prevAssignedSquares);
    boardService.updateBoard(board);
  }

  private void setMetaInformation(@NotNull NotationHistory recursiveNotationHistory, NotationDrive notationDrive) {
    NotationDrive last = recursiveNotationHistory.getLast();
    String comment = notationDrive.getComment();
    if (StringUtils.isNotBlank(comment)) {
      last.setComment(comment.substring(1, comment.length() - 1));
    }
    for (int i = 0; i < last.getMoves().size(); i++) {
      NotationMove m = last.getMoves().get(i);
      NotationMove mParsed = notationDrive.getMoves().get(i);
      m.setMoveStrength(mParsed.getMoveStrength());
    }
  }
}
