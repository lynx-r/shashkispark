package com.workingbit.share.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

  public enum EnumStrokeType {
    SIMPLE,
    CAPTURE
  }
}
