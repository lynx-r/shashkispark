package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.workingbit.share.domain.DeepClone;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.workingbit.share.model.NotationStroke.EnumStrokeType.CAPTURE;
import static com.workingbit.share.model.NotationStroke.EnumStrokeType.SIMPLE;

/**
 * Created by Aleksey Popryaduhin on 21:33 03/10/2017.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NotationAtomStroke implements DeepClone {

  private NotationStroke.EnumStrokeType type;
  private List<String> strokes = new ArrayList<>();
  private String boardId;
  private boolean cursor;
  private String moveStrength;

  @DynamoDBIgnore
  public String getNotation() {
    return strokes
        .stream()
        .collect(Collectors.joining(type == SIMPLE ? SIMPLE.getType() : CAPTURE.getType()));
  }

  public void setNotation(String notation) {
  }

  /**
   * WARN equals without comparing with super
   * @param o
   * @return
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof NotationAtomStroke)) return false;
    NotationAtomStroke that = (NotationAtomStroke) o;
    return type == that.type &&
        Objects.equals(strokes, that.strokes) &&
        Objects.equals(boardId, that.boardId);
  }

  @Override
  public int hashCode() {

    return Objects.hash(super.hashCode(), type, strokes, boardId);
  }

  public static NotationAtomStroke fromPdn(String stroke) {
    return fromPdn(stroke, null);
  }

  public static NotationAtomStroke fromPdn(String stroke, String boardId) {
    NotationAtomStroke notationAtomStroke = new NotationAtomStroke();
    boolean capture = stroke.contains(CAPTURE.getPdnType());
    if (capture) {
      notationAtomStroke.setType(CAPTURE);
      notationAtomStroke.setStrokes(Arrays.asList(stroke.split(CAPTURE.getPdnType())));
    } else {
      notationAtomStroke.setType(SIMPLE);
      notationAtomStroke.setStrokes(Arrays.asList(stroke.split(SIMPLE.getPdnType())));
    }
    notationAtomStroke.setBoardId(boardId);
    return notationAtomStroke;
  }

  public static NotationAtomStroke create(NotationStroke.EnumStrokeType type, List<String> strokes, String boardId, boolean cursor) {
    return new NotationAtomStroke(type, strokes, boardId, cursor, null);
  }
}
