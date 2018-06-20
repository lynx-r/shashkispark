package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.domain.DeepClone;
import com.workingbit.share.model.enumarable.EnumNotation;
import com.workingbit.share.model.enumarable.EnumNotationFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Aleksey Popryaduhin on 21:29 03/10/2017.
 */
@AllArgsConstructor
@Data
public class NotationDrive implements DeepClone, NotationFormat {

  /**
   * Number of drive in notation
   */
  private String notationNumber;

  /**
   * Number of move in `moves`
   */
  private NotationMoves moves;

  private NotationDrives variants;

  private DomainId notationHistoryId;

  private boolean ellipses;
  /**
   * Does notation in notationFormat format like 1. 12-21
   */
  private EnumNotationFormat notationFormat;

  private int boardDimension;

  private String comment;
  /**
   * Mark drive as first in list
   */
  private boolean root;

  /**
   * is selected notation drive
   */
  private boolean selected;

  /**
   * is current variant
   */
  private boolean current;
  /**
   * is previous current variant
   */
  private boolean previous;

  /**
   * Id drive in variants
   */
  private int idInVariants;

  private int ancestors;

  /**
   * idInVariants of parent item
   */
  private int parentId;

  private String parentColor;

  private String driveColor;

  /**
   * Начиная с этого хода идет скрытая задача
   */
  private boolean taskBelow;

  public NotationDrive() {
    variants = NotationDrives.create();
    moves = new NotationMoves();
  }

  public NotationDrive(boolean root) {
    this();
    this.root = root;
  }

  @NotNull
  public static NotationDrive create(NotationMoves moves) {
    NotationDrive notationDrive = new NotationDrive();
    notationDrive.setNotationNumber(null);
    notationDrive.setMoves(moves);
    return notationDrive;
  }

  public static void copyMetaOf(NotationDrive orig, NotationDrive target) {
    target.notationFormat = orig.notationFormat;
  }

  public static void copyOf(@NotNull NotationDrive orig, @NotNull NotationDrive target) {
    copyMetaOf(orig, target);
    target.ellipses = orig.ellipses;
    target.comment = orig.comment;
    target.variants = target.variants.deepClone();
    target.notationNumber = orig.notationNumber;
    target.moves = orig.moves.deepClone();
  }

  public boolean addVariant(NotationDrive variant) {
    return variants.add(variant);
  }

  public boolean addAllVariants(@NotNull NotationDrives variants) {
    return this.variants.addAll(variants);
  }

  public NotationDrives getVariants() {
    return variants;
  }

  public void setVariants(@NotNull NotationDrives variants) {
    this.variants = variants.deepClone();
  }

  @DynamoDBIgnore
  public int getNotationNumberInt() {
    if (StringUtils.isNotBlank(notationNumber)) {
      return Integer.valueOf(notationNumber.substring(0, notationNumber.indexOf(".")));
    }
    return 0;
  }

  public void setNotationNumberInt(int moveNumber) {
    this.notationNumber = moveNumber + EnumNotation.NUMBER.getPdn();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof NotationDrive)) return false;
//    if (!super.equals(o)) return false;
    NotationDrive that = (NotationDrive) o;
    return ellipses == that.ellipses &&
        Objects.equals(moves, that.moves) &&
        Objects.equals(getNotationNumberInt(), that.getNotationNumberInt());
  }

  @Override
  public int hashCode() {

    return Objects.hash(super.hashCode(), getNotationNumberInt(), ellipses);
  }

  public void parseNameFromPdn(@NotNull String name) {
    switch (name) {
      case "NUMERICMOVE":
        notationFormat = EnumNotationFormat.DIGITAL;
        break;
      case "ALPHANUMERICMOVE":
        notationFormat = EnumNotationFormat.ALPHANUMERIC;
        break;
      case "SHORTMOVE":
        notationFormat = EnumNotationFormat.SHORT;
        break;
      case "ELLIPSES":
        ellipses = true;
        break;
    }
  }

  public void addMoveFromPdn(@NotNull String move) {
    NotationMove atom = NotationMove.fromPdn(move, notationFormat);
    moves.add(atom);
  }

  public void setBoardDimension(int boardDimension) {
    this.boardDimension = boardDimension;
    moves.setBoardDimension(boardDimension);
    variants.setDimension(boardDimension);
  }

  @NotNull
  public String print(String prefix) {
    if (root) {
      return prefix + getClass().getSimpleName() +
          prefix + "\t" + "root" +
          prefix + "\t" + "variants: " + variants.print(prefix + "\t");
    }
    return new StringBuilder()
        .append(prefix).append(getClass().getSimpleName())
        .append(prefix).append("\t").append("notationNumber: ").append(notationNumber)
        .append(prefix).append("\t").append("notationMoves: ").append(moves.print(prefix + "\t"))
        .append(prefix).append("\t").append("variants: ").append(variants.print(prefix + "\t"))
        .append(prefix).append("\t").append("ellipses: ").append(ellipses)
        .append(prefix).append("\t").append("notationFormat: ").append(notationFormat)
        .append(prefix).append("\t").append("comment: ").append(comment)
        .append("\n")
        .toString();
  }

  @Override
  public String asString() {
    if (root) {
      return variants.variantsToPdn();
    }
    return (StringUtils.isNotBlank(notationNumber) ? notationNumber : "") +
        (!moves.isEmpty() ? moves.asString() + " " : "") +
        (!variants.isEmpty() ? variants.variantsToPdn() : "");
  }

  @Override
  public String asTree(String indent, String tabulation) {
    if (root) {
      return variants.variantsToTreePdn(indent + tabulation, tabulation);
    }
    return (StringUtils.isNotBlank(notationNumber) ? indent + notationNumber : "") +
        (!moves.isEmpty() ? moves.asString() + " " : "") +
        (!variants.isEmpty() ? variants.variantsToTreePdn(indent + tabulation, tabulation) : "\n");
  }

  @Override
  public String toString() {
    return "NotationDrive{" +
        "notationNumber='" + notationNumber + '\'' +
        ", moves=" + moves +
        ", variants=" + variants +
        ", current=" + current +
        ", previous=" + previous +
        ", idInVariants=" + idInVariants +
        '}';
  }

  @JsonIgnore
  @DynamoDBIgnore
  public int getVariantsSize() {
    return variants.size();
  }

  public NotationDrive removeLastVariant() {
    return variants.removeLast();
  }

  public void setNotationFormat(EnumNotationFormat format) {
    this.notationFormat = format;
    moves.setNotationFormat(format);
    variants.setNotationFormat(format);
  }

  @JsonIgnore
  @DynamoDBIgnore
  public NotationDrive getLastVariant() {
    return variants.getLast();
  }

  @JsonIgnore
  @DynamoDBIgnore
  public Optional<NotationDrive> getVariantById(int idInVariants) {
    return variants
        .stream()
        .filter(notationDrive -> notationDrive.getIdInVariants() == idInVariants)
        .findFirst();
  }

  @JsonIgnore
  @DynamoDBIgnore
  public List<NotationDrive> getVariantSubList(int start, int end) {
    if (variants.isEmpty() || start >= variants.size()) {
      return new ArrayList<>();
    }
    return variants.subList(start, end);
  }

  public NotationDrives filterVariantById(int removeVariantId) {
    return variants
        .stream()
        .filter(notationDrive -> notationDrive.getIdInVariants() != removeVariantId)
        .collect(Collectors.toCollection(NotationDrives::new));
  }

  @JsonIgnore
  @DynamoDBIgnore
  public int getVariantId() {
    int variantsSize = getVariantsSize();
    boolean found = variants.stream()
        .anyMatch(notationDrive -> notationDrive.getIdInVariants() == getVariantsSize());
    while (found) {
      int finalVariantsSize = variantsSize;
      found = variants.stream()
          .anyMatch(notationDrive -> notationDrive.getIdInVariants() == finalVariantsSize);
      if (found) {
        variantsSize++;
      }
    }
    return variantsSize;
  }
}
