package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.domain.DeepClone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Aleksey Popryaduhin on 10:12 04/10/2017.
 */
public class NotationHistory implements DeepClone {

  private final Logger logger = LoggerFactory.getLogger(NotationHistory.class);

  private NotationDrives history;
  private NotationDrives notation;
  private NotationDrive currentNotationDrive;
  private NotationDrive variantNotationDrive;

  public NotationHistory() {
    this(true);
  }

  private NotationHistory(boolean hasRoot) {
    if (hasRoot) {
      notation = NotationDrives.create();
      NotationDrive root = new NotationDrive(true);
      notation.add(root);
      history = NotationDrives.create();
      history.add(root.deepClone());
    } else {
      notation = NotationDrives.create();
      history = NotationDrives.create();
    }
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

  public NotationDrives getHistory() {
    return history;
  }

  public void setHistory(NotationDrives history) {
    this.history = history;
  }

  public void add(NotationDrive notationDrive) {
    add(size(), notationDrive);
  }

  public void addAllEverywhere(Collection<? extends NotationDrive> c) {
    c.forEach(this::add);
  }

  public void add(int index, NotationDrive element) {
    history.add(index, element);
    notation.add(index, element);
    syncAdd(index);
  }

  public void addFirst(NotationDrive notationDrive) {
    add(1, notationDrive);
  }

  public void addLast(NotationDrive notationDrive) {
    add(notationDrive);
  }

  @DynamoDBIgnore
  @JsonIgnore
  public NotationDrive getFirst() {
    return notation.getFirst();
  }

  @DynamoDBIgnore
  @JsonIgnore
  public NotationDrive getLastSafeHistory() {
    boolean isRootDrive = history.getLast().isRoot();
    if (isRootDrive) {
      NotationDrive notationDrive = new NotationDrive();
      notationDrive.setNotationNumberInt(1);
      add(notationDrive);
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

  public boolean remove(NotationDrive o) {
//    history.remove(o);
    return notation.remove(o);
  }

  public void removeAll(NotationDrives c) {
    c.forEach(this::remove);
  }

  public NotationDrive removeFirst() {
    history.removeFirst();
    return notation.removeFirst();
  }

  public NotationDrive removeLast() {
    history.removeLast();
    return notation.removeLast();
  }

  public int size() {
    return notation.size();
  }

  public int indexOf(NotationDrive o) {
    return notation.indexOf(o);
  }

  public NotationDrives subListHistory(int fromIndex, int toIndex) {
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
    removeVariantsFromLastNotation();

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
        moves.getLast().setCursor(true);
      }
    }

    logger.info("Notation after fork: " + pdnString());
    return true;
  }

  private void resetNotationSelected(NotationDrives notation) {
    notation.forEach(notationDrive -> notationDrive.setSelected(false));
  }

  public void resetCurrentAndSetPresious(NotationDrive lastHist) {
    lastHist.getVariants().forEach(v -> {
      setPreviousNotation(v);
      v.setCurrent(false);
    });
  }

  public void setPreviousNotation(NotationDrive v) {
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

    // add tail of notation to variants
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

    removeVariantsFromLastNotation();

    // add varint's to switch notation to main notation drives
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

    logger.info("Notation after switch: " + pdnString());
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

  private void removeVariantsFromLastNotation() {
    if (!notation.isEmpty()) {
      NotationDrive last = notation.getLast();
      NotationDrives variants = last.getVariants();
      if (!variants.isEmpty()) {
        variants.removeLast();
      }
    }
  }

  private void addAllVariantsInHistoryAndNotation(NotationDrive variant) {
    NotationDrive lastHist = history.getLast();
    setCurrentMarkerForNotationDrive(variant, lastHist);

    history.addAll(variant.getVariants());

    resetNotationSelected(notation);
    notation.addAll(variant.getVariants());
    notation.getLast().setSelected(true);

    notation.forEach(this::resetMovesCursor);
    history.forEach(this::resetMovesCursor);

    NotationDrive lastNot = notation.getLast();
    if (!lastNot.getMoves().isEmpty()) {
      lastNot.getMoves().getLast().setCursor(true);
    }
  }

  private void resetMovesCursor(NotationDrive drive) {
    drive.getMoves().forEach(m -> m.setCursor(false));
  }

  private void setCurrentMarkerForNotationDrive(NotationDrive variant, NotationDrive lastHist) {
    lastHist.getVariants()
        .stream()
        .filter(h -> variantHasContinuePair(h, variant))
        .findFirst()
        .ifPresent(v -> v.setCurrent(true));
  }

  private NotationMoves switchToMoves(NotationDrive currentNotationDrive, NotationDrive variantNotationDrive) {
    return variantNotationDrive != null
        ? variantNotationDrive.getVariants().get(0).getMoves()
        : (currentNotationDrive.getVariants().isEmpty() ? null
        : currentNotationDrive.getVariants().get(0).getMoves());
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
    System.out.println("HISTORY: " + history.toPdn());
    System.out.print("VARIANTS: ");
    return notation.toPdn();
  }

  private void syncAdd(int index) {
    NotationDrive historyDirve = history.get(index);
    NotationDrive variantsDrive = notation.get(index);
    if (historyDirve.getMoves().size() != variantsDrive.getMoves().size()) {
      historyDirve.setMoves(variantsDrive.getMoves());
    }
  }

  public void printPdn() {
    System.out.println(variantsToPdn());
  }

  @JsonIgnore
  @DynamoDBIgnore
  public Optional<String> getLastNotationBoardId() {
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
    NotationDrive lastHistory = history.getLast();
    NotationDrive.copyOf(lastNotation, lastHistory);
  }
}
