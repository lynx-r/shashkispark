package com.workingbit.share.model.enumarable;

public enum EnumNotationFormat {
  DIGITAL("Числовая нотация"),
  ALPHANUMERIC("Число-буквенная нотация");

  private String display;

  EnumNotationFormat(String display) {
    this.display = display;
  }

  public String getDisplay() {
    return display;
  }
}
