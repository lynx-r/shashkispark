package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.domain.DeepClone;
import com.workingbit.share.model.enumarable.EnumNotation;
import com.workingbit.share.model.enumarable.EnumNotationFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.workingbit.share.model.enumarable.EnumNotation.CAPTURE;
import static com.workingbit.share.model.enumarable.EnumNotation.SIMPLE;

/**
 * Moves like e1-b2 or a1:e2
 * <p>
 * Created by Aleksey Popryaduhin on 21:33 03/10/2017.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NotationMove implements DeepClone, NotationFormat {

  private EnumNotation type;
  /**
   * Moves. The first is notation like a1 or b2 the second is boardId
   */
  @NotNull
  private LinkedList<NotationSimpleMove> move = new LinkedList<>();
  private String moveStrength;

  /**
   * Num of squares on a side
   */
  @DynamoDBIgnore
  private int boardDimension;

  @DynamoDBIgnore
  private EnumNotationFormat notationFormat;

  @JsonIgnore
  @DynamoDBIgnore
  public String getNotationInFormat(EnumNotationFormat notationFormat) {
    return getMoveNotations(notationFormat)
        .stream()
        .collect(Collectors.joining(type == SIMPLE ? SIMPLE.getSimple() : CAPTURE.getSimple()));
  }

  @JsonIgnore
  @DynamoDBIgnore
  private String getNotationPdn() {
    return getMoveNotations(EnumNotationFormat.DIGITAL)
        .stream()
        .collect(Collectors.joining(type == SIMPLE ? SIMPLE.getPdn() : CAPTURE.getPdn()));
  }

  @JsonIgnore
  @DynamoDBIgnore
  private List<String> getMoveNotations(EnumNotationFormat format) {
    return move
        .stream()
        .map(move -> move.getNotation())
        .collect(Collectors.toList());
  }

  @JsonIgnore
  @DynamoDBIgnore
  public List<String> getMoveNotationsPdn() {
    return move
        .stream()
        .map(move -> move.getNotation())
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unused")
  public void setNotation(String ignore) {
  }

  public void setNotationFormat(EnumNotationFormat format) {
    this.notationFormat = format;
    move.forEach(m -> m.setFormat(format));
  }

  public void setBoardDimension(int dimension) {
    this.boardDimension = dimension;
    move.forEach(m -> m.setBoardDimension(dimension));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof NotationMove)) return false;
//    if (!super.equals(o)) return false;
    NotationMove that = (NotationMove) o;
    return type == that.type &&
        Objects.equals(move, that.move);
  }

  @Override
  public int hashCode() {

    return Objects.hash(super.hashCode(), type, move);
  }

  @NotNull
  static NotationMove fromPdn(@NotNull String stroke) {
    NotationMove notationMove = new NotationMove();
    checkAndAddMoveForPdn(stroke, notationMove, CAPTURE.getPdn(), SIMPLE.getPdn());
    checkAndAddMoveForPdn(stroke, notationMove, CAPTURE.getSimple(), SIMPLE.getSimple());
    return notationMove;
  }

  private static void checkAndAddMoveForPdn(String stroke, @NotNull NotationMove notationMove, @NotNull String capture, @NotNull String simple) {
    boolean isCapture = stroke.contains(capture);
    if (isCapture) {
      notationMove.setType(CAPTURE);
      notationMove.setMoveKeysForPdn(stroke.split(capture));
    } else {
      notationMove.setType(SIMPLE);
      notationMove.setMoveKeysForPdn(stroke.split(simple));
    }
  }

  @NotNull
  public static NotationMove create(EnumNotation type) {
    NotationMove notationMove = new NotationMove();
    notationMove.setType(type);
    return notationMove;
  }

  @NotNull
  public String print(String prefix) {
    return new StringBuilder()
        .append(prefix).append(getClass().getSimpleName())
        .append(prefix).append("\t").append("type: ").append(type)
        .append(prefix).append("\t").append("move: ").append(move.toString())
//        .append(prefix).append("\t").append("cursor: ").append(cursor)
        .append(prefix).append("\t").append("moveStrength: ").append(moveStrength)
        .toString();
  }

  public String asString() {
    String stroke = getNotationInFormat(notationFormat) + (moveStrength != null ? " " + moveStrength : " ");
    return String.format("%1$-10s", stroke);
  }

  @Override
  public String asTree(String indent, String tabulation) {
    return asString();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("type", type)
        .append("move", move)
//        .append("cursor", cursor)
        .append("moveStrength", moveStrength)
        .toString();
  }

  @JsonIgnore
  @DynamoDBIgnore
  private void setMoveKeysForPdn(String[] moveKeys) {
    move = new LinkedList<>();
    for (String moveKey : moveKeys) {
      move.add(new NotationSimpleMove(moveKey, null));
    }
  }

  @JsonIgnore
  @DynamoDBIgnore
  public Optional<DomainId> getLastMoveBoardId() {
    return Optional.ofNullable(move.getLast().getBoardId());
  }

  public void addMove(String previousNotation, DomainId prevBoardId, String currentNotation, DomainId currentBoardId) {
    move = new LinkedList<>() {{
      add(new NotationSimpleMove(previousNotation, prevBoardId));
      add(new NotationSimpleMove(currentNotation, currentBoardId));
    }};
  }

  public void resetCursor() {
    move.forEach(m -> m.setCursor(false));
  }

  @JsonIgnore
  @DynamoDBIgnore
  public Optional<NotationSimpleMove> getLastMove() {
    if (!move.isEmpty()) {
      return Optional.of(move.getLast());
    }
    return Optional.empty();
  }
}
