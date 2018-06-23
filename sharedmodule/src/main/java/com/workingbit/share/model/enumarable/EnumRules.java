package com.workingbit.share.model.enumarable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.workingbit.share.converter.EnumRulesDeserializer;
import lombok.Getter;

import java.util.Arrays;

/**
 * Created by Aleksey Popryaduhin on 09:32 10/08/2017.
 */
@JsonDeserialize(using = EnumRulesDeserializer.class)
@Getter
public enum EnumRules {
  RUSSIAN(8, 3, "RUSSIAN", 25),
  RUSSIAN_GIVEAWAY(-8, 3, "RUSSIAN_GIVEAWAY", 25),
  INTERNATIONAL(10, 4, "INTERNATIONAL", 20),
  INTERNATIONAL_GIVEAWAY(-10, 4, "INTERNATIONAL_GIVEAWAY", 20);

  /**
   * Размерность где, знак указывает на правила
   */
  private int dimensionRule;
  private int rowsForDraughts;

  private String display;
  private int typeNumber;

  EnumRules(int dimension, int rowsForDraughts, String display, int typeNumber) {
    this.dimensionRule = dimension;
    this.rowsForDraughts = rowsForDraughts;
    this.display = display;
    this.typeNumber = typeNumber;
  }

  public static EnumRules fromTypeNumber(int typeNumber) {
    return Arrays.stream(values())
        .filter(enumRules -> enumRules.getTypeNumber() == typeNumber)
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Type number " + typeNumber + " unrecognized"));
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

  public int getTypeNumber() {
    return typeNumber;
  }
}
