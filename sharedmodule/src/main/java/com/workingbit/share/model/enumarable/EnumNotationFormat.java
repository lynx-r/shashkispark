package com.workingbit.share.model.enumarable;

public enum EnumNotationFormat {
  NUMERIC("Числовая нотация"),
  ALPHANUMERIC("Число-буквенная нотация"),
  SHORT("Сокращенная нотация"),
  ;

  private String display;

  EnumNotationFormat(String display) {
    this.display = display;
  }

  public String getDisplay() {
    return display;
  }
}
