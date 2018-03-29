package com.workingbit.share.model;

import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Created by Aleksey Popryaduhin on 10:12 04/10/2017.
 */
public class NotationStrokes extends LinkedList<NotationStroke> {

  public String toString(String prefix) {
    return stream()
        .map(notationStroke -> prefix + notationStroke.toString(prefix + "\t"))
        .collect(Collectors.joining("\n"));
  }
}
