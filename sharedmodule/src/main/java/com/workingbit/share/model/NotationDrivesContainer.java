package com.workingbit.share.model;

import com.workingbit.share.domain.DeepClone;
import com.workingbit.share.util.Utils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Aleksey Popryaduhin on 10:12 04/10/2017.
 */
public class NotationDrivesContainer implements ToPdn, DeepClone {

  private NotationDrives history;
  private NotationDrives variants;

  public NotationDrivesContainer() {
    this(true);
  }

  private NotationDrivesContainer(boolean hasRoot) {
    if (hasRoot) {
      variants = NotationDrives.create();
      NotationDrive root = new NotationDrive(true);
      variants.add(root);
      history = NotationDrives.create();
      history.add(root.deepClone());
    } else {
      variants = NotationDrives.create();
      history = NotationDrives.create();
    }
  }

  public NotationDrives getVariants() {
    return variants;
  }

  public NotationDrives getHistory() {
    return history;
  }

  public boolean add(NotationDrive notationDrive) {
    history.add(notationDrive);
    return variants.add(notationDrive);
  }

  public boolean addAll(Collection<? extends NotationDrive> c) {
    history.addAll(c);
    return variants.addAll(c);
  }

  public void add(int index, NotationDrive element) {
    history.add(index, element);
    variants.add(index, element);
  }

  public void addFirst(NotationDrive notationDrive) {
    history.addFirst(notationDrive);
    variants.addFirst(notationDrive);
  }

  public void addLast(NotationDrive notationDrive) {
    history.addLast(notationDrive);
    variants.addLast(notationDrive);
  }

  public boolean addAll(int index, Collection<? extends NotationDrive> c) {
    history.addAll(index, c);
    return variants.addAll(index, c);
  }

  public NotationDrive getFirst() {
    return variants.getFirst();
  }

  public NotationDrive getLastVariant() {
    return variants.getLast();
  }

  public boolean remove(Object o) {
    history.remove(o);
    return variants.remove(o);
  }

  public void removeAllVariants(NotationDrives c) {
    variants.removeAll(c);
  }

  public NotationDrive removeFirst() {
    history.removeFirst();
    return variants.removeFirst();
  }

  public NotationDrive removeLast() {
    history.removeLast();
    return variants.removeLast();
  }

  public int countVariants() {
    return variants.size();
  }

  public int indexOfVariant(NotationDrive o) {
    return variants.indexOf(o);
  }

  public NotationDrives subVariants(int fromIndex, int toIndex) {
    List<NotationDrive> subList = variants.subList(fromIndex, toIndex);
    NotationDrives nd = new NotationDrives();
    nd.addAll(subList);
    return nd;
  }

  public String printVariants(String prefix) {
    return variants.stream()
        .map(notationStroke -> notationStroke.print(prefix + "\t"))
        .collect(Collectors.joining("\n"));
  }

  public void forkAt(NotationDrive forkFromNotationDrive) {
    assert forkFromNotationDrive.getVariants().size() == 1;

    int indexFork = indexOfVariant(forkFromNotationDrive);
    NotationDrives forked = subVariants(indexFork, countVariants());
    NotationDrives forkedNotationDrives = NotationDrives.Builder.getInstance()
        .addAll(forked)
        .build();

    removeAllVariants(forkedNotationDrives);

    NotationDrive variant = forkedNotationDrives.getFirst().deepClone();
    variant.setVariants(forkedNotationDrives);
    variant.setSibling(variant.getVariants().size());

    NotationDrive newDriveNotation = getLastVariant();
    newDriveNotation.setForkNumber(newDriveNotation.getForkNumber() + 1);

    newDriveNotation.getVariants().add(variant);
  }

  public void switchTo(NotationDrive switchToNotationDrive) {
    assert switchToNotationDrive.getVariants().size() == 1;

    int indexFork = indexOfVariant(switchToNotationDrive);
    NotationDrive toSwitchDrive = variants.get(indexFork);
    NotationDrivesContainer toSwitchVariants = toSwitchDrive.getVariants().deepClone();
    toSwitchDrive.getVariants().removeLast();

    // add current notation drive after indexSwitch to variants
    if (indexFork + 1 < countVariants()) {
      List<NotationDrive> forked = subVariants(indexFork + 1, countVariants());
      NotationDrives forkedNotationDrives = NotationDrives.Builder.getInstance()
          .addAll(forked)
          .build();

      // remove tail
      variants.removeAll(forkedNotationDrives);

      NotationDrive variant = forkedNotationDrives.getFirst().deepClone();
      variant.setVariants(forkedNotationDrives);
      toSwitchVariants.add(variant);
    }

    // find in last variants drive to switch
    Optional<NotationDrive> variantToSwitch = toSwitchVariants.variants
        .stream()
        // switchToNotationDrive MUST have one variant to witch user switches
        .filter(nd -> nd.getMoves().equals(switchToNotationDrive.getVariants().get(0).getMoves()))
        .findFirst();

    // TODO Удалять предыдущую ветку??
    // add varint's to switch variants to main notation drives
    variantToSwitch.ifPresent(switchVariant -> {
      addAll(switchVariant.getVariants());
    });
    toSwitchDrive.setForkNumber(toSwitchDrive.getForkNumber() - 1);
  }

  public boolean isEmpty() {
    return variants.isEmpty();
  }

  public NotationDrive get(int index) {
    return variants.get(index);
  }

  @Override
  public String toPdn() {
    return Utils.notationDrivesToPdn(variants);
  }

  public void setVariants(NotationDrives variants) {
    this.variants = variants.deepClone();
  }

  public static NotationDrivesContainer create() {
    return new NotationDrivesContainer(false);
  }

  public static NotationDrivesContainer createWithRoot() {
    return new NotationDrivesContainer(true);
  }
}
