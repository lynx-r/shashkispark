package com.workingbit.share.model;

import com.workingbit.share.domain.DeepClone;
import com.workingbit.share.model.enumarable.EnumNotationFormat;
import com.workingbit.share.util.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Created by Aleksey Popryaduhin on 10:12 04/10/2017.
 */
public class NotationMoves extends LinkedList<NotationMove> implements NotationFormat, DeepClone {

  String print(String prefix) {
    return stream()
        .map(notationMove -> notationMove.print(prefix + "\t"))
        .collect(Collectors.joining("\n"));
  }

  @Nullable
  NotationSimpleMove getLastMove() {
    LinkedList<NotationSimpleMove> move = getLast().getMove();
    if (!move.isEmpty()) {
      return move.getLast();
    }
    return null;
  }

  void resetCursors() {
    forEach(NotationMove::resetCursor);
  }

  public String asString() {
    return Utils.listToPdn(new ArrayList<>(this));
  }

  @Override
  public String asTree(String indent, String tabulation) {
    return asString();
  }

  void setNotationFormat(EnumNotationFormat format) {
    forEach(move -> move.setNotationFormat(format));
  }

  void setBoardDimension(int boardDimension) {
    forEach(move -> move.setBoardDimension(boardDimension));
  }
}
