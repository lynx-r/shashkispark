package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.domain.DeepClone;
import com.workingbit.share.model.enumarable.EnumNotation;
import com.workingbit.share.model.enumarable.EnumNotationFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;

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
  private boolean ellipses;
  /**
   * Does notation in notationFormat format like 1. 12-21
   */
  @DynamoDBIgnore
  private EnumNotationFormat notationFormat;

  @DynamoDBIgnore
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

  public NotationDrive() {
    variants = NotationDrives.create();
    moves = new NotationMoves();
  }

  NotationDrive(boolean root) {
    this();
    this.root = root;
  }

  public static NotationDrive create(NotationMoves moves) {
    NotationDrive notationDrive = new NotationDrive();
    notationDrive.setNotationNumber(null);
    notationDrive.setMoves(moves);
    return notationDrive;
  }

  public static void copyMetaOf(NotationDrive orig, NotationDrive target) {
    target.notationFormat = orig.notationFormat;
  }

  public static void copyOf(NotationDrive orig, NotationDrive target) {
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

  public boolean addAllVariants(NotationDrives variants) {
    return this.variants.addAll(variants);
  }

  public NotationDrives getVariants() {
    return variants;
  }

  public void setVariants(NotationDrives variants) {
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

  public void parseNameFromPdn(String name) {
    switch (name) {
      case "NUMERICMOVE":
        notationFormat = EnumNotationFormat.ЧИСЛОВОЙ;
        break;
      case "ALPHANUMERICMOVE":
        notationFormat = EnumNotationFormat.БУКВЕННЫЙ;
        break;
      case "ELLIPSES":
        ellipses = true;
        break;
    }
  }

  public void addMoveFromPdn(String move) {
    NotationMove atom = NotationMove.fromPdn(move);
    moves.add(atom);
  }

  public void setBoardDimension(int boardDimension) {
    this.boardDimension = boardDimension;
    moves.setBoardDimension(boardDimension);
    variants.setDimension(boardDimension);
  }

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
    return new ToStringBuilder(this)
        .append("notationNumber", notationNumber)
        .append("moves", moves)
        .append("variants", variants)
        .append("root", root)
        .toString();
  }

  @DynamoDBIgnore
  @JsonIgnore
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
}
