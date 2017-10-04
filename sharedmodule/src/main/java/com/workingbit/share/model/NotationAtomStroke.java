package com.workingbit.share.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Aleksey Popryaduhin on 21:33 03/10/2017.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NotationAtomStroke {

  private EnumStrokeType type;
  private List<String> strokes = new ArrayList<>();
  private String boardId;
  private boolean cursor;

  private Set<NotationStrokes> alternative = new HashSet<>();

  public NotationAtomStroke(EnumStrokeType type, List<String> strokes, String boardId, boolean cursor) {
    this.type = type;
    this.strokes = strokes;
    this.boardId = boardId;
    this.cursor = cursor;
  }

  public enum EnumStrokeType {
    SIMPLE,
    CAPTURE
  }
}
