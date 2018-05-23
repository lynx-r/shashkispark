package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedJson;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import com.workingbit.share.domain.DeepClone;
import com.workingbit.share.domain.impl.Draught;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Aleksey Popryaduhin on 10:12 04/10/2017.
 */
@Getter
@Setter
public class NotationFen implements DeepClone {

  @Nullable
  private String turn;
  private boolean blackTurn;
  @DynamoDBTypeConvertedJson(targetType = Sequence.class)
  private Sequence black;
  @DynamoDBTypeConvertedJson(targetType = Sequence.class)
  private Sequence white;

  @DynamoDBTyped(value = DynamoDBMapperFieldModel.DynamoDBAttributeType.M)
  @DynamoDBAttribute(attributeName = "userId")
  private DomainId boardId;

  public NotationFen() {
    white = new Sequence();
    black = new Sequence();
  }

  public int getSize() {
    return Math.min(white.getSquares().size(), black.getSquares().size());
  }

  @SuppressWarnings("unused")
  public void setSize(int size) {
  }

  @NotNull
  public String getAsString() {
    return "[FEN \"" + turn + ':' +
        "W" + white + ':' +
        "B" + black + "\"]";
  }

  @SuppressWarnings("unused")
  public void setAsString(String asString) {
  }

  public void setTurn(@Nullable String turn) {
    this.turn = turn;
    if (turn != null) {
      blackTurn = turn.equals("B");
    }
  }

  public void setBlackTurn(boolean blackTurn) {
    this.blackTurn = blackTurn;
    turn = blackTurn ? "B" : "W";
  }

  @NotNull
  @Override
  public String toString() {
    return getAsString();
  }

  public void setSequenceFromBoard(@NotNull Map<String, Draught> draughts, boolean black) {
    Sequence sequence = new Sequence();
    for (Draught draught : draughts.values()) {
      Square square = new Square();
      square.setK(draught.isQueen());
      square.setNumber(draught.getNotationNum());
      sequence.add(square);
    }
    if (black) {
      this.black = sequence;
    } else {
      this.white = sequence;
    }
  }

  @Getter
  @Setter
  public static class Sequence {
    private LinkedList<Square> squares;

    Sequence() {
      squares = new LinkedList<>();
    }

    public void add(Square square) {
      squares.add(square);
    }

    @Override
    public String toString() {
      return squares
          .stream()
          .map(Square::toString)
          .collect(Collectors.joining(","));
    }
  }

  @Getter
  @Setter
  public static class Square {
    private boolean k;
    private String number;

    @NotNull
    @Override
    public String toString() {
      return (k ? 'K' : "") + number;
    }
  }
}
