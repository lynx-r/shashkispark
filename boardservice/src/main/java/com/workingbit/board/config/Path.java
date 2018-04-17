package com.workingbit.board.config;

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
  public static final String BOARD_SWITCH = "/board/switch";
  public static final String BOARD_VIEW_BRANCH = "/board/view-branch";
  public static final String BOARD_FORK = "/board/fork";
  public static final String CHANGE_TURN = "/board/change-turn";
  /**
   * save changes and return updated Board in BoardBox
   */
  public static final String BOARD_LOAD_PREVIEW = "/board/load-board-preview";
}