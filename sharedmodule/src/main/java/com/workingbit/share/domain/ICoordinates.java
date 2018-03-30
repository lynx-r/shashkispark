package com.workingbit.share.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.workingbit.share.model.EnumRules;

import java.util.Map;

import static com.workingbit.share.util.Utils.ALPH;
import static com.workingbit.share.util.Utils.ALPHANUMERIC64_TO_NUMERIC100;
import static com.workingbit.share.util.Utils.ALPHANUMERIC64_TO_NUMERIC64;

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
        : getAlphanumericNotation64();
  }

  @DynamoDBIgnore
  default String getPdnNumericNotation100() {
    return ALPHANUMERIC64_TO_NUMERIC100.get(getAlphanumericNotation64());
  }

  default void setPdnNotationNumeric100(String notation) {
  }

  @DynamoDBIgnore
  default String getAlphanumericNotation64() {
    return ALPH.get(getH()) + (getDim() - getV());
  }

  default void setAlphanumericNotation64(String notation) {
  }

  static String toAlphanumericNotation64(String notation) {
      // try from numeric to alphanumeric
      return ALPHANUMERIC64_TO_NUMERIC64
          .entrySet()
          .stream()
          .filter(stringStringEntry -> stringStringEntry.getValue().equals(notation))
          .findFirst()
          .map(Map.Entry::getKey)
          // already alphanumeric
          .orElse(notation);
  }
}
