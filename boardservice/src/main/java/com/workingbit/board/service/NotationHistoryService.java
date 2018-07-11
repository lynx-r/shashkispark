package com.workingbit.board.service;

import com.workingbit.board.repo.ReactiveNotationHistoryRepository;
import com.workingbit.share.domain.impl.Notation;
import com.workingbit.share.domain.impl.NotationHistory;
import com.workingbit.share.model.DomainId;
import com.workingbit.share.model.NotationDrive;
import com.workingbit.share.model.NotationDrives;
import com.workingbit.share.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.workingbit.share.util.Utils.isCorrespondedNotation;

/**
 * Created by Aleksey Popryadukhin on 14/04/2018.
 */
@Service
public class NotationHistoryService {

  private ReactiveNotationHistoryRepository notationHistoryRepository;

  public NotationHistoryService(ReactiveNotationHistoryRepository notationHistoryRepository) {
    this.notationHistoryRepository = notationHistoryRepository;
  }

  void save(@NotNull NotationHistory notationHistory) {
    notationHistoryRepository.save(notationHistory);
  }

  NotationHistory forkAt(int forkFromNotationDrive, Notation notation) {
    // forked variant
    NotationHistory notationHistory = notation.getNotationHistory().deepClone();
    notationHistory.getCurrentVariant()
        .ifPresent(notationDrive -> {
          notationDrive.setPreviousWithVariant(true);
          notation.setPrevVariantId(null);
        });
    NotationDrive forkNotationDrive = notationHistory.get(forkFromNotationDrive);
    int variantId = forkNotationDrive.getVariantId();
    boolean root = variantId == 0;
    DomainId rootHistoryId = null;
    if (root) {
      // root variant
      NotationHistory rootNotationHistory = notation.getNotationHistory().deepClone();
      Utils.setRandomIdAndCreatedAt(rootNotationHistory);
      rootNotationHistory.setCurrentIndex(forkFromNotationDrive);
      rootNotationHistory.setVariantIndex(0);
      rootHistoryId = rootNotationHistory.getDomainId();
      notation.addForkedNotationHistory(rootNotationHistory);
      // inc variant id
      variantId++;
    }
    Utils.setRandomIdAndCreatedAt(notationHistory);
    notationHistory.setCurrentIndex(forkFromNotationDrive);
    notationHistory.setVariantIndex(variantId);
    notation.addForkedNotationHistory(notationHistory);

    // new fork
    NotationDrives notationDrives = notationHistory.getNotation();
    NotationDrives cutPoint = subListNotation(notationDrives, forkFromNotationDrive, notationDrives.size());
    NotationDrives cutNotationDrives = cutPoint.deepClone();

    notationDrives.removeAll(cutPoint);
    resetNotationMarkers(notationDrives);

    NotationDrive forkedVariant = forkNotationDrive.deepClone();

    // if first variant
    NotationDrive rootV = null;
    if (rootHistoryId != null) {
      rootV = createRootDrive(forkNotationDrive, cutNotationDrives, rootHistoryId);
      forkedVariant.setDriveColor(Utils.getRandomColor());
      forkedVariant.addVariant(rootV);
      forkedVariant.setParentColor(rootV.getParentColor());
    }
    cutNotationDrives.setIdInVariants(variantId);
    NotationDrive cutNotationDrivesFirst = cutNotationDrives.getFirst();
    NotationDrive firstNew = cutNotationDrivesFirst.deepClone();
    firstNew.setIdInVariants(variantId);
    NotationDrives switchVariants = forkedVariant.getVariants();
    if (rootV == null) {
      // to not touch rootV reset previous in variants to switch
      resetPreviousInSwitchVariants(switchVariants);
    } else {
      fillFirstVariant(rootV, firstNew);
    }
    cutNotationDrivesFirst.setVariants(NotationDrives.create());
    firstNew.setVariants(new NotationDrives(List.of(cutNotationDrivesFirst)));
    setMarkersAndColorsForForkedVariant(firstNew, switchVariants);
    fillForkedVariant(forkedVariant, firstNew, notationHistory.getDomainId());

    notationDrives.add(forkedVariant);
    notationDrives.setLastMoveCursor();

    Collection<NotationHistory> notationHistories = notation.getForkedNotations().values();
    syncVariantsInForkedNotations(forkFromNotationDrive, notationHistory, forkedVariant, notationHistories);
    notationHistoryRepository.deleteAll(notationHistories);
    return notationHistory;
  }

  private void fillForkedVariant(NotationDrive forkedVariant, NotationDrive firstNew, DomainId domainId) {
    firstNew.setNotationHistoryId(domainId);
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
          notationDrive.setCurrentWithVariant(false);
        });
  }

  private void resetPreviousInSwitchVariants(NotationDrives switchVariants) {
    switchVariants
        .stream()
        .filter(NotationDrive::isPrevious)
        .findFirst()
        .ifPresent(notationDrive -> notationDrive.setPreviousWithVariant(false));
  }

  private void fillFirstVariant(NotationDrive rootV, NotationDrive firstNew) {
    firstNew.setParentId(0);
    rootV.setAncestors(rootV.getAncestors() + 1);
    firstNew.setParentColor(rootV.getDriveColor());
    firstNew.setDriveColor(Utils.getRandomColor());
    firstNew.resetCursor();
  }

  private void setMarkersAndColorsForForkedVariant(NotationDrive firstNew, NotationDrives switchVariants) {
    switchVariants
        .stream()
        .filter(NotationDrive::isCurrent)
        .findFirst()
        .ifPresent(cur -> {
          cur.setAncestors(cur.getAncestors() + 1);
          firstNew.setParentId(cur.getIdInVariants());
          firstNew.setParentColor(cur.getDriveColor());
          firstNew.setDriveColor(Utils.getRandomColor());
          firstNew.resetCursor();
          firstNew.setCurrentWithVariant(true);
          cur.setCurrentWithVariant(false);
          cur.setPreviousWithVariant(true);
        });
  }

  private NotationDrive createRootDrive(NotationDrive forkNotationDrive, NotationDrives cutNotationDrives, DomainId rootHistoryId) {
    NotationDrive rootV = null;
    if (forkNotationDrive.getVariantsSize() == 0) {
      rootV = forkNotationDrive.deepClone();
      NotationDrives rootCut = cutNotationDrives.deepClone();
      rootCut.setIdInVariants(0);
      rootV.setNotationHistoryId(rootHistoryId);
      rootV.setVariants(rootCut);
      rootV.setSelected(false);
      rootV.setPreviousWithVariant(true);
      // temporary current later it changes to false
      rootV.setCurrentWithVariant(true);
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
      if (isCorrespondedNotation(notationHistory, history)) {
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

  void createNotationHistoryForNotation(Notation notation) {
    NotationHistory notationHistory = NotationHistory.createWithRoot();
    Utils.setRandomIdAndCreatedAt(notationHistory);
    notationHistory.setCurrentIndex(0);
    notationHistory.setVariantIndex(0);
    notationHistory.setNotationId(notation.getDomainId());
    notation.addForkedNotationHistory(notationHistory);
    notation.setNotationHistory(notationHistory);
    notation.setNotationHistoryId(notationHistory.getDomainId());
  }

  void deleteByNotationId(DomainId notationId) {
    var notationHistories = notationHistoryRepository.findByNotationId(notationId);
    notationHistoryRepository.deleteAll(notationHistories);
//      notationHistories.forEach(notationStoreService::removeNotationHistory);
  }
}
