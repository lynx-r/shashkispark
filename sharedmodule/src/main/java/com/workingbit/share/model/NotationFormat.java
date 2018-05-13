package com.workingbit.share.model;

/**
 * Created by Aleksey Popryadukhin on 30/03/2018.
 */
public interface NotationFormat {
  String asString();

  String asTree(String indent, String tabulation);
}
