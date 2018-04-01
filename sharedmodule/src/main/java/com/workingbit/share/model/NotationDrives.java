package com.workingbit.share.model;

import com.workingbit.share.domain.DeepClone;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.workingbit.share.util.Utils.listToPdn;

/**
 * Created by Aleksey Popryaduhin on 10:12 04/10/2017.
 */
public class NotationDrives extends LinkedList<NotationDrive> implements ToPdn, DeepClone {

  public NotationDrives() {
    this(false);
  }

  private NotationDrives(boolean hasRoot) {
    if (hasRoot) {
      NotationDrive root = new NotationDrive();
      root.setRoot(hasRoot);
      add(root);
    }
  }

  public String print(String prefix) {
    return stream()
        .map(notationStroke -> notationStroke.print(prefix + "\t"))
        .collect(Collectors.joining("\n"));
  }

  public String toPdn() {
    return listToPdn(new ArrayList<>(this));
  }

  public static NotationDrives createWithoutRoot() {
    return new NotationDrives(false);
  }

  public static NotationDrives createWithRoot() {
    return new NotationDrives(true);
  }

  public static class Builder {

    private NotationDrives drives;

    private Builder() {
      drives = new NotationDrives(false);
    }

    public static NotationDrives.Builder getInstance() {
      return new NotationDrives.Builder();
    }

    public NotationDrives.Builder add(NotationDrive drive) {
      drives.add(drive);
      return this;
    }

    public NotationDrives build() {
      return drives;
    }

    public NotationDrives.Builder addAll(List<NotationDrive> drives) {
      this.drives.addAll(drives);
      return this;
    }
  }
}
