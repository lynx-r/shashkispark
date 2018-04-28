package com.workingbit.share.model.enumarable;

public enum EnumNotation {
    NUMBER(".", ". "),
    LPAREN("(", " ( "),
    RPAREN(")", " ) "),
    SIMPLE("-", "-"),
    CAPTURE(":", "x"),
    END_GAME_SYMBOL("*", " * ");

    private String simple;
    private String pdn;

    EnumNotation(String simple, String pdn) {
      this.simple = simple;
      this.pdn = pdn;
    }

    public String getSimple() {
      return simple;
    }

    public String getPdn() {
      return pdn;
    }
  }
