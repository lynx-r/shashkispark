package com.workingbit.board.service;

import com.workingbit.share.domain.impl.*;
import com.workingbit.share.exception.DaoException;
import com.workingbit.share.model.*;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.workingbit.board.BoardEmbedded.*;
import static com.workingbit.share.util.Utils.isCorrespondedNotation;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

/**
 * Created by Aleksey Popryadukhin on 14/04/2018.
 */
public class NotationService {

  private Logger logger = LoggerFactory.getLogger(NotationService.class);

  void save(@NotNull Notation notation, boolean fill) {
    notationDao.save(notation);
//    notationStoreService.removeNotation(notation);
    if (fill) {
      fillNotation(notation);
    }
  }

  /**
   * does not do actually saving
   *
   * @param boardBox populated boardbox
   */
  void createNotationForBoardBox(@NotNull BoardBox boardBox) {
    Notation notation = new Notation();
    Utils.setRandomIdAndCreatedAt(notation);
    notation.getNotationFen().setBoardId(boardBox.getBoardId());

    notationHistoryService.createNotationHistoryForNotation(notation);

    notation.setBoardBoxId(boardBox.getDomainId());
    notation.setRules(boardBox.getBoard().getRules());
    boardBox.setNotationId(notation.getDomainId());
    boardBox.setNotation(notation);
  }

  void clearNotationInBoardBox(@NotNull BoardBox bb) {
    var notation = notationDao.findById(bb.getNotationId());
    notationHistoryService.deleteByNotationId(bb.getNotationId());
    notationHistoryService.createNotationHistoryForNotation(notation);
    notationHistoryDao.save(notation.getNotationHistory());
    notation.setNotationFen(new NotationFen());
    save(notation, false);
    bb.setNotation(notation);
    boardBoxDao.save(bb);
  }

  Optional<NotationLine> removeVariant(Notation notation) {
    fillNotation(notation);
    NotationLine notationLine = notation.getNotationHistory().getNotationLine();
    return notation.findNotationHistoryByLine(notationLine)
        .map(toRemoveNotationHistory -> {
          toRemoveNotationHistory.getCurrentNotationDrive()
              .ifPresent(curDrive -> curDrive.getVariantById(notationLine.getVariantIndex())
                  .ifPresent(curVariant -> {
                    DomainIds boardIdsToRemove = curVariant
                        .getVariants()
                        .subList(1, curVariant.getVariantsSize())
                        .stream()
                        .map(NotationDrive::getMoves)
                        .flatMap(Collection::stream)
                        .map(NotationMove::getBoardId)
                        .distinct()
                        .collect(collectingAndThen(toCollection(LinkedList::new), DomainIds::new));
                    boardIdsToRemove.removeFirst();
                    boardDao.batchDelete(boardIdsToRemove);
                  })
              );
          toRemoveNotationHistory.removeByCurrentIndex();
          notationHistoryDao.delete(toRemoveNotationHistory.getDomainId());
          notation.removeForkedNotations(toRemoveNotationHistory);
          return toRemoveNotationHistory.getCurrentNotationDrive()
              .map(current -> {
                NotationHistory nh = toRemoveNotationHistory;
                NotationDrives variants = current.getVariants();
                if (variants.size() > 1) {
                  removeOneOfVariant(nh, variants);
                } else {
                  nh = removeLastVariant(notation, toRemoveNotationHistory, current, nh);
                }
                syncVariants(nh, notation);
                return variants.isEmpty() ? null : nh.getNotationLine();
              })
              .orElse(null);
        });
  }

  void setNotationFenFromBoard(@NotNull Notation notation, @NotNull Board board) {
    NotationFen notationFen = new NotationFen();
    notationFen.setBoardId(board.getDomainId());
    int dimension = board.getRules().getDimension();
    updateDraughtsDimension(dimension, board.getBlackDraughts());
    updateDraughtsDimension(dimension, board.getWhiteDraughts());
    notationFen.setBlackTurn(board.isBlackTurn());
    notationFen.setSequenceFromBoard(board.getBlackDraughts(), true);
    notationFen.setSequenceFromBoard(board.getWhiteDraughts(), false);
    notation.setNotationFen(notationFen);
  }

  Notation findById(@NotNull DomainId notationId) {
//    if (authUser == null) {
//      throw RequestException.notFound404();
//    }
    Notation byId = notationDao.findById(notationId);
    fillNotation(byId);
//          notationStoreService.putNotation(authUser.getUserSession(), byId);
    return byId;
  }

  Notation forkAt(int forkFromNotationDrive, Notation notation) {
    NotationHistory newNotationHistory = notationHistoryService.forkAt(forkFromNotationDrive, notation);
    notation.setNotationHistory(newNotationHistory);
    notation.setNotationHistoryId(newNotationHistory.getDomainId());
    save(notation, false);
    return notation;
  }

  Notation switchTo(NotationLine notationLine, Notation notation) {
    return notation.findNotationHistoryByLine(notationLine)
        .map(notationHistory -> {
          notationHistory.getNotation().replaceAll(notationDrive -> {
            notationDrive.setSelected(false);
            return notationDrive;
          });
          return notationHistory.getCurrentNotationDrive()
              .map(current -> {
                current.resetCursor();
                Integer prevVariantId = notation.getPrevVariantId() == null ? -1 : notation.getPrevVariantId();
                setVariantDriveMarkers(notationLine, notation, current, prevVariantId);
                notationHistory.setLastMoveCursor();
                notationHistory.getLast().setSelected(true);
                notationHistoryDao.save(notationHistory);
                notation.setNotationHistory(notationHistory);
                notation.setNotationHistoryId(notationHistory.getDomainId());
                save(notation, false);
                return notation;
              })
              .orElse(notation);
        })
        .orElse(notation);
  }

  List<Notation> findByIds(DomainIds domainIds) {
    List<Notation> byIds = notationDao.findByIds(domainIds);
    fillNotationByNotationIds(byIds);
    return byIds;
  }

  void syncSubVariants(NotationHistory toSyncNotationHist, Notation notation) {
    toSyncNotationHist.getCurrentVariant()
        .ifPresent(currentSyncVariant -> {
          notation.getForkedNotations().replaceAll((s, notationHistory) -> {
            if (isCorrespondedNotation(toSyncNotationHist, notationHistory)) {
              NotationDrive cur = notationHistory.get(toSyncNotationHist.getCurrentIndex());
              cur.getVariantById(toSyncNotationHist.getVariantIndex())
                  .ifPresent(curVariant -> {
                    complementMoves(curVariant, currentSyncVariant.getMoves());
                    curVariant.setVariants(currentSyncVariant.getVariants());
                  });
            }
            return notationHistory;
          });
          notationHistoryDao.batchSave(notation.getForkedNotations().values());
        });
  }

  void syncVariants(NotationHistory toSyncNotationHist, Notation notation) {
    toSyncNotationHist.getCurrentNotationDrive()
        .ifPresent(currentNotationDrive -> {
          AtomicBoolean save = new AtomicBoolean(false);
          notation.getForkedNotations().replaceAll((s, notationHistory) -> {
            if (isCorrespondedNotation(toSyncNotationHist, notationHistory)) {
              NotationDrive cur = notationHistory.get(toSyncNotationHist.getCurrentIndex());
              cur.setCurrentWithVariant(currentNotationDrive.isCurrent());
              cur.setPreviousWithVariant(currentNotationDrive.isPrevious());
              cur.setVariants(currentNotationDrive.getVariants());
              cur.setBoardDimension(notation.getRules().getDimension());
              save.set(true);
            }
            return notationHistory;
          });
          if (save.get()) {
            notation.syncFormatAndRules();
            notationHistoryDao.batchSave(notation.getForkedNotations().values());
          }
        });
  }

  void deleteById(DomainId notationId) {
    notationDao.delete(notationId.getDomainId());
//    notationStoreService.removeNotationById(notationId);
  }

  void populateBoardWithNotation(DomainId notationId, @NotNull Board board,
                                 @NotNull Notation notation,
                                 @NotNull List<Board> batchBoards) {
    int startMovingFrom = notation.getNotationHistory().get(1).getNotationNumberInt() - 1;
    board.setDriveCount(startMovingFrom);
    NotationHistory recursiveFillNotationHistory = NotationHistory.createWithRoot();
    recursiveFillNotationHistory.getFirst().setNotationNumberInt(startMovingFrom);
    recursiveFillNotationHistory.setStartMovingFrom(startMovingFrom);
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
    notation.setNotationHistory(recursiveFillNotationHistory);
    notation.syncFormatAndRules();
    notationHistoryDao.save(recursiveFillNotationHistory);
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
          vHistory.setCurrentIndex(recursiveNotationHistory.size());
          vHistory.setVariantIndex(idInVariants);
          idInVariants++;
          vHistory.setNotationId(notationId);
          NotationDrives curVariants = vDrive.getVariants();
          if (!curVariants.isEmpty()) {
            NotationHistory subRecursiveNotationHistory = NotationHistory.createWithRoot();
            int startMovingFrom = vDrive.getNotationNumberInt();
            subRecursiveNotationHistory.getFirst().setNotationNumberInt(startMovingFrom);
            subRecursiveNotationHistory.setStartMovingFrom(startMovingFrom);
            subRecursiveNotationHistory.setNotationLine(new NotationLine(0, 0));
            Board subBoard = recursiveBoard.deepClone();
            for (NotationDrive subDrive : curVariants) {
              NotationMoves vDrives = subDrive.getMoves();
              for (NotationMove drive : vDrives) {
                subBoard = boardService.emulateMove(drive, subBoard, subRecursiveNotationHistory, batchBoards);
              }
            }
            NotationDrives subNotation = subRecursiveNotationHistory.getNotation();
            subNotation.removeFirst();
            vHistory.addAll(subNotation);
            vDrive.setMoves(subNotation.getFirst().getMoves());
            vDrive.setVariants(subNotation);
          } else {
            NotationDrive first = curVariants.getFirst();
            first.setNotationHistoryId(vHistory.getDomainId());
            NotationHistory subRecursiveNotationHistory = NotationHistory.createWithRoot();
            subRecursiveNotationHistory.setNotationLine(new NotationLine(0, 0));
            Board subBoard = recursiveBoard.deepClone();
            for (NotationMove drive : first.getMoves()) {
              subBoard = boardService.emulateMove(drive, subBoard, subRecursiveNotationHistory, batchBoards);
            }
            first.setMoves(subRecursiveNotationHistory.getLast().getMoves());
            vDrive.setMoves(first.getMoves());
            vHistory.add(first);
          }
          notation.addForkedNotationHistory(vHistory);
          variantsPopulated.add(vDrive);
        }
      }
      NotationMoves drives = notationDrive.getMoves();
      for (NotationMove drive : drives) {
        recursiveBoard = boardService.emulateMove(drive, recursiveBoard, recursiveNotationHistory, batchBoards);
      }
      setMetaInformation(recursiveNotationHistory, notationDrive);
      if (!variantsPopulated.isEmpty()) {
        // проходим по только что заполнеными вариантам и отмечаем текущий по совпадению ходов и количеству
        // вариантов
        Map<Integer, Integer> idSizeDrive = new HashMap<>();
        for (NotationDrive drive : variantsPopulated) {
          if (recursiveNotationHistory.getLast().getMoves().equals(drive.getVariants().getFirst().getMoves())) {
            idSizeDrive.put(drive.getIdInVariants(), drive.getVariantsSize());
          }
        }
        int maxSize = idSizeDrive.values()
            .stream()
            .mapToInt(value -> value)
            .max()
            .orElse(0);
        Integer maxSizeId = idSizeDrive.entrySet()
            .stream()
            .filter(integerIntegerEntry -> integerIntegerEntry.getValue() == maxSize)
            .findFirst()
            .map(Map.Entry::getKey)
            .orElse(0);
        variantsPopulated
            .stream()
            .filter(nd -> nd.getIdInVariants() == maxSizeId)
            .findFirst()
            .ifPresent(nd -> nd.setCurrentWithVariant(true));
        recursiveNotationHistory.getLast().addAllVariants(variantsPopulated);
      }
    }
    notation.addForkedNotationHistory(recursiveNotationHistory);
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
            NotationDrive parent = variants.get(i - 1);
            parent.setAncestors(parent.getAncestors() + 1);
            notationDrive.setParentId(parent.getIdInVariants());
            notationDrive.setParentColor(parent.getDriveColor());
            notationDrive.setDriveColor(Utils.getRandomColor());
          }
          if (curDrive.getVariantsSize() - 2 >= variants.size()) {
            variants.get(curDrive.getVariantsSize() - 2).setPreviousWithVariant(true);
          }
        }
      }
    }
    return lastCurrentDrive;
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

  private void setNotationHistoryForNotation(@NotNull Notation notation, NotationDrive lastCurrentDrive, NotationHistory notationHistory) {
    NotationDrives curNotation = notationHistory.getNotation();
    if (lastCurrentDrive == null) {
      lastCurrentDrive = curNotation.getLast();
    }
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

  private void fillNotation(Notation notation) {
    try {
      List<NotationHistory> byNotationId = notationHistoryDao.findByNotationId(notation.getDomainId());
      byNotationId
          .stream()
          .filter(notationHistory -> notation.getNotationHistoryId().equals(notationHistory.getDomainId()))
          .findFirst()
          .ifPresent(notation::setNotationHistory);
      notation.addForkedNotationHistories(byNotationId);
      notation.syncFormatAndRules();
    } catch (DaoException e) {
      if (e.getCode() != HTTP_NOT_FOUND) {
        logger.error(e.getMessage(), e);
      }
    }
  }

  private void fillNotationByNotationIds(List<Notation> notations) {
    try {
      DomainIds notationIds = notations
          .stream()
          .map(Notation::getDomainId)
          .collect(collectingAndThen(toCollection(LinkedList::new), DomainIds::new));
      Map<DomainId, List<NotationHistory>> historyByNotationId = notationHistoryDao.findByNotationIds(notationIds)
          .stream()
          .collect(Collectors.groupingBy(NotationHistory::getNotationId));

      for (Notation notation : notations) {
        List<NotationHistory> byNotationId = historyByNotationId.get(notation.getDomainId());
        if (byNotationId == null) {
          continue;
        }
        byNotationId
            .stream()
            .filter(notationHistory -> notation.getNotationHistoryId().equals(notationHistory.getDomainId()))
            .findFirst()
            .ifPresent(notation::setNotationHistory);
        notation.addForkedNotationHistories(byNotationId);
        notation.syncFormatAndRules();
      }
    } catch (DaoException e) {
      if (e.getCode() != HTTP_NOT_FOUND) {
        logger.error(e.getMessage(), e);
      }
    }
  }

  private void setVariantDriveMarkers(NotationLine notationLine, Notation notation, NotationDrive current, Integer prevVariantId) {
    AtomicBoolean isPrevious = new AtomicBoolean();
    current.getVariants()
        .replaceAll(notationDrive -> {
          if (!isPrevious.get()) {
            isPrevious.set(notationDrive.getIdInVariants() == prevVariantId);
          }
          notationDrive.setPreviousWithVariant(notationDrive.isCurrent());
          boolean isCurrent = notationDrive.getIdInVariants() == notationLine.getVariantIndex();
          notationDrive.setCurrentWithVariant(isCurrent);
          if (isCurrent) {
            notation.setPrevVariantId(notationDrive.getIdInVariants());
          }
          return notationDrive;
        });
    if (isPrevious.get()) {
      current.getVariants()
          .replaceAll(notationDrive -> {
            boolean prev = notationDrive.getIdInVariants() == prevVariantId;
            notationDrive.setPreviousWithVariant(prev);
            return notationDrive;
          });
    }
  }

  private void complementMoves(NotationDrive notationDrive, NotationMoves moves) {
    NotationMoves movesOrig = notationDrive.getMoves();
    if (moves.size() != movesOrig.size()) {
      for (int i = movesOrig.size(); i < moves.size(); i++) {
        NotationMove move = moves.get(i);
        NotationMove moveOrig = move.deepClone();
        moveOrig.setMove(new LinkedList<>(move.getMove()));
        movesOrig.add(moveOrig);
      }
    }
  }

  @NotNull
  private NotationHistory removeLastVariant(Notation notation, NotationHistory notationHistory, NotationDrive current, NotationHistory nh) {
    NotationDrive lastVariant = current.getVariants().getFirst();
    current.getVariants().clear();
    Collection<NotationHistory> values = new ArrayList<>(notation.getForkedNotations().values());
    values.stream()
        .filter(toDelete -> toDelete.getCurrentIndex().equals(notationHistory.getCurrentIndex()))
        .forEach(toDelete -> {
          notationHistoryDao.delete(toDelete.getDomainId());
          notation.removeForkedNotations(toDelete);
        });
//    int nextCurrentIndex = notation.getForkedNotations().values().stream().mapToInt(NotationHistory::getCurrentIndex).max().orElse(0);
    int currentIndex = lastVariant.getNotationNumberInt() - notationHistory.getStartMovingFrom();
    int currentVariant = lastVariant.getIdInVariants();
    return notation
        .findNotationHistoryByLine(new NotationLine(0, 0))
        .map(nhNew -> {
          nhNew.getNotation()
              .removeIf(nd -> nd.getNotationNumberInt() >= lastVariant.getNotationNumberInt());
          NotationDrives newVariants = lastVariant.getVariants();
          newVariants.setIdInVariants(0);
          nhNew.getNotation().addAll(newVariants);
          notation.setNotationHistoryId(nhNew.getDomainId());
          save(notation, false);
          notationHistoryDao.save(nhNew);
          return nhNew;
        })
        .orElseThrow();
//    if (nhNew.isPresent()) {
//      nh = nhNew.get();
//      NotationDrive currentDrive = nh.getNotation().get(notationHistory.getCurrentIndex());
//      Optional<NotationDrive> curVariant = currentDrive.getVariants()
//          .stream()
//          .filter(NotationDrive::isCurrent)
//          .findFirst();
//      if (curVariant.isPresent()) {
//        NotationDrive currentVariant = curVariant.get();
//      }
//    }
//    throw new BoardServiceException(ErrorMessages.UNABLE_TO_REMOVE_VARIANT);
  }

  private void removeOneOfVariant(NotationHistory nh, NotationDrives variants) {
    variants.replaceAll(notationDrive -> {
      notationDrive.setPreviousWithVariant(false);
      notationDrive.setCurrentWithVariant(false);
      return notationDrive;
    });
    NotationDrive first = variants.getFirst();
    first.setCurrentWithVariant(true);
    int idInVariants = first.getIdInVariants();
    nh.setVariantIndex(idInVariants);
    nh.addAll(first.getVariants());
  }

  private void updateDraughtsDimension(int dimension, Map<String, Draught> whiteDraughts) {
    whiteDraughts.replaceAll((notation, draught) -> {
      draught.setDim(dimension);
      return draught;
    });
  }
}
