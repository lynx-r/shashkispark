package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.domain.DeepClone;
import com.workingbit.share.model.enumarable.EnumNotationFormat;
import com.workingbit.share.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Aleksey Popryadukhin on 02/04/2018.
 */
//@JsonDeserialize(using = NotationDrivesDeserializer.class)
public class NotationDrives extends LinkedList<NotationDrive> implements NotationFormat, DeepClone {

  public NotationDrives() {
  }

  public NotationDrives(@NotNull List<NotationDrive> notationDrives) {
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

  @DynamoDBIgnore
  @JsonIgnore
  public Optional<NotationDrive> getCurrentVariant(Integer currentIndex) {
    return getCurrentVariantStream(currentIndex).findFirst();
  }

  @DynamoDBIgnore
  @JsonIgnore
  private Stream<NotationDrive> getCurrentVariantStream(Integer currentIndex) {
    return get(currentIndex)
        .getVariants()
        .stream()
        .filter(NotationDrive::isCurrent);
  }

  @DynamoDBIgnore
  @JsonIgnore
  public Optional<NotationDrive> getPreviousVariant() {
    return stream()
        .filter(NotationDrive::isCurrent)
        .flatMap(notationDrive -> notationDrive.getVariants().stream())
        .filter(NotationDrive::isPrevious)
        .findFirst();
  }

  @JsonIgnore
  @DynamoDBIgnore
  public boolean isEmpty() {
    return super.isEmpty()
        || size() == 1
        && getLast().getVariants().isEmpty();
  }

  @JsonIgnore
  @DynamoDBIgnore
  public Optional<DomainId> getLastNotationBoardId() {
    NotationDrive notationLast = getLast();
    if (notationLast.isRoot()) {
      NotationMoves moves = notationLast.getVariants()
          .getLast()
          .getMoves();
      if (!moves.isEmpty()) {
        return Optional.of(moves.getFirst()
            .getBoardId());
      }
      return Optional.empty();
    }
    NotationMoves moves = notationLast.getMoves();
    return Optional.of(moves.getLast().getBoardId());
  }

  @JsonIgnore
  @DynamoDBIgnore
  public Optional<DomainId> getLastNotationBoardIdInVariants(Integer currentIndex) {
    NotationDrive notationLast = getLast();
//    if (notationLast.isRoot()) {
//      return notationLast.getVariants()
//          .stream()
//          .filter(NotationDrive::isCurrent)
//          .flatMap(notationDrive -> notationDrive.getVariants().stream())
//          .filter(NotationDrive::isSelected)
//          .map(NotationDrive::getMoves)
//          .map(NotationMoves::getLastMove)
//          .filter(Objects::nonNull)
//          .map(NotationSimpleMove::getBoardId)
//          .findFirst();
//    }
    return Optional.ofNullable(getCurrentVariantStream(currentIndex)
        .map(NotationDrive::getVariants)
        .map(LinkedList::getLast)
        .map(NotationDrive::getMoves)
        .map(NotationMoves::getLast)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null)
        .getBoardId());
  }

  @JsonIgnore
  @DynamoDBIgnore
  public Optional<NotationMove> getLastMove() {
    NotationDrive last = getLast();
    if (!last.getMoves().isEmpty()) {
      return Optional.of(last.getMoves().getLast());
    }
    return Optional.empty();
  }

  public void setLastMoveCursor() {
    if (isEmpty()) {
      return;
    }
    forEach(nd -> nd.getMoves().resetCursors());
    NotationMoves moves = getLast().getMoves();
    if (!moves.isEmpty()) {
      NotationSimpleMove lastMove = moves.getLastMove();
      if (lastMove != null) {
        lastMove.setCursor(true);
      }
    }
  }

  public static class Builder {

    private NotationDrives drives;

    private Builder() {
      drives = new NotationDrives();
    }

    public static NotationDrives.Builder getInstance() {
      return new NotationDrives.Builder();
    }

    @NotNull
    public NotationDrives.Builder add(NotationDrive drive) {
      drives.add(drive);
      return this;
    }

    public NotationDrives build() {
      return drives;
    }

    @NotNull
    public NotationDrives.Builder addAll(@NotNull List<NotationDrive> drives) {
      this.drives.addAll(drives);
      return this;
    }
  }
}
