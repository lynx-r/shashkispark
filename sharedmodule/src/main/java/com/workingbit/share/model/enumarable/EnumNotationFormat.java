package com.workingbit.share.model.enumarable;

public enum EnumNotationFormat {
  DIGITAL("Числовой"),
  ALPHANUMERIC("Число-буквенный");

  private String display;

  EnumNotationFormat(String display) {
    this.display = display;
  }

  public String getDisplay() {
    return display;
  }
}
