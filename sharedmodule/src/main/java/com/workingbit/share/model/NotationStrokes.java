package com.workingbit.share.model;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by Aleksey Popryaduhin on 10:12 04/10/2017.
 */
public class NotationStrokes extends LinkedList<NotationStroke> {

  public String print(String prefix) {
    return stream()
        .map(notationStroke -> prefix + notationStroke.print(prefix + "\t"))
        .collect(Collectors.joining("\n"));
  }

  public String toPdn() {
    AtomicInteger i = new AtomicInteger();
    return stream()
        .map(s -> {
          String pdn = s.toPdn(); /*String.format("%1$-25s", s.toPdn());*/
          i.getAndIncrement();
          if (i.get() > 3) {
            pdn += "\n";
            i.set(0);
          }
          return pdn;
        }).collect(Collectors.joining());
  }
}
