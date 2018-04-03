package com.workingbit.board.util;

public class Path {

  public static final String BOARD_ADD_DRAUGHT = "/board/add-draught";
  /**
   * save changes
   */
  public static final String BOARD = "/board";
  public static final String BOARD_BY_ID = "/board/:id";
  public static final String BOARD_MOVE = "/board/move";
  public static final String BOARD_HIGHLIGHT = "/board/highlight";
  public static final String BOARD_REDO = "/board/redo";
  public static final String BOARD_UNDO = "/board/undo";
  public static final String MAKE_WHITE_STROKE = "/board/make-white-stroke";
  /**
   * save changes and return updated Board in BoardBox
   */
  public static final String BOARD_UPDATE = "/board/update-board";
  public static final String BOARD_LOAD_PREVIEW = "/board/load-board-preview";
}