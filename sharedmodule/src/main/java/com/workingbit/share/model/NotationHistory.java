package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.domain.DeepClone;
import com.workingbit.share.model.enumarable.EnumNotationFormat;
import com.workingbit.share.model.enumarable.EnumRules;

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
  public NotationDrive getFirst() {
    return notation.getFirst();
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
    NotationDrives cutNotationDrives = cutpoint.deepClone();

    notation.removeAll(cutNotationDrives);
    resetNotationSelected(notation);
    notation.getLast().setSelected(true);

    history.removeAll(cutNotationDrives);

    // if not duplicated
    NotationDrive cutFirst = cutNotationDrives.getFirst();
    NotationDrive variant = cutFirst.deepClone();
    NotationDrive lastHist = history.getLast();
    Optional<NotationDrive> continueDrive = variantHasContinue(lastHist, variant, cutNotationDrives);
    if (!continueDrive.isPresent()) {
      variant.setIdInVariants(lastHist.getVariantsSize() + 1);
      cutNotationDrives.setIdInVariants(variant.getIdInVariants());
      variant.setVariants(cutNotationDrives);
      resetCurrentAndSetPresious(lastHist);
      variant.setCurrent(true);
      lastHist.addVariant(variant);
    }

    if (!notation.isEmpty()) {
      notation.forEach(this::resetMovesCursor);
      NotationMoves moves = notation.getLast().getMoves();
      if (!moves.isEmpty()) {
        moves.setCursor(true);
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
      Optional<NotationDrive> continueDrive = variantHasContinue(history.getLast(), variant, forkedNotationDrives);
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

  private Optional<NotationDrive> variantHasContinue(NotationDrive currentDrive, NotationDrive variant, NotationDrives cutNotationDrives) {
    if (currentDrive.getVariants().isEmpty() && variant.getVariants().isEmpty()) {
      return isNotationDriveMovesEqual(currentDrive, variant) ? Optional.of(currentDrive) : Optional.empty();
    }
    for (NotationDrive current : currentDrive.getVariants()) {
      if (isNotationDriveMovesEqual(current, variant)) {
        NotationDrives currentVariants = current.getVariants();
        if (variantHasContinuePair(currentVariants, cutNotationDrives)) {
          return Optional.of(current);
        }
      }
    }
    return Optional.empty();
  }

  private boolean variantHasContinuePair(NotationDrives currents, NotationDrives variants) {
    Iterator<NotationDrive> iteratorCurrent = currents.iterator();
    Iterator<NotationDrive> iteratorVariant = variants.iterator();
    while ((iteratorCurrent.hasNext() && iteratorVariant.hasNext())) {
      NotationDrive current = iteratorCurrent.next();
      NotationDrive variant = iteratorVariant.next();
      if (current.getVariants().isEmpty() && variant.getVariants().isEmpty()) {
        return currents.size() == variants.size() && current.getMoves().equals(variant.getMoves());
      }
      if (variantHasContinuePair(current.getVariants(), variant.getVariants())) {
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
      lastNot.getMoves().setCursor(true);
    }
  }

  private void resetMovesCursor(NotationDrive drive) {
    drive.getMoves().resetCursors();
  }

  private void setCurrentMarkerForNotationDrive(NotationDrive variant, NotationDrive lastHist) {
    lastHist.getVariants()
        .stream()
        .filter(h -> variantHasContinuePair(h.getVariants(), variant.getVariants()))
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

  public String notationToPdn() {
    return history.asString();
  }

  public String notationToTreePdn(String indent, String tabulation) {
    return history.asTree(indent, tabulation);
  }

  public void printPdn() {
    System.out.println(notationToPdn());
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

  public String debugPdnString() {
    return "\n" +
        "HISTORY: " +
        history.asString() +
        "\n" +
        "NOTATION: " +
        notation.asString();
  }

  public void syncLastDrive() {
    NotationDrive lastNotation = notation.getLast();
    lastNotation.setSelected(true);
    NotationDrive lastHistory = history.getLast();
    NotationDrive.copyOf(lastNotation, lastHistory);
  }

  @JsonIgnore
  @DynamoDBIgnore
  public Optional<DomainId> getFirstNotationBoardId() {
    if (size() > 1 && !get(1).getMoves().isEmpty()) {
      return Optional.of(get(1).getMoves().getFirst().getMove().getFirst().getBoardId());
    }
    return Optional.empty();
  }

  public void setRules(EnumRules rules) {
    notation.setDimension(rules.getDimension());
    history.setDimension(rules.getDimension());
  }

  public void setFormat(EnumNotationFormat format) {
    notation.setNotationFormat(format);
    history.setNotationFormat(format);
  }


  public boolean isEqual(NotationHistory that) {
    EnumNotationFormat formatThat = that.getNotation().getFirst().getNotationFormat();
    EnumNotationFormat formatThis = this.getNotation().getFirst().getNotationFormat();
    that.setFormat(EnumNotationFormat.ЧИСЛОВОЙ);
    this.setFormat(EnumNotationFormat.ЧИСЛОВОЙ);
    boolean equals = that.notationToPdn().equals(this.notationToPdn());
    that.setFormat(formatThat);
    this.setFormat(formatThis);
    return equals;
  }
}
