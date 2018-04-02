package com.workingbit.share.model;

import com.workingbit.share.domain.DeepClone;
import com.workingbit.share.util.Utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Aleksey Popryadukhin on 02/04/2018.
 */
public class NotationDrives extends LinkedList<NotationDrive> implements ToPdn, DeepClone {

  public static NotationDrives create() {
    return new NotationDrives();
  }

  public String toPdn() {
    return Utils.listToPdn(new ArrayList<>(this));
  }

  public String variantsToPdn() {
    return Utils.notationDrivesToPdn(this);
  }

  public String print(String prefix) {
    return stream()
        .map(notationStroke -> notationStroke.print(prefix + "\t"))
        .collect(Collectors.joining("\n"));
  }

  public static class Builder {

    private NotationDrives drives;

    private Builder() {
      drives = new NotationDrives();
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
