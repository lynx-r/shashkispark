package com.workingbit.share.model;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by Aleksey Popryaduhin on 10:12 04/10/2017.
 */
public class NotationMoves extends LinkedList<NotationMove> {

  public String print(String prefix) {
    return stream()
        .map(notationStroke -> prefix + notationStroke.print(prefix + "\t"))
        .collect(Collectors.joining("\n"));
  }

  public String toPdn() {
    AtomicInteger i = new AtomicInteger();
    return stream()
        .map(s -> {
          String pdn = s.toPdn();
          i.getAndIncrement();
          if (i.get() > 3) {
            pdn = pdn.trim();
            pdn += "\n";
            i.set(0);
          }
          return pdn;
        }).collect(Collectors.joining());
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
