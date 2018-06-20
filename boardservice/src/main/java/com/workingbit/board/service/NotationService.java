package com.workingbit.board.service;

import com.workingbit.share.domain.impl.*;
import com.workingbit.share.exception.DaoException;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.*;
import com.workingbit.share.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    notationStoreService.removeNotation(notation);
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

  NotationLine removeVariant(Notation notation) {
    NotationLine notationLine = notation.getNotationHistory().getNotationLine();
    return notation.findNotationHistoryByLine(notationLine)
        .map(notationHistory -> {
          notationHistory.getCurrentNotationDrive()
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
          notationHistory.removeByCurrentIndex();
          notationHistoryDao.delete(notationHistory.getDomainId());
          notation.removeForkedNotations(notationHistory);
          return notationHistory.getCurrentNotationDrive()
              .map(current -> {
                NotationHistory nh = notationHistory;
                NotationDrives variants = current.getVariants();
                if (variants.size() > 1) {
                  variants.replaceAll(notationDrive -> {
                    notationDrive.setPrevious(false);
                    notationDrive.setCurrent(false);
                    return notationDrive;
                  });
                  NotationDrive first = variants.getFirst();
                  first.setCurrent(true);
                  int idInVariants = first.getIdInVariants();
                  nh.setVariantNotationDrive(idInVariants);
                } else {
                  current.getVariants().clear();
                  Collection<NotationHistory> values = new ArrayList<>(notation.getForkedNotations().values());
                  values.stream()
                      .filter(toDelete -> toDelete.getCurrentIndex().equals(notationHistory.getCurrentIndex()))
                      .forEach(toDelete -> {
                        notationHistoryDao.delete(toDelete.getDomainId());
                        notation.removeForkedNotations(toDelete);
                      });
                  int nextCurrentIndex = notation.getForkedNotations().values().stream().mapToInt(NotationHistory::getCurrentIndex).max().orElse(0);
                  var nhNew = notation
                      .findNotationHistoryByLine(new NotationLine(nextCurrentIndex, 0));
                  if (nhNew.isPresent()) {
                    nh = nhNew.get();
                  }
                  NotationDrive currentDrive = nh.getNotation().get(notationHistory.getCurrentIndex());
                  Optional<NotationDrive> curVariant = currentDrive.getVariants()
                      .stream()
                      .filter(NotationDrive::isCurrent)
                      .findFirst();
                  if (curVariant.isPresent()) {
                    NotationDrive currentVariant = curVariant.get();
                    nh.getNotation()
                        .removeIf(nd -> nd.getNotationNumberInt() >= currentDrive.getNotationNumberInt());
                    NotationDrives newVariants = currentVariant.getVariants();
                    newVariants.setIdInVariants(0);
                    nh.getNotation().addAll(newVariants);
                  }
                  notationHistoryDao.save(nh);
                }
                syncVariants(nh, notation);
                return nh.getNotationLine();
              })
              .orElseThrow();
        })
        .orElseThrow();
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

  private void updateDraughtsDimension(int dimension, Map<String, Draught> whiteDraughts) {
    whiteDraughts.replaceAll((notation, draught) -> {
      draught.setDim(dimension);
      return draught;
    });
  }

  Notation findById(@NotNull DomainId notationId, @Nullable AuthUser authUser) {
    if (authUser == null) {
      throw RequestException.notFound404();
    }
    return notationStoreService
        .getNotation(authUser.getUserSession(), notationId)
        .orElseGet(() -> {
          Notation byId = notationDao.findById(notationId);
          fillNotation(byId);
          notationStoreService.putNotation(authUser.getUserSession(), byId);
          return byId;
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
                current.getMoves().replaceAll(notationMove -> {
                  notationMove.setCursor(false);
                  return notationMove;
                });
                Integer prevVariantId = notation.getPrevVariantId() == null ? -1 : notation.getPrevVariantId();
                setVariantDriveMarkers(notationLine, notation, current, prevVariantId);
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

  private void setVariantDriveMarkers(NotationLine notationLine, Notation notation, NotationDrive current, Integer prevVariantId) {
    AtomicBoolean isPrevious = new AtomicBoolean();
    current.getVariants()
        .replaceAll(notationDrive -> {
          if (!isPrevious.get()) {
            isPrevious.set(notationDrive.getIdInVariants() == prevVariantId);
          }
          notationDrive.setPrevious(notationDrive.isCurrent());
          boolean isCurrent = notationDrive.getIdInVariants() == notationLine.getVariantIndex();
          notationDrive.setCurrent(isCurrent);
          if (isCurrent) {
            notation.setPrevVariantId(notationDrive.getIdInVariants());
          }
          return notationDrive;
        });
    if (isPrevious.get()) {
      current.getVariants()
          .replaceAll(notationDrive -> {
            boolean prev = notationDrive.getIdInVariants() == prevVariantId;
            notationDrive.setPrevious(prev);
            return notationDrive;
          });
    }
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

  void syncVariants(NotationHistory toSyncNotationHist, Notation notation) {
    toSyncNotationHist.getCurrentNotationDrive()
        .ifPresent(currentNotationDrive -> {
          AtomicBoolean save = new AtomicBoolean(false);
          notation.getForkedNotations().replaceAll((s, notationHistory) -> {
            if (isCorrespondedNotation(toSyncNotationHist, notationHistory)) {
              NotationDrive cur = notationHistory.get(toSyncNotationHist.getCurrentIndex());
              cur.setCurrent(currentNotationDrive.isCurrent());
              cur.setPrevious(currentNotationDrive.isPrevious());
              cur.setVariants(currentNotationDrive.getVariants());
              save.set(true);
            }
            return notationHistory;
          });
          if (save.get()) {
            notationHistoryDao.batchSave(notation.getForkedNotations().values());
          }
        });
  }

  void deleteById(DomainId notationId) {
    notationDao.delete(notationId.getDomainId());
    notationStoreService.removeNotationById(notationId);
  }
}
