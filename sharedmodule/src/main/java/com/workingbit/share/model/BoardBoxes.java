package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.domain.impl.BoardBox;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.map.ListOrderedMap;

import java.util.ArrayList;
import java.util.List;

@JsonTypeName("BoardBoxes")
@Getter
@Setter
public class BoardBoxes implements Payload {
  private ListOrderedMap<String, BoardBox> boardBoxes;

  public BoardBoxes() {
    this.boardBoxes = new ListOrderedMap<>();
  }

  public static BoardBoxes create(ListOrderedMap<String, BoardBox> collection) {
    BoardBoxes boardBoxes = new BoardBoxes();
    boardBoxes.putAll(collection);
    return boardBoxes;
  }

  public void push(BoardBox boardBox) {
    boardBoxes.put(String.valueOf(boardBoxes.size() + 1), boardBox);
  }

  public void order() {
    List<BoardBox> list = new ArrayList<>(boardBoxes.valueList());
    // sort desc by date
    list.sort((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt()));
    ListOrderedMap<String, BoardBox> temp = new ListOrderedMap<>();
    for (int i = 0; i < list.size(); i++) {
      temp.put(String.valueOf(list.size() - i), list.get(i));
    }
    boardBoxes.clear();
    boardBoxes.putAll(temp);
  }

  public void put(String key, BoardBox boardBox) {
    boardBoxes.put(key, boardBox);
  }

  public void putAll(ListOrderedMap<String, BoardBox> boardBoxes) {
    this.boardBoxes.putAll(boardBoxes);
  }

  public int size() {
    return this.boardBoxes.size();
  }
}
