package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.domain.DeepClone;
import com.workingbit.share.model.enumarable.EnumNotation;
import com.workingbit.share.model.enumarable.EnumNotationFormat;
import org.apache.commons.lang3.StringUtils;
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
public class NotationMove implements DeepClone, NotationFormat {

  private EnumNotation type;
  /**
   * Moves. The first is notation like a1 or b2 the second is boardId
   */
  @NotNull
  private LinkedList<NotationSimpleMove> move = new LinkedList<>();

  private String moveStrength;

  private DomainId boardId;

  /**
   * Num of squares on a side
   */
  private int boardDimension;

  private EnumNotationFormat notationFormat;

  private boolean cursor;

  /**
   * whether move visible. is used in frontend
   */
  private boolean visible;

  public NotationMove() {
    visible = true;
  }

  NotationMove(@NotNull String stroke, EnumNotationFormat notationFormat, int boardDimension) {
    checkAndAddMoveForPdn(stroke, CAPTURE.getPdn(), SIMPLE.getPdn(), notationFormat);
    checkAndAddMoveForPdn(stroke, CAPTURE.getSimple(), SIMPLE.getSimple(), notationFormat);
    setNotationFormat(notationFormat);
    setMoveFormat(notationFormat);
    setBoardDimension(boardDimension);
  }

  public EnumNotation getType() {
    return type;
  }

  public void setType(EnumNotation type) {
    this.type = type;
  }

  public boolean isCursor() {
    return cursor;
  }

  public void setCursor(boolean cursor) {
    this.cursor = cursor;
  }

  @NotNull
  public LinkedList<NotationSimpleMove> getMove() {
    return move;
  }

  public void setMove(@NotNull LinkedList<NotationSimpleMove> move) {
    this.move = move;
  }

  public String getMoveStrength() {
    return moveStrength;
  }

  public void setMoveStrength(String moveStrength) {
    this.moveStrength = moveStrength;
  }

  public DomainId getBoardId() {
    return boardId;
  }

  public void setBoardId(DomainId boardId) {
    this.boardId = boardId;
  }

  public int getBoardDimension() {
    return boardDimension;
  }

  public EnumNotationFormat getNotationFormat() {
    return notationFormat;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  @DynamoDBIgnore
  public String getNotationAlphaNumeric() {
    return getNotationAsString(EnumNotationFormat.ALPHANUMERIC);
  }

  @JsonIgnore
  @DynamoDBIgnore
  private String getNotationAsString(EnumNotationFormat notationFormat) {
    return getMoveNotations(notationFormat)
        .stream()
        .collect(Collectors.joining(type == SIMPLE ?
            (EnumNotationFormat.SHORT.equals(notationFormat) ? "" : SIMPLE.getSimple())
            : CAPTURE.getSimple()));
  }

  @SuppressWarnings("unused")
  public void setNotation(String ignore) {
  }

  void setNotationFormat(EnumNotationFormat format) {
    this.notationFormat = format;
  }

  private void checkAndAddMoveForPdn(String stroke, @NotNull String capture, @NotNull String simple,
                                     EnumNotationFormat notationFormat) {
    boolean isCapture = stroke.contains(capture);
    if (isCapture) {
      setType(CAPTURE);
      setMoveKeysForPdn(stroke.split(capture));
    } else if (stroke.contains(simple)) {
      setType(SIMPLE);
      switch (notationFormat) {
        case ALPHANUMERIC:
        case NUMERIC:
          setMoveKeysForPdn(stroke.split(simple));
          break;
      }
    } else if (notationFormat.equals(EnumNotationFormat.SHORT)) {
      setType(SIMPLE);
      String endMove = stroke.substring(1);
      String startMove = stroke.substring(0, 1);
      String[] split = startMove.split("");
      List<String> list = new ArrayList<>(List.of(split));
      list.add(endMove);
      setMoveKeysForPdn(list.toArray(new String[0]));
    }
  }

  void setBoardDimension(int dimension) {
    this.boardDimension = dimension;
    move.replaceAll(m -> {
      m.setBoardDimension(dimension);
      return m;
    });
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof NotationMove)) return false;
    NotationMove that = (NotationMove) o;
    return type == that.type &&
        Objects.equals(move, that.move);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), type, move);
  }

  void setMoveFormat(EnumNotationFormat format) {
    move.replaceAll(m -> {
      m.setFormat(format);
      return m;
    });
  }

  @JsonIgnore
  @DynamoDBIgnore
  public List<String> getMoveNotations() {
    return getMoveNotations(notationFormat);
  }

  @JsonIgnore
  @DynamoDBIgnore
  private List<String> getMoveNotations(EnumNotationFormat notationFormat) {
    switch (notationFormat) {
      case ALPHANUMERIC:
      case NUMERIC:
        return move
            .stream()
            .map(NotationSimpleMove::getNotation)
            .collect(Collectors.toList());
      case SHORT:
        List<String> list = new ArrayList<>();
        int size = move.size();
        for (int i = 0; i < size; i++) {
          NotationSimpleMove notationSimpleMove = move.get(i);
          String notation;
          if (i != size - 1) {
            notation = notationSimpleMove.getNotation().replaceAll("\\d+", "");
          } else {
            notation = notationSimpleMove.getNotation();
          }
          list.add(notation);
        }
        return list;
      default:
        throw new RuntimeException("Формат не распознан");
    }
  }

  @NotNull
  public static NotationMove create(EnumNotation type) {
    NotationMove notationMove = new NotationMove();
    notationMove.setType(type);
    return notationMove;
  }

  @NotNull
  String print(String prefix) {
    return prefix + getClass().getSimpleName() +
        prefix + "\t" + "type: " + type +
        prefix + "\t" + "move: " + move.toString();
//        .append(prefix).append("\t").append("cursor: ").append(cursor)
  }

  public String asString(EnumNotationFormat notationFormat) {
    EnumNotationFormat oldNotationFormat = null;
    if (!this.notationFormat.equals(notationFormat)) {
      oldNotationFormat = this.notationFormat;
      setMoveFormat(notationFormat);
    }
    String stroke = getNotationAsString(notationFormat);
    String notation = String.format("%s %s ", stroke, (StringUtils.isNotBlank(moveStrength) ? moveStrength : ""));
    if (!this.notationFormat.equals(notationFormat)) {
      setMoveFormat(oldNotationFormat);
    }
    return notation;
  }

  @Override
  public String asStringAlphaNumeric() {
    return asString(EnumNotationFormat.ALPHANUMERIC);
  }

  @Override
  public String asStringNumeric() {
    return asString(EnumNotationFormat.NUMERIC);
  }

  @Override
  public String asStringShort() {
    return asString(EnumNotationFormat.SHORT);
  }

  @Override
  public String asTree(String indent, String tabulation) {
    return asString(EnumNotationFormat.ALPHANUMERIC);
  }

  @Override
  public String toString() {
    return "NotationMove{" + asString(EnumNotationFormat.ALPHANUMERIC) + "}";
  }

  @JsonIgnore
  @DynamoDBIgnore
  private void setMoveKeysForPdn(String[] moveKeys) {
    move = new LinkedList<>();
    for (String moveKey : moveKeys) {
      move.add(new NotationSimpleMove(moveKey));
    }
  }

  @JsonIgnore
  @DynamoDBIgnore
  DomainId getLastMoveBoardId() {
    return boardId;
  }

  public void addMove(String previousNotation, String currentNotation, DomainId currentBoardId) {
    move = new LinkedList<>(
        List.of(
            new NotationSimpleMove(previousNotation),
            new NotationSimpleMove(currentNotation)
        )
    );
    this.boardId = currentBoardId;
  }

  void resetCursor() {
    setCursor(false);
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
