package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.domain.DeepClone;
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
public class NotationDrive implements DeepClone, ToPdn {

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
  private boolean numeric;
  private String comment;
  private boolean root;
  /**
   * Root of variants
   */
  private boolean subRoot;
  /**
   * Is the current drive a forkNumber?
   */
  private int forkNumber;
  private int sibling;

  public NotationDrive() {
    variants = NotationDrives.create();
    moves = new NotationMoves();
  }

  public NotationDrive(boolean root) {
    this();
    this.root = root;
  }

  public static NotationDrive create(NotationMoves moves) {
    NotationDrive notationDrive = new NotationDrive();
    notationDrive.setNotationNumber(null);
    notationDrive.setMoves(moves);
    return notationDrive;
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
        numeric = true;
        break;
      case "ALPHANUMERICMOVE":
        numeric = false;
        break;
      case "ELLIPSES":
        ellipses = true;
        break;
    }
  }

  public void addMoveFromPdn(String move, String boardId) {
    NotationMove atom = NotationMove.fromPdn(move);
    moves.add(atom);
  }

  public void addMoveFromPdn(String move) {
    addMoveFromPdn(move, null);
  }

  public String print(String prefix) {
    if (root) {
      return prefix + getClass().getSimpleName() +
          prefix + "\t" + "root" +
          prefix + "\t" + "variants: " + variants.print(prefix + "\t");
    }
    return new StringBuilder()
        .append(prefix).append(getClass().getSimpleName())
        .append(prefix).append("\t").append("subRoot: ").append(subRoot)
        .append(prefix).append("\t").append("forkNumber: ").append(forkNumber)
        .append(prefix).append("\t").append("sibling: ").append(sibling)
        .append(prefix).append("\t").append("notationNumber: ").append(notationNumber)
        .append(prefix).append("\t").append("notationMoves: ").append(moves.print(prefix + "\t"))
        .append(prefix).append("\t").append("variants: ").append(variants.print(prefix + "\t"))
        .append(prefix).append("\t").append("ellipses: ").append(ellipses)
        .append(prefix).append("\t").append("numeric: ").append(numeric)
        .append(prefix).append("\t").append("comment: ").append(comment)
        .append("\n")
        .toString();
  }

  public String toPdn() {
    if (root) {
      return variants.variantsToPdn();
    }
    return (StringUtils.isNotBlank(notationNumber) ? notationNumber + " " : "" ) +
        (!moves.isEmpty() ? moves.toPdn() + " " : "") +
        (!variants.isEmpty() ? variants.variantsToPdn() : "");
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("notationNumber", notationNumber)
        .append("moves", moves)
        .append("variants", variants)
        .append("root", root)
        .append("subRoot", subRoot)
        .append("forkNumber", forkNumber)
        .append("sibling", sibling)
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
}
