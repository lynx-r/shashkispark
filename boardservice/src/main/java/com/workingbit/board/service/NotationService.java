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

import static com.workingbit.board.BoardEmbedded.*;
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

    notationHistoryService.createForNotation(notation);

    notation.setBoardBoxId(boardBox.getDomainId());
    notation.setRules(boardBox.getBoard().getRules());
    boardBox.setNotationId(notation.getDomainId());
    boardBox.setNotation(notation);
  }

  void clearNotationInBoardBox(@NotNull BoardBox bb) {
    var notation = notationDao.findById(bb.getNotationId());
    notationHistoryService.deleteByNotationId(bb.getNotationId());
    notationHistoryService.createForNotation(notation);
    notation.setNotationFen(new NotationFen());
    save(notation, false);
    bb.setNotation(notation);
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
                    .stream()
                    .map(NotationDrive::getMoves)
                    .flatMap(Collection::stream)
                    .map(NotationMove::getMove)
                    .flatMap(Collection::stream)
                    .map(NotationSimpleMove::getBoardId)
                    .distinct()
                    .collect(collectingAndThen(toCollection(LinkedList::new), DomainIds::new));
                boardDao.batchDelete(boardIdsToRemove);
              }));
          notationHistory.removeByCurrentIndex();
          notation.getForkedNotations().remove(notationHistory.getId());
          return notationHistory.getCurrentNotationDrive()
              .map(current -> {
                NotationDrives variants = current.getVariants();
                if (variants.size() != 0) {
                  variants.replaceAll(notationDrive -> {
                    notationDrive.setPrevious(false);
                    notationDrive.setCurrent(false);
                    return notationDrive;
                  });
                  NotationDrive first = variants.getFirst();
                  first.setCurrent(true);
                  int idInVariants = first.getIdInVariants();
                  notationHistory.setVariantNotationDrive(idInVariants);
                } else {
                  notationHistory.setVariantNotationDrive(0);
                }
                syncVariants(notationHistory, notation);
                return notationHistory.getNotationLine();
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
      notation.addNotationHistoryAll(byNotationId);
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
    byIds.forEach(this::fillNotation);
    return byIds;
  }

  void syncSubVariants(NotationHistory toSyncNotationHist, Notation notation) {
    toSyncNotationHist.getCurrentVariant()
        .ifPresent(currentSyncVariant -> {
          List<Board> toSave = new ArrayList<>();
          notation.getForkedNotations().replaceAll((s, notationHistory) -> {
            if (!s.equals(toSyncNotationHist.getId())) {
              NotationDrive cur = notationHistory.get(toSyncNotationHist.getCurrentIndex());
              cur.getVariantById(toSyncNotationHist.getVariantIndex())
                  .ifPresent(curVariant -> {
                    complementMoves(curVariant, currentSyncVariant.getMoves());
                    curVariant.setVariants(currentSyncVariant.getVariants());
                  });
            }
            return notationHistory;
          });
          boardDao.batchSave(toSave);
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

  private void syncVariants(NotationHistory toSyncNotationHist, Notation notation) {
    toSyncNotationHist.getCurrentNotationDrive()
        .ifPresent(currentNotationDrive -> {
          notation.getForkedNotations().replaceAll((s, notationHistory) -> {
            if (!s.equals(toSyncNotationHist.getId())) {
              NotationDrive cur = notationHistory.get(toSyncNotationHist.getCurrentIndex());
              cur.setVariants(currentNotationDrive.getVariants());
            }
            return notationHistory;
          });
          notationHistoryDao.batchSave(notation.getForkedNotations().values());
        });
  }
}
