package com.workingbit.share.domain.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.common.DBConstants;
import com.workingbit.share.converter.BoardIdNotationConverter;
import com.workingbit.share.converter.DraughtMapConverter;
import com.workingbit.share.converter.LocalDateTimeConverter;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.model.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by Aleksey Popryaduhin on 23:21 21/09/2017.
 */
@JsonTypeName("board")
@Getter
@Setter
@ToString
@DynamoDBTable(tableName = DBConstants.BOARD_TABLE)
public class Board extends BaseDomain implements Payload {

  @DynamoDBHashKey(attributeName = "id")
  private String id;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBRangeKey(attributeName = "createdAt")
  private LocalDateTime createdAt;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBAttribute(attributeName = "updatedAt")
  private LocalDateTime updatedAt;

  @DynamoDBAttribute(attributeName = "boardBoxId")
  private String boardBoxId;

  /**
   * Next boards map. Key next square notation, value board id
   */
  @DynamoDBTypeConverted(converter = BoardIdNotationConverter.class)
  @DynamoDBAttribute(attributeName = "previousBoards")
  private LinkedList<BoardIdNotation> previousBoards = new LinkedList<>();

  @DynamoDBTypeConverted(converter = BoardIdNotationConverter.class)
  @DynamoDBAttribute(attributeName = "nextBoards")
  private LinkedList<BoardIdNotation> nextBoards = new LinkedList<>();

  /**
   * Black draughts associated with owner square
   */
  @DynamoDBTypeConverted(converter = DraughtMapConverter.class)
  @DynamoDBAttribute(attributeName = "blackDraughts")
  private Map<String, Draught> blackDraughts = new HashMap<>();

  @DynamoDBTypeConverted(converter = DraughtMapConverter.class)
  @DynamoDBAttribute(attributeName = "whiteDraughts")
  private Map<String, Draught> whiteDraughts = new HashMap<>();

  /**
   * Currently selected square
   */
  @DynamoDBTypeConvertedJson(targetType = Square.class)
  @DynamoDBAttribute(attributeName = "selectedSquare")
  private Square selectedSquare;

  /**
   * Next move for draught
   */
  @DynamoDBTypeConvertedJson(targetType = Square.class)
  @DynamoDBAttribute(attributeName = "nextSquare")
  private Square nextSquare;

  @DynamoDBTypeConvertedJson(targetType = Square.class)
  @DynamoDBAttribute(attributeName = "previousSquare")
  private Square previousSquare;

  /**
   * Squares for API
   */
  @DynamoDBIgnore
  private List<Square> squares = new ArrayList<>();

  /**
   * Squares without nulls
   */
  @DynamoDBIgnore
  @JsonIgnore
  private List<Square> assignedSquares = new ArrayList<>();

  /**
   * Is player on the black side?
   */
  @DynamoDBAttribute(attributeName = "black")
  private boolean black;

  @DynamoDBAttribute(attributeName = "blackTurn")
  private boolean blackTurn;

  /**
   * Count of completed moves like 1. a1-a2 e2-e3 and 2. f1-f2 c2-c3
   */
  @DynamoDBAttribute(attributeName = "driveCount")
  private int driveCount;

  @DynamoDBTypeConvertedEnum
  @DynamoDBAttribute(attributeName = "rules")
  private EnumRules rules;

//  @JsonIgnore
//  @DynamoDBTypeConvertedJson(targetType = NotationHistory.class)
//  @DynamoDBAttribute(attributeName = "notationHistory")
//  private NotationHistory notationHistory;

//  public Board() {
//    notationHistory = NotationHistory.createWithRoot();
//  }


  public Board() {
  }

  public Board(boolean black, EnumRules rules) {
    this();
    this.black = black;
  }

  public String popPreviousBoard() {
    return previousBoards.isEmpty() ? null : previousBoards.pop().getBoardId();
  }

  public void pushPreviousBoard(String boardId, String anchorNotation, String possibleNotation) {
    this.previousBoards.push(new BoardIdNotation(boardId, anchorNotation, possibleNotation));
  }

  public String popNextBoard() {
    return nextBoards.isEmpty() ? null : nextBoards.pop().getBoardId();
  }

  public void pushNextBoard(String boardId, String anchorNotation, String possibleNotation) {
    nextBoards.push(new BoardIdNotation(boardId, anchorNotation, possibleNotation));
  }

  public void addBlackDraughts(String notation, Draught draught) {
    blackDraughts.put(notation, draught);
  }

  public void addWhiteDraughts(String notation, Draught draught) {
    whiteDraughts.put(notation, draught);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Board board = (Board) o;
    return Objects.equals(id, board.id);
  }

  @Override
  public int hashCode() {

    return Objects.hash(id);
  }
}
