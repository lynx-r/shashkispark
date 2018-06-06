package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.domain.impl.BoardBox;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.map.AbstractOrderedMapDecorator;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JsonTypeName("BoardBoxes")
public class BoardBoxes extends AbstractOrderedMapDecorator<String, BoardBox> implements Payload {
  private ListOrderedMap<String, BoardBox> boardBoxes;

  public BoardBoxes() {
    this.boardBoxes = new ListOrderedMap<>();
  }

  public BoardBoxes(List<BoardBox> boardBoxList) {
    this();
    boardBoxList.forEach(this::push);
  }

  public void push(@NotNull BoardBox boardBox) {
    super.put(String.valueOf(boardBox.getIdInArticle()), boardBox);
  }

  public ListOrderedMap<String, BoardBox> getBoardBoxes() {
    return boardBoxes;
  }

  public void setBoardBoxes(ListOrderedMap<String, BoardBox> boardBoxes) {
    this.boardBoxes = boardBoxes;
  }

  @Override
  protected OrderedMap<String, BoardBox> decorated() {
    return boardBoxes;
  }
}
