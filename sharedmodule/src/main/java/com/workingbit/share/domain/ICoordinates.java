package com.workingbit.share.domain;

import com.workingbit.share.model.enumarable.EnumRules;

import static com.workingbit.share.util.Utils.*;

/**
 * Created by Aleksey Popryaduhin on 15:10 11/08/2017.
 */
public interface ICoordinates {

  /**
   * row
   */
  int getV();

  void setV(int v);

  /**
   * col
   */
  int getH();

  void setH(int h);

  /**
   * Board's dimension
   */
  int getDim();

  void setDim(int dim);

  default String getNotation() {
    return getDim() == EnumRules.INTERNATIONAL.getDimension()
        ? getPdnNumericNotation100()
        : getAlphanumericNotation();
  }

  @SuppressWarnings("unused")
  default void setNotation(String ignore) {
  }

  default String getNotationNum() {
    return getDim() == EnumRules.INTERNATIONAL.getDimension()
        ? getPdnNumericNotation100()
        : getPdnNumericNotation64();
  }

  @SuppressWarnings("unused")
  default void setNotationNum(String ignore) {
  }

  private String getPdnNumericNotation64() {
    return ALPHANUMERIC_TO_NUMERIC_64.get(getAlphanumericNotation());
  }

  private String getPdnNumericNotation100() {
    return ALPHANUMERIC_TO_NUMERIC_100.get(getAlphanumericNotation());
  }

  private String getAlphanumericNotation() {
    return ALPH.get(getH()) + (getDim() - getV());
  }
}
