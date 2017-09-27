package com.workingbit.share.model;

/**
 * Created by Aleksey Popryaduhin on 12:52 24/09/2017.
 */
public class BoardIdNotation {
  private String boardId;
  private String notation;

  public BoardIdNotation() {
  }

  public BoardIdNotation(String boardId, String notation) {
    this.boardId = boardId;
    this.notation = notation;
  }

  public String getBoardId() {
    return boardId;
  }

  public void setBoardId(String boardId) {
    this.boardId = boardId;
  }

  public String getNotation() {
    return notation;
  }

  public void setNotation(String notation) {
    this.notation = notation;
  }
}
