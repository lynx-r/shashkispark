package com.workingbit.share.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static com.workingbit.share.util.Utils.listToPdn;

/**
 * Created by Aleksey Popryaduhin on 10:12 04/10/2017.
 */
public class NotationDrives extends LinkedList<NotationDrive> implements ToPdn {

  public String print(String prefix) {
    return stream()
        .map(notationStroke -> prefix + notationStroke.print(prefix + "\t"))
        .collect(Collectors.joining("\n"));
  }

  public String toPdn() {
    return listToPdn(new ArrayList<>(this));
  }
}
