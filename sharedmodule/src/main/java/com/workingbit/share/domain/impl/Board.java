package com.workingbit.share.domain.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.model.Payload;
import com.workingbit.share.model.enumarable.EnumRules;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

/**
 * Created by Aleksey Popryaduhin on 23:21 21/09/2017.
 */
@JsonTypeName(Board.CLASS_NAME)
@Getter
@Setter
@Document(collection = Board.CLASS_NAME)
public class Board extends BaseDomain implements Payload {

  public static final String CLASS_NAME = "Board";

  private BoardBox boardBox;

  /**
   * Black draughts associated with owner square
   */
  private Map<String, Draught> blackDraughts = new HashMap<>();

  private Map<String, Draught> whiteDraughts = new HashMap<>();

  /**
   * Currently selected square
   */
  private Square selectedSquare;

  /**
   * Next move for draught
   */
  private Square nextSquare;

  private Square previousSquare;

  /**
   * Squares for API
   */
  @Transient
  private List<Square> squares = new ArrayList<>();

  /**
   * Squares without nulls
   */
  @Transient
  @JsonIgnore
  private List<Square> assignedSquares = new ArrayList<>();

  /**
   * Is player on the black side?
   */
  private boolean black;

  private boolean blackTurn;

  /**
   * Count of completed moves like 1. a1-a2 e2-e3 and 2. f1-f2 c2-c3
   */
  private int driveCount;

  private EnumRules rules;

  public Board() {
  }

  public Board(boolean black, EnumRules rules) {
    this();
    this.black = black;
    this.rules = rules;
  }

  public void addBlackDraughts(String notation, Draught draught) {
    blackDraughts.put(notation, draught);
  }

  public void addWhiteDraughts(String notation, Draught draught) {
    whiteDraughts.put(notation, draught);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Board board = (Board) o;
    return Objects.equals(getId(), board.getId());
  }

  @Override
  public int hashCode() {

    return Objects.hash(getId());
  }
}
