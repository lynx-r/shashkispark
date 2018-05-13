package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.workingbit.share.model.enumarable.EnumNotationFormat;
import com.workingbit.share.model.enumarable.EnumRules;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

import static com.workingbit.share.util.Utils.*;

/**
 * Created by Aleksey Popryadukhin on 04/04/2018.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NotationSimpleMove {
  private String notation;
  private DomainId boardId;
  private boolean cursor;
  @DynamoDBIgnore
  private EnumNotationFormat format;
  @DynamoDBIgnore
  private int boardDimension;

  public NotationSimpleMove(String notation, DomainId boardId) {
    this.notation = notation;
    this.boardId = boardId;
  }

  public NotationSimpleMove(String notation, DomainId boardId, boolean curosr) {
    this.notation = notation;
    this.boardId = boardId;
    this.cursor = curosr;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof NotationSimpleMove)) return false;
    NotationSimpleMove that = (NotationSimpleMove) o;
    return Objects.equals(notation, that.notation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(notation);
  }

  public String getNotation() {
    if (format == null) {
      return notation;
    }
    if (boardDimension == 0) {
      return notation;
    }
    switch (format) {
      case PDN:
        return getNotationNum();
      case CLASSIC:
        return getNotationAlpha();
      default:
        return notation;
    }
  }

  private String getNotationAlpha() {
    return boardDimension == EnumRules.INTERNATIONAL.getDimension()
        ? getAlphanumericNotation100()
        : getAlphanumericNotation64();
  }

  @SuppressWarnings("unused")
  private void setNotation(String notation) {
    this.notation = notation;
  }

  private String getNotationNum() {
    return boardDimension == EnumRules.INTERNATIONAL.getDimension()
        ? getPdnNumericNotation100()
        : getPdnNumericNotation64();
  }

  @SuppressWarnings("unused")
  private void setNotationNum(String ignore) {
  }

  private String getPdnNumericNotation64() {
    if (notation.matches("[a-j]\\d")) {
      return ALPHANUMERIC_TO_NUMERIC_64.get(notation);
    }
    return notation;
  }

  private String getPdnNumericNotation100() {
    if (notation.matches("[a-j]\\d")) {
      return ALPHANUMERIC_TO_NUMERIC_100.get(notation);
    }
    return notation;
  }

  private String getAlphanumericNotation64() {
    if (notation.matches("\\d\\d?")) {
      return NUMERIC_TO_ALPHANUMERIC_64.get(notation);
    }
    return notation;
  }

  private String getAlphanumericNotation100() {
    if (notation.matches("\\d\\d?")) {
      return NUMERIC_TO_ALPHANUMERIC_100.get(notation);
    }
    return notation;
  }

}
