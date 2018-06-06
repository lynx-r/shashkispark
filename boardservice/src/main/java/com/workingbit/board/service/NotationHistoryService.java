package com.workingbit.board.service;

import com.workingbit.share.domain.impl.Notation;
import com.workingbit.share.domain.impl.NotationHistory;
import com.workingbit.share.exception.DaoException;
import com.workingbit.share.model.DomainId;
import com.workingbit.share.model.NotationDrive;
import com.workingbit.share.model.NotationDrives;
import com.workingbit.share.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import static com.workingbit.board.BoardEmbedded.notationHistoryDao;
import static com.workingbit.board.BoardEmbedded.notationStoreService;

/**
 * Created by Aleksey Popryadukhin on 14/04/2018.
 */
public class NotationHistoryService {

  void save(@NotNull NotationHistory notationHistory) {
    notationHistoryDao.save(notationHistory);
    notationStoreService.removeNotationHistory(notationHistory);
  }

  NotationHistory forkAt(int forkFromNotationDrive, Notation notation) {
    // forked variant
    NotationHistory notationHistory = notation.getNotationHistory().deepClone();
    notationHistory.getCurrentVariant()
        .ifPresent(notationDrive -> {
          notationDrive.setPrevious(true);
          notation.setPrevVariantId(null);
        });
    NotationDrive forkNotationDrive = notationHistory.get(forkFromNotationDrive);
    int variantId = forkNotationDrive.getVariantId();
    boolean root = variantId == 0;
    if (root) {
      // root variant
      NotationHistory rootNotationHistory = notation.getNotationHistory().deepClone();
      Utils.setRandomIdAndCreatedAt(rootNotationHistory);
      rootNotationHistory.setCurrentNotationDrive(forkFromNotationDrive);
      rootNotationHistory.setVariantNotationDrive(0);
      rootNotationHistory.getLast().setCurrent(true);
      notation.addNotationHistory(rootNotationHistory);
      // inc variant id
      variantId++;
    }
    Utils.setRandomIdAndCreatedAt(notationHistory);
    notationHistory.setCurrentNotationDrive(forkFromNotationDrive);
    notationHistory.setVariantNotationDrive(variantId);
    notation.addNotationHistory(notationHistory);

    // new fork
    NotationDrives notationDrives = notationHistory.getNotation();
    NotationDrives cutPoint = subListNotation(notationDrives, forkFromNotationDrive, notationDrives.size());
    NotationDrives cutNotationDrives = cutPoint.deepClone();

    notationDrives.removeAll(cutPoint);
    resetNotationMarkers(notationDrives);

    // if first variant
    NotationDrive rootV = createRootDrive(forkNotationDrive, cutNotationDrives);
    NotationDrive forkedVariant = forkNotationDrive.deepClone();
    forkedVariant.setDriveColor(Utils.getRandomColor());

    if (rootV != null) {
      forkedVariant.addVariant(rootV);
      forkedVariant.setParentColor(rootV.getParentColor());
    }
    cutNotationDrives.setIdInVariants(variantId);
    NotationDrive firstNew = cutNotationDrives.getFirst().deepClone();
    firstNew.setIdInVariants(variantId);
    firstNew.setCurrent(true);
    NotationDrives switchVariants = forkedVariant.getVariants();
    if (rootV == null) {
      // to not touch rootV reset previous in variants to switch
      resetPreviousInSwitchVariants(switchVariants);
    } else {
      fillFirstVariant(rootV, firstNew);
    }
    cutNotationDrives.getFirst().setVariants(NotationDrives.create());
    firstNew.setVariants(new NotationDrives(List.of(cutNotationDrives.getFirst())));
    setMarkersAndColorsForSwitchVariants(firstNew, switchVariants);
    fillForkedVariant(forkedVariant, firstNew);

    notationDrives.add(forkedVariant);
    notationDrives.setLastMoveCursor();

    Collection<NotationHistory> notationHistories = notation.getForkedNotations().values();
    syncVariantsInForkedNotations(forkFromNotationDrive, notationHistory, forkedVariant, notationHistories);
    notationHistoryDao.batchSave(notationHistories);
    return notationHistory;
  }

  private void fillForkedVariant(NotationDrive forkedVariant, NotationDrive firstNew) {
    forkedVariant.addVariant(firstNew);
    forkedVariant.setCurrent(true);
    forkedVariant.setSelected(true);
  }

  private void resetNotationMarkers(NotationDrives notationDrives) {
    notationDrives
        .stream()
        .filter(NotationDrive::isCurrent)
        .findFirst()
        .ifPresent(notationDrive -> {
          notationDrive.setSelected(false);
          notationDrive.setCurrent(false);
        });
  }

  private void resetPreviousInSwitchVariants(NotationDrives switchVariants) {
    switchVariants
        .stream()
        .filter(NotationDrive::isPrevious)
        .findFirst()
        .ifPresent(notationDrive -> notationDrive.setPrevious(false));
  }

  private void fillFirstVariant(NotationDrive rootV, NotationDrive firstNew) {
    firstNew.setParentId(0);
    firstNew.setParentColor(rootV.getDriveColor());
    firstNew.setDriveColor(Utils.getRandomColor());
  }

  private void setMarkersAndColorsForSwitchVariants(NotationDrive firstNew, NotationDrives switchVariants) {
    switchVariants
        .stream()
        .filter(NotationDrive::isCurrent)
        .findFirst()
        .ifPresent(cur -> {
          cur.setAncestors(cur.getAncestors() + 1);
          firstNew.setParentId(cur.getIdInVariants());
          firstNew.setParentColor(cur.getDriveColor());
          cur.setCurrent(false);
          cur.setPrevious(true);
        });
  }

  private NotationDrive createRootDrive(NotationDrive forkNotationDrive, NotationDrives cutNotationDrives) {
    NotationDrive rootV = null;
    if (forkNotationDrive.getVariantsSize() == 0) {
      rootV = forkNotationDrive.deepClone();
      NotationDrives rootCut = cutNotationDrives.deepClone();
      rootCut.setIdInVariants(0);
      rootV.setVariants(rootCut);
      rootV.setSelected(false);
      rootV.setPrevious(true);
      rootV.setCurrent(false);
      rootV.setParentColor("purple");
      rootV.setDriveColor(Utils.getRandomColor());
    }
    return rootV;
  }

  private void syncVariantsInForkedNotations(int forkFromNotationDrive,
                                             NotationHistory notationHistory,
                                             NotationDrive forkedVariant,
                                             Collection<NotationHistory> notationHistories) {
    for (NotationHistory history : notationHistories) {
      if (!history.getId().equals(notationHistory.getId())) {
        NotationDrive forkedCur = history.get(forkFromNotationDrive);
        forkedCur.setVariants(forkedVariant.getVariants());
      }
    }
  }

  @NotNull
  private NotationDrives subListNotation(NotationDrives notationDrives, int fromIndex, int toIndex) {
    List<NotationDrive> subList = notationDrives.subList(fromIndex, toIndex);
    NotationDrives nd = new NotationDrives();
    nd.addAll(subList);
    return nd;
  }

  void createForNotation(Notation notation) {
    NotationHistory notationHistory = NotationHistory.createWithRoot();
    Utils.setRandomIdAndCreatedAt(notationHistory);
    notationHistory.setCurrentNotationDrive(0);
    notationHistory.setVariantNotationDrive(0);
    notationHistory.setNotationId(notation.getDomainId());
    notation.addNotationHistory(notationHistory);
    notation.setNotationHistory(notationHistory);
    notation.setNotationHistoryId(notationHistory.getDomainId());
  }

  void deleteByNotationId(DomainId notationId) {
    try {
      var notationHistories = notationHistoryDao.findByNotationId(notationId);
      notationHistoryDao.batchDelete(notationHistories);
    } catch (DaoException ignore) {
    }
  }
}
