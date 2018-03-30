package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.workingbit.share.domain.DeepClone;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Created by Aleksey Popryaduhin on 21:29 03/10/2017.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NotationDrive implements DeepClone, ToPdn {

  private String notationNumber;
  private int moveNumber;
  private NotationMoves moves = new NotationMoves();
  private NotationDrives variants = new NotationDrives();
  private boolean ellipses;
  private boolean numeric;
  private String comment;

  @DynamoDBIgnore
  public int getNotationNumberInt() {
    return Integer.valueOf(notationNumber.substring(0, notationNumber.indexOf(".")));
  }

  public void setNotationNumberInt(int moveNumber) {
    this.notationNumber = moveNumber + EnumStrokeType.NUMBER.getPdnType();
  }

  public static NotationDrive create(NotationMoves moves) {
    NotationDrive notationDrive = new NotationDrive();
    notationDrive.setMoves(moves);
    return notationDrive;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof NotationDrive)) return false;
    if (!super.equals(o)) return false;
    NotationDrive that = (NotationDrive) o;
    return ellipses == that.ellipses &&
        Objects.equals(notationNumber, that.notationNumber);
  }

  @Override
  public int hashCode() {

    return Objects.hash(super.hashCode(), notationNumber, ellipses);
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
    NotationMove atom = NotationMove.fromPdn(move, boardId);
    moves.add(atom);
  }

  public void addMoveFromPdn(String move) {
    addMoveFromPdn(move, null);
  }

  public String print(String prefix) {
    return new StringBuilder()
        .append(getClass().getSimpleName())
        .append("\n").append(prefix).append("notationNumber: ").append(notationNumber)
        .append("\n").append(prefix).append("notationAtomStrokes: ").append(moves.print(prefix + "\t"))
        .append("\n").append(prefix).append("variants: \n").append(variants.print(prefix + "\t"))
        .append("\n").append(prefix).append("ellipses: ").append(ellipses)
        .append("\n").append(prefix).append("numeric: ").append(numeric)
        .append("\n").append(prefix).append("comment: ").append(comment)
        .append("\n")
        .toString();
  }

  public String toPdn() {
    return notationNumber + " " +
        (!moves.isEmpty() ? moves.toPdn() + " " : "") +
        (!variants.isEmpty() ? "( " + variants.toPdn() + ") " : "") +
        (comment != null ? comment + " " : "");
  }

  public enum EnumStrokeType {
    NUMBER(". ", ". "),
    SIMPLE("-", "-"),
    CAPTURE(":", "x");

    private String type;
    private String pdnType;

    EnumStrokeType(String type, String pdnType) {
      this.type = type;
      this.pdnType = pdnType;
    }

    public String getType() {
      return type;
    }

    public String getPdnType() {
      return pdnType;
    }
  }
}
