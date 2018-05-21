package com.workingbit.share.model;

import com.workingbit.share.domain.impl.Board;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aleksey Popryadukhin on 21/05/2018.
 */
public class Boards {

  private Map<String, Board> boards;

  public Boards() {
    boards = new HashMap<>();
  }

  public void put(String key, Board board) {
    boards.put(key, board);
  }

  public void putAll(Map<String, Board> boards) {
    this.boards.putAll(boards);
  }

  public int size() {
    return boards.size();
  }
}
