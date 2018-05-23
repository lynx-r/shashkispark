package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.workingbit.share.converter.NotationDrivesConverter;
import com.workingbit.share.converter.NotationDrivesDeserializer;
import com.workingbit.share.converter.NotationDrivesSerializer;
import com.workingbit.share.domain.DeepClone;
import com.workingbit.share.model.enumarable.EnumNotationFormat;
import com.workingbit.share.model.enumarable.EnumRules;
import com.workingbit.share.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

/**
 * Created by Aleksey Popryaduhin on 10:12 04/10/2017.
 */
public class NotationHistory implements DeepClone {

  @JsonSerialize(using = NotationDrivesSerializer.class)
  @JsonDeserialize(using = NotationDrivesDeserializer.class)
  @DynamoDBTypeConverted(converter = NotationDrivesConverter.class)
  private NotationDrives notation;
  /**
   * using for fork and switch
   */
  private int currentNotationDrive;
  /**
   * using for fork and switch
   */
  private int variantNotationDrive;

  public NotationHistory() {
    createWithRoot();
  }

  public NotationHistory(NotationDrives notation) {
    this.notation = notation;
  }

  @NotNull
  private static NotationDrives createNotationDrives(boolean hasRoot) {
    if (hasRoot) {
      NotationDrives notationHistory = NotationDrives.create();
      NotationDrive root = new NotationDrive(true);
      root.setSelected(true);
      notationHistory.add(root);
      return notationHistory;
    } else {
      return NotationDrives.create();
    }
  }

  public NotationDrives getNotation() {
    return notation;
  }

  public void setNotation(NotationDrives notation) {
    this.notation = notation;
  }

  @DynamoDBIgnore
  @JsonIgnore
  public void setNotation(@NotNull List<NotationDrive> notationDrives) {
    this.notation = new NotationDrives(notationDrives);
  }

  public void setRules(@NotNull EnumRules rules) {
    notation.setDimension(rules.getDimensionAbs());
  }

  public void setFormat(EnumNotationFormat format) {
    notation.setNotationFormat(format);
  }


  public int getCurrentNotationDrive() {
    return currentNotationDrive;
  }

  public void setCurrentNotationDrive(int currentNotationDrive) {
    this.currentNotationDrive = currentNotationDrive;
  }

  public int getVariantNotationDrive() {
    return variantNotationDrive;
  }

  public void setVariantNotationDrive(int variantNotationDrive) {
    this.variantNotationDrive = variantNotationDrive;
  }

  public void add(NotationDrive element) {
    notation.add(element);
  }

  @JsonIgnore
  @DynamoDBIgnore
  public NotationDrive getLast() {
    return notation.getLast();
  }

  public int size() {
    return notation.size();
  }

  @NotNull
  private NotationDrives subListNotation(int fromIndex, int toIndex) {
    List<NotationDrive> subList = notation.subList(fromIndex, toIndex);
    NotationDrives nd = new NotationDrives();
    nd.addAll(subList);
    return nd;
  }

  public boolean forkAt(int forkFromNotationDrive) {
    NotationDrives cutpoint = subListNotation(forkFromNotationDrive, size());
    NotationDrives cutNotationDrives = cutpoint.deepClone();
    NotationDrive fVariant = get(forkFromNotationDrive).deepClone();

//    if (forkFromNotationDrive != 0) {
    notation.removeAll(cutpoint);
//    } else {
//      notation.removeAll(cutpoint.subList(1, cutpoint.size()));
//    }
    notation
        .stream()
        .filter(NotationDrive::isCurrent)
        .findFirst()
        .ifPresent(notationDrive -> {
          notationDrive.setSelected(false);
          notationDrive.setCurrent(false);
        });

    // if first variant
    NotationDrive rootV = null;
    if (fVariant.getVariantsSize() == 0) {
      rootV = fVariant.deepClone();
      NotationDrives rootCut = cutNotationDrives.deepClone();
      rootCut.setIdInVariants(0);
      rootV.setVariants(rootCut);
      rootV.setSelected(false);
      rootV.setPrevious(true);
      rootV.setCurrent(false);
      rootV.setParentColor("purple");
      rootV.setDriveColor(Utils.getRandomColor());
    }
    NotationDrive forkedVariant = fVariant.deepClone();
    forkedVariant.setDriveColor(Utils.getRandomColor());

    if (rootV != null) {
      forkedVariant.addVariant(rootV);
      forkedVariant.setParentColor(rootV.getParentColor());
    }
    cutNotationDrives.setIdInVariants(forkedVariant.getIdInVariants());
//    NotationDrives lastVariants = notation.getLast().getVariants();
    NotationDrive firstNew = cutNotationDrives.getFirst().deepClone();
//    if (forkFromNotationDrive == 0) {
//      firstNew.setIdInVariants(lastVariants.size());
//      lastVariants
//          .stream()
//          .filter(NotationDrive::isPrevious)
//          .findFirst()
//          .ifPresent(notationDrive -> notationDrive.setPrevious(false));
//      lastVariants
//          .stream()
//          .filter(NotationDrive::isCurrent)
//          .findFirst()
//          .ifPresent(notationDrive -> {
//            notationDrive.setCurrent(false);
//            notationDrive.setPrevious(true);
//          });
//      firstNew.setVariants(notation.getFirst().getVariants().deepClone());
//    } else {
    firstNew.setIdInVariants(forkedVariant.getVariantsSize());
//    }
    firstNew.setCurrent(true);
    NotationDrives switchVariants = forkedVariant.getVariants();
    if (rootV == null) {
      switchVariants
          .stream()
          .filter(NotationDrive::isPrevious)
          .findFirst()
          .ifPresent(notationDrive -> notationDrive.setPrevious(false));
    } else {
      firstNew.setParentId(0);
      firstNew.setParentColor(rootV.getDriveColor());
      firstNew.setDriveColor(Utils.getRandomColor());
    }
    cutNotationDrives.getFirst().setVariants(NotationDrives.create());
    firstNew.setVariants(new NotationDrives(List.of(cutNotationDrives.getFirst())));
    switchVariants
        .stream()
        .filter(NotationDrive::isCurrent)
        .findFirst()
        .ifPresent(currentVariant -> {
          currentVariant.setAncestors(currentVariant.getAncestors() + 1);
          firstNew.setParentId(currentVariant.getIdInVariants());
          firstNew.setParentColor(currentVariant.getDriveColor());
          currentVariant.setCurrent(false);
          currentVariant.setPrevious(true);
        });
    forkedVariant.addVariant(firstNew);
    forkedVariant.setCurrent(true);
    forkedVariant.setSelected(true);

//    if (forkFromNotationDrive == 0) {
//      lastVariants.add(firstNew);
//    } else {
    notation.add(forkedVariant);

    setLastMoveCursor();
    return true;
  }

  public boolean switchTo(int currentNotationDrive,
                          int variantNotationDrive) {
    getCurrentVariant()
        .ifPresent(notationDrive -> {
          NotationDrive switchTo = notation.get(currentNotationDrive);

          NotationDrives toRemove = notationDrive.getVariants();
          notation.removeAll(toRemove.subList(1, toRemove.size()));

          NotationDrives switchToVariants = switchTo.getVariants();
          switchToVariants
              .forEach(nd -> nd.setPrevious(false));
          switchToVariants
              .stream()
              .filter(NotationDrive::isCurrent)
              .findFirst()
              .ifPresent(current -> {
                current.setCurrent(false);
                current.setPrevious(true);
              });
          NotationDrive variantToSwitch = switchToVariants.get(variantNotationDrive);
          variantToSwitch.setCurrent(true);
          variantToSwitch.setPrevious(false);

          // replace first drive's variants with switched on variants
          NotationDrives variantsToSwitch = variantToSwitch.getVariants();
          NotationDrive notationDriveSwitched = variantsToSwitch.getFirst();
          notationDriveSwitched.setCurrent(true);
          notationDriveSwitched.setPrevious(false);

          List<NotationDrive> newVariants = variantsToSwitch.subList(1, variantsToSwitch.size());
          newVariants
              .stream()
              .filter(NotationDrive::isSelected)
              .findFirst()
              .ifPresent(nd -> nd.setSelected(false));
          notation.addAll(newVariants);
          notation.getLast().setSelected(true);
          setLastMoveCursor();
        });
    return true;
  }

  public void removeByNotationNumberInVariants(int notationNumber, int variantNumber) {
    notation
        .stream()
        .filter(notationDrive -> notationDrive.getNotationNumberInt() == notationNumber)
        .findFirst()
        .ifPresent(inNotation ->
            inNotation.getVariants()
                .removeIf(inVariants -> inVariants.getIdInVariants() == variantNumber)
        );
  }

  public void setLastMoveCursor() {
    if (notation.isEmpty()) {
      return;
    }
    notation.forEach(nd -> nd.getMoves().resetCursors());
    NotationMoves moves = notation.getLast().getMoves();
    if (!moves.isEmpty()) {
      NotationSimpleMove lastMove = moves.getLastMove();
      if (lastMove != null) {
        lastMove.setCursor(true);
      }
    }
  }

  @JsonIgnore
  @DynamoDBIgnore
  public boolean isEmpty() {
    return notation.isEmpty()
        || notation.size() == 1
        && notation.getLast().getVariants().isEmpty();
  }

  @JsonIgnore
  @DynamoDBIgnore
  public NotationDrive get(int index) {
    return notation.get(index);
  }

  public static NotationHistory createNotationDrives() {
    return new NotationHistory(createNotationDrives(false));
  }

  public static NotationHistory createWithRoot() {
    return new NotationHistory(createNotationDrives(true));
  }

  public String notationToPdn() {
    return notation.asString();
  }

  public String notationToTreePdn(String indent, String tabulation) {
    return notation.asTree(indent, tabulation);
  }

  public void printPdn() {
    System.out.println(notationToPdn());
  }

  @JsonIgnore
  @DynamoDBIgnore
  public Optional<DomainId> getLastNotationBoardId() {
    NotationDrive notationLast = notation.getLast();
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
  }

  @JsonIgnore
  @DynamoDBIgnore
  public Optional<DomainId> getLastNotationBoardIdInVariants() {
    NotationDrive notationLast = notation.getLast();
    if (notationLast.isRoot()) {
      return notationLast.getVariants()
          .stream()
          .filter(NotationDrive::isCurrent)
          .flatMap(notationDrive -> notationDrive.getVariants().stream())
          .filter(NotationDrive::isSelected)
          .map(NotationDrive::getMoves)
          .map(NotationMoves::getLastMove)
          .filter(Objects::nonNull)
          .map(NotationSimpleMove::getBoardId)
          .findFirst();
    }
    return getCurrentVariantStream()
        .map(NotationDrive::getVariants)
        .map(LinkedList::getLast)
        .map(NotationDrive::getMoves)
        .map(NotationMoves::getLastMove)
        .filter(Objects::nonNull)
        .map(NotationSimpleMove::getBoardId)
        .findFirst();
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

  @NotNull
  public String debugPdnString() {
    return "\n" +
        "NOTATION: " +
        notation.asString();
  }

  @DynamoDBIgnore
  @JsonIgnore
  private Optional<NotationDrive> getCurrentVariant() {
    return getCurrentVariantStream()
        .findFirst();
  }

  @DynamoDBIgnore
  @JsonIgnore
  private Stream<NotationDrive> getCurrentVariantStream() {
    return notation
        .stream()
        .filter(NotationDrive::isCurrent)
        .flatMap(notationDrive -> notationDrive.getVariants().stream())
        .filter(NotationDrive::isCurrent);
  }

  public boolean isEqual(@NotNull NotationHistory that) {
    EnumNotationFormat formatThat = that.getNotation().getFirst().getNotationFormat();
    EnumNotationFormat formatThis = this.getNotation().getFirst().getNotationFormat();
    that.setFormat(EnumNotationFormat.DIGITAL);
    this.setFormat(EnumNotationFormat.DIGITAL);
    boolean equals = that.notationToPdn().equals(this.notationToPdn());
    that.setFormat(formatThat);
    this.setFormat(formatThis);
    return equals;
  }

  public void syncMoves() {
    getCurrentVariant().ifPresent(notationDrive -> {
      if (getLast().getMoves().size() == 2) {
        if (notationDrive.getNotationNumberInt() == getLast().getNotationNumberInt()) {
          notationDrive.setMoves(getLast().getMoves());
        }
        notationDrive.getLastVariant().setMoves(getLast().getMoves());
      } else {
        notationDrive.addVariant(getLast());
      }
    });
    setLastMoveCursor();
  }

  public void setLastSelected(boolean selected) {
    getLast().setSelected(selected);
  }
}
