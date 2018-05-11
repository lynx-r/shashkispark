package com.workingbit.share.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Aleksey Popryadukhin on 04/04/2018.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NotationSimpleMove {
  private String notation;
  private DomainId boardId;
  private boolean cursor;

  public NotationSimpleMove(String notation, DomainId boardId) {
    this.notation = notation;
    this.boardId = boardId;
  }
}
