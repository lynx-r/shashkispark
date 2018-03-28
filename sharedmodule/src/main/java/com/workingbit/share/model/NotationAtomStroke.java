package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.workingbit.share.domain.DeepClone;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.workingbit.share.model.NotationAtomStroke.EnumStrokeType.CAPTURE;
import static com.workingbit.share.model.NotationAtomStroke.EnumStrokeType.SIMPLE;

/**
 * Created by Aleksey Popryaduhin on 21:33 03/10/2017.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NotationAtomStroke implements DeepClone {

  private EnumStrokeType type;
  private List<String> strokes = new ArrayList<>();
  private String boardId;
  private boolean cursor;

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

  public enum EnumStrokeType {
    SIMPLE("-"),
    CAPTURE(":");

    private String type;

    EnumStrokeType(String type) {
      this.type = type;
    }

    public String getType() {
      return type;
    }
  }
}
