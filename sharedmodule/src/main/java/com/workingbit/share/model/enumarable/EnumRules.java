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
//@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum EnumRules {
  RUSSIAN(8, 3, "RUSSIAN"),
  RUSSIAN_GIVEAWAY(-8, 3, "RUSSIAN_GIVEAWAY"),
  INTERNATIONAL(10, 4, "INTERNATIONAL"),
  INTERNATIONAL_GIVEAWAY(-10, 4, "INTERNATIONAL_GIVEAWAY");

///  @JsonSerialize(using = ToStringSerializer.class)
//  @JsonValue
  private int dimension;
//  @JsonSerialize(using = ToStringSerializer.class)
//  @JsonValue
  private int rowsForDraughts;

  private String display;

  EnumRules(int dimension, int rowsForDraughts, String display) {
    this.dimension = dimension;
    this.rowsForDraughts = rowsForDraughts;
    this.display = display;
  }

  @Override
  public String toString() {
    return name();
  }

  //  @JsonCreator()
//  public EnumRules fromJson(Map<String, Object> data) {
//    this.dimension = dimension;
//    this.rowsForDraughts = rowsForDraughts;
//    return EnumRules.RUSSIAN;
//  }

//  @JsonCreator
//  public EnumRules fromObject(Map<String, Object> data) {
//    System.out.println(data);
//    return RUSSIAN;
//  }


  @JsonIgnore
  public int getNumRowsForDraughts() {
    return rowsForDraughts;
  }

  @JsonIgnore
  public int getDimensionAbs() {
    return Math.abs(dimension);
  }

  @JsonIgnore
  public int getDraughtsCount() {
    return dimension / 2 * rowsForDraughts;
  }
}
