package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.domain.impl.BoardBox;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@JsonTypeName("BoardBoxes")
@AllArgsConstructor
@Getter
@Setter
public class BoardBoxes implements Payload {
  private List<BoardBox> boardBoxes;

  public BoardBoxes() {
    this.boardBoxes = new ArrayList<>();
  }

  public static BoardBoxes create(Collection<BoardBox> collection) {
    BoardBoxes boardBoxes = new BoardBoxes();
    boardBoxes.getBoardBoxes().addAll(collection);
    return boardBoxes;
  }

  public void add(BoardBox boardBox) {
    boardBoxes.add(boardBox);
  }

  public void addAll(Collection<BoardBox> boardBoxes) {
    this.boardBoxes.addAll(boardBoxes);
  }

  public int size() {
    return this.boardBoxes.size();
  }
}
