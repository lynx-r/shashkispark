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

  /**
   * Number of drive in notation
   */
  private String notationNumber;

  /**
   * Number of move in `moves`
   */
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
    this.notationNumber = moveNumber + EnumMoveType.NUMBER.getPdnType();
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
        .append(prefix).append("\t").append("notationNumber: ").append(notationNumber)
        .append(prefix).append("\t").append("notationMoves: ").append(moves.print(prefix + "\t"))
        .append(prefix).append("\t").append("variants: \n").append(variants.print(prefix + "\t"))
        .append(prefix).append("\t").append("ellipses: ").append(ellipses)
        .append(prefix).append("\t").append("numeric: ").append(numeric)
        .append(prefix).append("\t").append("comment: ").append(comment)
        .append("\n")
        .toString();
  }

  public String toPdn() {
    return notationNumber + " " +
        (!moves.isEmpty() ? moves.toPdn() + " " : "") +
        (!variants.isEmpty() ? "( " + variants.toPdn() + ") " : "") +
        (comment != null ? comment + " " : "");
  }

  public enum EnumMoveType {
    NUMBER(". ", ". "),
    SIMPLE("-", "-"),
    CAPTURE(":", "x"),
    END_GAME_SYMBOL("*", "*");

    private String type;
    private String pdnType;

    EnumMoveType(String type, String pdnType) {
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
