package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.domain.impl.BoardBox;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.map.ListOrderedMap;

import java.util.List;

@JsonTypeName("BoardBoxes")
@Getter
@Setter
public class BoardBoxes implements Payload {
  private ListOrderedMap<String, BoardBox> boardBoxes;

  public BoardBoxes() {
    this.boardBoxes = new ListOrderedMap<>();
  }

  public BoardBoxes(List<BoardBox> boardBoxList) {
    this();
    boardBoxList.forEach(this::push);
  }

  public static BoardBoxes create(ListOrderedMap<String, BoardBox> collection) {
    BoardBoxes boardBoxes = new BoardBoxes();
    boardBoxes.putAll(collection);
    return boardBoxes;
  }

  public void push(BoardBox boardBox) {
    boardBoxes.put(String.valueOf(boardBox.getIdInArticle()), boardBox);
  }

  public void putAll(ListOrderedMap<String, BoardBox> boardBoxes) {
    this.boardBoxes.putAll(boardBoxes);
  }

  public int size() {
    return this.boardBoxes.size();
  }
}
