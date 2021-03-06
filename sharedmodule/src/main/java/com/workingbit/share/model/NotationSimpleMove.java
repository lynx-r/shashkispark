package com.workingbit.share.model;

import com.workingbit.share.domain.DeepClone;
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
public class NotationSimpleMove implements DeepClone {
  private String notation;
  private boolean cursor;
  private EnumNotationFormat format;
  private int boardDimension;

  public NotationSimpleMove(String notation) {
    this.notation = notation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof NotationSimpleMove)) return false;
    NotationSimpleMove that = (NotationSimpleMove) o;
    int oldBoardDimension = that.getBoardDimension();
    that.setBoardDimension(this.boardDimension);
    boolean equals = Objects.equals(getNotationNum(), that.getNotationNum());
    that.setBoardDimension(oldBoardDimension);
    return equals;
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
      throw new RuntimeException("Размерность доски не установлена");
    }
    switch (format) {
      case NUMERIC:
        return getNotationNum();
      case ALPHANUMERIC:
        return getNotationAlpha();
      case SHORT:
        return getNotationAlpha();
      default:
        return notation;
    }
  }

  public String getNotationAlpha() {
    return boardDimension == EnumRules.INTERNATIONAL.getDimension()
        ? getAlphanumericNotation100()
        : getAlphanumericNotation64();
  }

  @SuppressWarnings("unused")
  private void setNotation(String notation) {
    this.notation = notation;
  }

  public String getNotationNum() {
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
    if (notation.matches("[a-j]\\d\\d?")) {
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
