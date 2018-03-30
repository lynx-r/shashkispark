package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.workingbit.share.domain.DeepClone;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.workingbit.share.model.NotationDrive.EnumMoveType.CAPTURE;
import static com.workingbit.share.model.NotationDrive.EnumMoveType.SIMPLE;

/**
 * Created by Aleksey Popryaduhin on 21:33 03/10/2017.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NotationMove implements DeepClone, ToPdn {

  private NotationDrive.EnumMoveType type;
  /**
   * Moves like a1 and b2
   */
  private String[] move = new String[100];
  private String boardId;
  private boolean cursor;
  private String moveStrength;

  @DynamoDBIgnore
  public String getNotation() {
    return Stream.of(move)
        .collect(Collectors.joining(type == SIMPLE ? SIMPLE.getType() : CAPTURE.getType()));
  }

  @DynamoDBIgnore
  private String getNotationPdn() {
    return Stream.of(move)
        .collect(Collectors.joining(type == SIMPLE ? SIMPLE.getPdnType() : CAPTURE.getPdnType()));
  }

  @SuppressWarnings("unused")
  public void setNotation(String ignore) {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof NotationMove)) return false;
//    if (!super.equals(o)) return false;
    NotationMove that = (NotationMove) o;
    return Arrays.equals(move, that.move);
  }

  @Override
  public int hashCode() {

    int result = super.hashCode();
    result = 31 * result + Arrays.hashCode(move);
    return result;
  }

  public static NotationMove fromPdn(String stroke) {
    return fromPdn(stroke, null);
  }

  public static NotationMove fromPdn(String stroke, String boardId) {
    NotationMove notationMove = new NotationMove();
    boolean capture = stroke.contains(CAPTURE.getPdnType());
    if (capture) {
      notationMove.setType(CAPTURE);
      notationMove.setMove(stroke.split(CAPTURE.getPdnType()));
    } else {
      notationMove.setType(SIMPLE);
      notationMove.setMove(stroke.split(SIMPLE.getPdnType()));
    }
    notationMove.setBoardId(boardId);
    return notationMove;
  }

  public static NotationMove create(NotationDrive.EnumMoveType type, String boardId, boolean cursor) {
    NotationMove notationMove = new NotationMove();
    notationMove.setType(type);
    notationMove.setBoardId(boardId);
    notationMove.setCursor(cursor);
    return notationMove;
  }

  public String print(String prefix) {
    return new StringBuilder()
        .append(prefix).append(getClass().getSimpleName())
        .append(prefix).append("\t").append("type: ").append(type)
        .append(prefix).append("\t").append("move: ").append(Arrays.toString(move))
        .append(prefix).append("\t").append("cursor: ").append(cursor)
        .append(prefix).append("\t").append("moveStrength: ").append(moveStrength)
        .toString();
  }

  public String toPdn() {
    String stroke = getNotationPdn() + (moveStrength != null ? " " + moveStrength : " ");
    return String.format("%1$-10s", stroke);
  }
}
