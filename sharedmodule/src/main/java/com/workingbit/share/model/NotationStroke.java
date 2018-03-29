package com.workingbit.share.model;

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
  private NotationStrokes alternative = new NotationStrokes();

  public NotationStroke(NotationAtomStroke first, NotationAtomStroke second) {
    this.first = first;
    this.second = second;
  }

  public int getMoveNumberInt() {
    return Integer.parseInt(moveNumber);
  }

  public void setMoveNumber(String moveNumber) {
    this.moveNumber = moveNumber;
  }

  public void setMoveNumber(int moveNumber) {
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

  public void parseStrokeFromPdn(String stroke, boolean first) {
    if (first) {
      this.first = NotationAtomStroke.fromPdn(stroke);
    } else {
      this.second = NotationAtomStroke.fromPdn(stroke);
    }
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
