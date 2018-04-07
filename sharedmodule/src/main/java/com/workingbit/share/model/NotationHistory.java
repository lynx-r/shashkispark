package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.domain.DeepClone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by Aleksey Popryaduhin on 10:12 04/10/2017.
 */
public class NotationHistory implements DeepClone {

  private final Logger logger = LoggerFactory.getLogger(NotationHistory.class);

  private NotationDrives history;
  private NotationDrives notation;

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

  public NotationDrives getHistory() {
    return history;
  }

  public void setHistory(NotationDrives history) {
    this.history = history;
  }

  public void add(NotationDrive notationDrive) {
    add(size(), notationDrive);
  }

  public void addAll(Collection<? extends NotationDrive> c) {
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
  public NotationDrive getLastOrCreateIfRoot() {
    boolean isRootDrive = notation.getLast().isRoot();
    if (isRootDrive) {
      NotationDrive notationDrive = new NotationDrive();
      notationDrive.setNotationNumberInt(1);
      add(notationDrive);
      return notationDrive;
    } else {
      return notation.getLast();
    }
  }

  @JsonIgnore
  @DynamoDBIgnore
  public NotationDrive getLast() {
    return notation.getLast();
  }

  public void addMovesToLast(NotationMoves moves) {
    NotationMoves ms = getLastOrCreateIfRoot().getMoves();
    ms.addAll(moves);
    history.getLast().setMoves(ms.deepClone());
  }

  public boolean remove(NotationDrive o) {
    history.remove(o);
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

  public NotationDrives subList(int fromIndex, int toIndex) {
    List<NotationDrive> subList = notation.subList(fromIndex, toIndex);
    NotationDrives nd = new NotationDrives();
    nd.addAll(subList);
    return nd;
  }

  public boolean forkAt(NotationDrive forkFromNotationDrive) {
    if (forkFromNotationDrive.getMoves().size() < 2) {
      System.out.println("Ignore fork " + forkFromNotationDrive.toPdn());
      return false;
    }

    int indexFork = indexOf(forkFromNotationDrive);
    NotationDrives forked = subList(indexFork, size());
    NotationDrives forkedNotationDrives = NotationDrives.Builder.getInstance()
        .addAll(forked)
        .build();

    removeAll(forkedNotationDrives);

    NotationDrive variant = forkedNotationDrives.getFirst().deepClone();
    variant.setVariants(forkedNotationDrives);
    variant.setSibling(variant.getVariantsSize());

    NotationDrive newDriveNotation = getLast();
    int forkNumber = newDriveNotation.getForkNumber() + 1;
    newDriveNotation.setForkNumber(forkNumber);

    newDriveNotation.addVariant(variant);

    history.removeAll(forkedNotationDrives);
    history.getLast().setForkNumber(forkNumber);
    history.getLast().addVariant(variant);

    logger.info("Нотация после хода: " + pdnString());
    return true;
  }

  public boolean switchTo(NotationDrive switchToNotationDrive) {
    assert switchToNotationDrive == null || switchToNotationDrive.getVariantsSize() == 1;

    int indexFork = indexOf(switchToNotationDrive);
    NotationDrive v_toSwitchDrive;
    if (switchToNotationDrive == null) {
      v_toSwitchDrive = getLastOrCreateIfRoot();
      indexFork = indexOf(v_toSwitchDrive);
    } else {
      v_toSwitchDrive = get(indexFork);
    }

    if (v_toSwitchDrive.getMoves().size() == 1) {
      System.out.println("Ignore switch: " + v_toSwitchDrive.toPdn());
      return false;
    }

    NotationDrives v_switchVariants = v_toSwitchDrive.getVariants().deepClone();
    v_toSwitchDrive.removeLastVariant();

    // add rest notation to notation
    if (indexFork + 1 < size()) {
      List<NotationDrive> forked = subList(indexFork + 1, size());
      NotationDrives forkedNotationDrives = NotationDrives.Builder.getInstance()
          .addAll(forked)
          .build();

      // remove tail
      removeAll(forkedNotationDrives);
//      history.removeAll(forkedNotationDrives);

      NotationDrive variant = forkedNotationDrives.getFirst().deepClone();
      variant.setVariants(forkedNotationDrives);
      v_switchVariants.add(variant);
      history.getLast().getVariants().add(variant);
    }

    // find in last notation drive to switch
    Optional<NotationDrive> variantToSwitch;
    if (switchToNotationDrive == null) {
      NotationDrive first = v_switchVariants.getFirst();
      variantToSwitch = Optional.of(first);
      history.getLast().getVariants().add(first);
    } else {
      variantToSwitch = v_switchVariants
          .stream()
          // switchToNotationDrive MUST have one variant to witch user switches
          .filter(nd -> nd.getMoves().equals(switchToNotationDrive.getVariants().get(0).getMoves()))
          .findFirst();
    }

    // add varint's to switch notation to main notation drives
    variantToSwitch.ifPresent(switchVariant -> {
      addAll(switchVariant.getVariants());
    });
    v_toSwitchDrive.setForkNumber(v_toSwitchDrive.getForkNumber() - 1);

    NotationDrive h_toSwitchDrive = history.get(indexFork);
    h_toSwitchDrive.setForkNumber(h_toSwitchDrive.getForkNumber() - 1);

    logger.info("Нотация после хода: " + pdnString());
    return true;
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

  public Optional<String> findLastVariantBoardId() {
    try {
      NotationMoves moves = notation
          .getLast()
          .getMoves();
      if (moves.isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(
          moves
              .getLast()
              .getLastMoveBoardId());
    } catch (Exception e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  @JsonIgnore
  @DynamoDBIgnore
  public NotationMove getLastMove() {
    return notation.getLast().getMoves().getLast();
  }

  public String pdnString() {
    return "\n" +
        "ИСТОРИЯ: " +
        history.toPdn() +
        "\n" +
        "НОТАЦИЯ: " +
        notation.toPdn() +
        "\n";
  }
}
