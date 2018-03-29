package com.workingbit.share.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.NotImplementedException;

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

  @DynamoDBIgnore
  default String getPdnNotation() {
    return String.format("%s%s", getH(), getDim() - getV());
  }

  default void setPdnNotation(String notation) {
  }

  default void fromPdnNotation(String pos) {
    throw new NotImplementedException("fromPdnNotation not implemented");
  }

  @DynamoDBIgnore
  default String getHNotation() {
    return Utils.alph.get(getH()) + (getDim() - getV());
  }

  default void setHNotation(String notation) {
  }

  default void fromHNotation(String pos) {
    setH(Utils.alph.indexOf(String.valueOf(pos.charAt(0))));
    setV(getDim() - Integer.valueOf(String.valueOf(pos.charAt(1))));
  }
}
