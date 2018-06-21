package com.workingbit.share.model;

/**
 * Created by Aleksey Popryadukhin on 30/03/2018.
 */
public interface NotationFormat {
  String asStringAlphaNumeric();

  String asStringNumeric();

  String asStringShort();

  String asTree(String indent, String tabulation);
}
