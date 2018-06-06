package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.domain.impl.BoardBox;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonTypeName("BoardBoxes")
@Getter
@Setter
public class BoardBoxes implements Payload {

  @JsonProperty
  private ListOrderedMap<String, BoardBox> boardBoxes;

  public BoardBoxes() {
    this.boardBoxes = new ListOrderedMap<>();
  }

  public BoardBoxes(Collection<BoardBox> boardBoxList) {
    this();
    boardBoxList.forEach(this::push);
  }

  public void push(@NotNull BoardBox boardBox) {
    boardBoxes.put(String.valueOf(boardBox.getIdInArticle()), boardBox);
  }

  public Set<Map.Entry<String, BoardBox>> entrySet() {
    return boardBoxes.entrySet();
  }

  public List<BoardBox> valueList() {
    return boardBoxes.valueList();
  }

}
