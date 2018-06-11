package com.workingbit.share.model.enumarable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.workingbit.share.converter.EnumRulesDeserializer;
import lombok.Getter;

/**
 * Created by Aleksey Popryaduhin on 09:32 10/08/2017.
 */
@JsonDeserialize(using = EnumRulesDeserializer.class)
@Getter
public enum EnumRules {
  RUSSIAN(8, 3, "RUSSIAN"),
  RUSSIAN_GIVEAWAY(-8, 3, "RUSSIAN_GIVEAWAY"),
  INTERNATIONAL(10, 4, "INTERNATIONAL"),
  INTERNATIONAL_GIVEAWAY(-10, 4, "INTERNATIONAL_GIVEAWAY");

  /**
   * Размерность где, знак указывает на правила
   */
  private int dimensionRule;
  private int rowsForDraughts;

  private String display;

  EnumRules(int dimension, int rowsForDraughts, String display) {
    this.dimensionRule = dimension;
    this.rowsForDraughts = rowsForDraughts;
    this.display = display;
  }

  @Override
  public String toString() {
    return name();
  }

  @JsonIgnore
  public int getNumRowsForDraughts() {
    return rowsForDraughts;
  }

  @JsonIgnore
  public int getDimension() {
    return Math.abs(dimensionRule);
  }

  @JsonIgnore
  public int getDraughtsCount() {
    return dimensionRule / 2 * rowsForDraughts;
  }
}
