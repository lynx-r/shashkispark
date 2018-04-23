package com.workingbit.share.model;

import com.workingbit.share.domain.DeepClone;
import com.workingbit.share.util.Utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Created by Aleksey Popryaduhin on 10:12 04/10/2017.
 */
public class NotationMoves extends LinkedList<NotationMove> implements ToPdn, DeepClone {

  public String print(String prefix) {
    return stream()
        .map(notationMove -> notationMove.print(prefix + "\t"))
        .collect(Collectors.joining("\n"));
  }

  public String toPdn() {
    return Utils.listToPdn(new ArrayList<>(this));
  }

  public static class Builder {

    private NotationMoves moves;

    private Builder() {
      moves = new NotationMoves();
    }

    public static Builder getInstance() {
      return new Builder();
    }

    public Builder add(NotationMove move) {
      moves.add(move);
      return this;
    }

    public NotationMoves build() {
      return moves;
    }
  }
}
