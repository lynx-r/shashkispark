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
public class NotationStroke implements DeepClone {

  private String moveNumber;
  private NotationAtomStroke first;
  private NotationAtomStroke second;
  private NotationStrokes variants = new NotationStrokes();
  private boolean ellipses;
  private boolean numeric;
  private String comment;

  public NotationStroke(NotationAtomStroke first, NotationAtomStroke second) {
    this.first = first;
    this.second = second;
  }

  @DynamoDBIgnore
  public int getMoveNumberInt() {
    return Integer.valueOf(moveNumber.substring(0, moveNumber.indexOf(".")));
  }

  public void setMoveNumberInt(int moveNumber) {
    this.moveNumber = moveNumber + EnumStrokeType.NUMBER.getPdnType();
  }

  /**
   * WARN equals without comparing with super
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NotationStroke that = (NotationStroke) o;
    return Objects.equals(moveNumber, that.moveNumber) &&
        Objects.equals(first, that.first) &&
        Objects.equals(second, that.second);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), moveNumber, first, second);
  }

  public void parseStrokeFromPdn(String stroke, boolean first, String name) {
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
    if (first) {
      this.first = NotationAtomStroke.fromPdn(stroke);
    } else {
      this.second = NotationAtomStroke.fromPdn(stroke);
    }
  }

  public String print(String prefix) {
    return new StringBuilder()
        .append(getClass().getSimpleName())
        .append("\n").append(prefix).append("moveNumber: ").append(moveNumber)
        .append("\n").append(prefix).append("first: ").append(first != null ? first.print( "\n\t" + prefix) : "")
        .append("\n").append(prefix).append("second: ").append(second != null ? second.print("\n\t" + prefix) : "")
        .append("\n").append(prefix).append("variants: \n").append(variants.print(prefix + "\t"))
        .append("\n").append(prefix).append("ellipses: ").append(ellipses)
        .append("\n").append(prefix).append("numeric: ").append(numeric)
        .append("\n").append(prefix).append("comment: ").append(comment)
        .append("\n")
        .toString();
  }

  public String toPdn() {
    return moveNumber + " " +
        (first != null ? first.toPdn() + " " : "") +
        (second != null ? second.toPdn() + " " : "") +
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
