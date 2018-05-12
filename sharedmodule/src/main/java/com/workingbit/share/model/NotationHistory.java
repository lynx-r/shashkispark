package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.domain.DeepClone;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Created by Aleksey Popryaduhin on 10:12 04/10/2017.
 */
public class NotationHistory implements DeepClone {

  private NotationDrives history;
  private NotationDrives notation;
  /**
   * using for fork and switch
   */
  private NotationDrive currentNotationDrive;
  /**
   * using for fork and switch
   */
  private NotationDrive variantNotationDrive;

  public NotationHistory() {
    this(true);
  }

  private NotationHistory(boolean hasRoot) {
    if (hasRoot) {
      notation = NotationDrives.create();
      NotationDrive root = new NotationDrive(true);
      root.setSelected(true);
      notation.add(root);
      history = NotationDrives.create();
      history.add(root.deepClone());
    } else {
      notation = NotationDrives.create();
      history = NotationDrives.create();
    }
  }

  public NotationDrives getHistory() {
    return history;
  }

  public void setHistory(NotationDrives history) {
    this.history = history;
  }

  public NotationDrives getNotation() {
    return notation;
  }

  public void setNotation(NotationDrives notation) {
    this.notation = notation.deepClone();
  }

  public NotationDrive getCurrentNotationDrive() {
    return currentNotationDrive;
  }

  public void setCurrentNotationDrive(NotationDrive currentNotationDrive) {
    this.currentNotationDrive = currentNotationDrive;
  }

  public NotationDrive getVariantNotationDrive() {
    return variantNotationDrive;
  }

  public void setVariantNotationDrive(NotationDrive variantNotationDrive) {
    this.variantNotationDrive = variantNotationDrive;
  }

  public void addInHistoryAndNotation(NotationDrive element) {
    history.add(element);
    notation.add(element);
  }

  @DynamoDBIgnore
  @JsonIgnore
  private NotationDrive getLastSafeHistory() {
    boolean isRootDrive = history.getLast().isRoot();
    if (isRootDrive) {
      NotationDrive notationDrive = new NotationDrive();
      notationDrive.setNotationNumberInt(1);
      addInHistoryAndNotation(notationDrive);
      return notationDrive;
    } else {
      return history.getLast();
    }
  }

  @JsonIgnore
  @DynamoDBIgnore
  public NotationDrive getLast() {
    return notation.getLast();
  }

  public int size() {
    return notation.size();
  }

  private int indexOf(NotationDrive o) {
    return notation.indexOf(o);
  }

  private NotationDrives subListHistory(int fromIndex, int toIndex) {
    List<NotationDrive> subList = history.subList(fromIndex, toIndex);
    NotationDrives nd = new NotationDrives();
    nd.addAll(subList);
    return nd;
  }

  public boolean forkAt(NotationDrive forkFromNotationDrive) {
    if (history.size() == 1) {
      return false;
    }
    int indexFork = indexOf(forkFromNotationDrive);
    NotationDrives cutpoint = subListHistory(indexFork, size());
    NotationDrives cutNotationDrives = NotationDrives.Builder.getInstance()
        .addAll(cutpoint)
        .build();

    notation.removeAll(cutNotationDrives);
    resetNotationSelected(notation);
    notation.getLast().setSelected(true);

    history.removeAll(cutNotationDrives);

    // if not duplicated
    NotationDrive cutFirst = cutNotationDrives.getFirst();
    NotationDrive variant = cutFirst.deepClone();
    NotationDrive lastHist = history.getLast();
    Optional<NotationDrive> continueDrive = variantHasContinue(lastHist, variant);
    if (!continueDrive.isPresent()) {
      variant.setVariants(cutNotationDrives);
      resetCurrentAndSetPresious(lastHist);
      variant.setCurrent(true);
      lastHist.addVariant(variant);
    }

    if (!notation.isEmpty()) {
      notation.forEach(this::resetMovesCursor);
      NotationMoves moves = notation.getLast().getMoves();
      if (!moves.isEmpty()) {
        moves.getLastMove().setCursor(true);
      }
    }

    return true;
  }

  private void resetNotationSelected(NotationDrives notation) {
    notation.forEach(notationDrive -> notationDrive.setSelected(false));
  }

  private void resetCurrentAndSetPresious(NotationDrive lastHist) {
    lastHist.getVariants().forEach(v -> {
      setPreviousNotation(v);
      v.setCurrent(false);
    });
  }

  private void setPreviousNotation(NotationDrive v) {
    if (v.isCurrent()) {
      v.setPrevious(true);
    } else {
      v.setPrevious(false);
    }
  }

  public boolean switchTo(NotationDrive currentNotationDrive,
                          NotationDrive variantNotationDrive) {
    int indexFork = history.indexOf(currentNotationDrive);
    NotationDrive toSwitchDrive;
    if (currentNotationDrive == null) {
      toSwitchDrive = getLastSafeHistory();
      indexFork = history.indexOf(toSwitchDrive);
    } else {
      toSwitchDrive = history.get(indexFork);
    }

    NotationDrives switchVariants = toSwitchDrive.getVariants().deepClone();

    // push tail of notation to variants
    if (indexFork + 1 < size()) {
      List<NotationDrive> forked = subListHistory(indexFork + 1, size());
      NotationDrives forkedNotationDrives = NotationDrives.Builder.getInstance()
          .addAll(forked)
          .build();

      // remove tail
      notation.removeAll(forkedNotationDrives);
      history.removeAll(forkedNotationDrives);

      NotationDrive variant = forkedNotationDrives.getFirst().deepClone();
      Optional<NotationDrive> continueDrive = variantHasContinue(history.getLast(), variant);
      // if not duplicated
      if (!continueDrive.isPresent()) {
        variant.setVariants(forkedNotationDrives);
        history.getLast().addVariant(variant);
      }
      continueDrive.ifPresent(d -> d.setCurrent(true));
    }

    // find in selected notation drive to switch
    NotationDrive variantToSwitch = null;
    if (currentNotationDrive != null) {
      resetCurrentAndSetPresious(currentNotationDrive);
      variantToSwitch = variantNotationDrive;
    }

//    removeVariantsFromLastNotation();

    // push varint's to switch notation to main notation drives
    if (variantToSwitch != null) {
      resetCurrentAndSetPresious(toSwitchDrive);
      addAllVariantsInHistoryAndNotation(variantToSwitch);
    } else if (!switchVariants.isEmpty()) {
      // switch sequentially to drive with min variants
      Optional<NotationDrive> withMinVariants = switchVariants
          .stream()
          .filter(NotationDrive::isCurrent)
          .findFirst();
      resetCurrentAndSetPresious(toSwitchDrive);
      withMinVariants.ifPresent(this::addAllVariantsInHistoryAndNotation);
    } else {
      return false;
    }

    return true;
  }

  private Optional<NotationDrive> variantHasContinue(NotationDrive currentDrive, NotationDrive variant) {
    if (currentDrive.getVariants().isEmpty() && variant.getVariants().isEmpty()) {
      return isNotationDriveMovesEqual(currentDrive, variant) ? Optional.of(currentDrive) : Optional.empty();
    }
    for (NotationDrive current : currentDrive.getVariants()) {
      if (isNotationDriveMovesEqual(current, variant)) {
        NotationDrive firstVariant = current.getVariants().getFirst();
        if (variantHasContinuePair(firstVariant, variant)) {
          return Optional.of(current);
        }
      }
    }
    return Optional.empty();
  }

  private boolean variantHasContinuePair(NotationDrive current, NotationDrive variant) {
    NotationDrives currents = current.getVariants();
    NotationDrives variants = variant.getVariants();
    if (variants.isEmpty() && currents.isEmpty()) {
      return current.getMoves().equals(variant.getMoves());
    }
    Iterator<NotationDrive> iteratorCurrent = currents.iterator();
    Iterator<NotationDrive> iteratorVariant = variants.iterator();
    while ((iteratorCurrent.hasNext() && iteratorVariant.hasNext())) {
      if (variantHasContinuePair(iteratorCurrent.next(), iteratorVariant.next())) {
        return true;
      }
    }
    return false;
  }

  private boolean isNotationDriveMovesEqual(NotationDrive currentDrive, NotationDrive variant) {
    return currentDrive.getMoves().equals(variant.getMoves());
  }

  private void addAllVariantsInHistoryAndNotation(NotationDrive variant) {
    NotationDrive lastHist = history.getLast();
    setCurrentMarkerForNotationDrive(variant, lastHist);

    history.addAll(variant.getVariants());

    notation.addAll(variant.getVariants());
    resetNotationSelected(notation);
    notation.getLast().setSelected(true);

    notation.forEach(this::resetMovesCursor);
    history.forEach(this::resetMovesCursor);

    NotationDrive lastNot = notation.getLast();
    if (!lastNot.getMoves().isEmpty()) {
      lastNot.getMoves().getLastMove().setCursor(true);
    }
  }

  private void resetMovesCursor(NotationDrive drive) {
    drive.getMoves().forEach(m -> m.getMove().forEach(move -> move.setCursor(false)));
  }

  private void setCurrentMarkerForNotationDrive(NotationDrive variant, NotationDrive lastHist) {
    lastHist.getVariants()
        .stream()
        .filter(h -> variantHasContinuePair(h, variant))
        .findFirst()
        .ifPresent(v -> v.setCurrent(true));
  }

  @JsonIgnore
  @DynamoDBIgnore
  public boolean isEmpty() {
    return notation.isEmpty();
  }

  @JsonIgnore
  @DynamoDBIgnore
  public NotationDrive get(int index) {
    return notation.get(index);
  }

  public static NotationHistory create() {
    return new NotationHistory(false);
  }

  public static NotationHistory createWithRoot() {
    return new NotationHistory(true);
  }

  public String variantsToPdn() {
    return notation.toPdn();
  }

  public void printPdn() {
    System.out.println(variantsToPdn());
  }

  @JsonIgnore
  @DynamoDBIgnore
  public Optional<DomainId> getLastNotationBoardId() {
    try {
      NotationDrive notationLast = history.getLast();
      if (notationLast.isRoot()) {
        NotationMoves moves = notationLast.getVariants()
            .getLast()
            .getMoves();
        if (!moves.isEmpty()) {
          return Optional.of(moves.getFirst()
              .getMove().getFirst()
              .getBoardId());
        }
        return Optional.empty();
      }
      NotationMoves moves = notationLast.getMoves();
      return moves.getLast().getLastMoveBoardId();
    } catch (Exception e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  @JsonIgnore
  @DynamoDBIgnore
  public Optional<NotationMove> getLastMove() {
    NotationDrive last = notation.getLast();
    if (!last.getMoves().isEmpty()) {
      return Optional.of(last.getMoves().getLast());
    }
    return Optional.empty();
  }

  public String pdnString() {
    return "\n" +
        "HISTORY: " +
        history.toPdn() +
        "\n" +
        "NOTATION: " +
        notation.toPdn();
  }

  public void syncLastDrive() {
    NotationDrive lastNotation = notation.getLast();
    lastNotation.setSelected(true);
    NotationDrive lastHistory = history.getLast();
    NotationDrive.copyOf(lastNotation, lastHistory);
  }
}
