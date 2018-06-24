package com.workingbit.share.model.enumarable;

import java.util.Arrays;

public enum EnumNotationFormat {
  NUMERIC("Числовая нотация", "N"),
  ALPHANUMERIC("Число-буквенная нотация", "A"),
  SHORT("Сокращенная нотация", "S"),
  ;

  private String display;
  private String type;

  EnumNotationFormat(String display, String type) {
    this.display = display;
    this.type = type;
  }

  public static EnumNotationFormat fromType(String type) {
    return Arrays.stream(values())
        .filter(format -> format.getType().equals(type))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Format " + type + " unrecognized"));
  }

  public String getDisplay() {
    return display;
  }

  public String getType() {
    return type;
  }
}
