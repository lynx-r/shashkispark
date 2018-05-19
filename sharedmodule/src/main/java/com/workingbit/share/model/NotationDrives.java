package com.workingbit.share.model;

import com.workingbit.share.domain.DeepClone;
import com.workingbit.share.model.enumarable.EnumNotationFormat;
import com.workingbit.share.util.Utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Aleksey Popryadukhin on 02/04/2018.
 */
public class NotationDrives extends LinkedList<NotationDrive> implements NotationFormat, DeepClone {

  public NotationDrives() {
  }

  public NotationDrives(List<NotationDrive> notationDrives) {
    addAll(notationDrives);
  }

  public static NotationDrives create() {
    return new NotationDrives();
  }

  @Override
  public String asString() {
    return Utils.listToPdn(new ArrayList<>(this));
  }

  @Override
  public String asTree(String indent, String tabulation) {
    return Utils.listToTreePdn(new ArrayList<>(this), indent, tabulation);
  }

  public String variantsToPdn() {
    return Utils.notationDrivesToPdn(this);
  }

  public String variantsToTreePdn(String indent, String tabulation) {
    return Utils.notationDrivesToTreePdn(this, indent, tabulation);
  }

  public String print(String prefix) {
    return stream()
        .map(notationStroke -> notationStroke.print(prefix + "\t"))
        .collect(Collectors.joining("\n"));
  }

  public void setNotationFormat(EnumNotationFormat format) {
    forEach(notationDrive -> notationDrive.setNotationFormat(format));
  }

  public void setDimension(int dimension) {
    forEach(notationDrive -> notationDrive.setBoardDimension(dimension));
  }

  public void setIdInVariants(int idInVariants) {
    forEach(tn -> tn.setIdInVariants(idInVariants));
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
