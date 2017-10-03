package com.workingbit.share.domain.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.converter.BoardIdNotationConverter;
import com.workingbit.share.common.DBConstants;
import com.workingbit.share.converter.DraughtMapConverter;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.model.BoardIdNotation;
import com.workingbit.share.model.EnumRules;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Created by Aleksey Popryaduhin on 23:21 21/09/2017.
 */
@NoArgsConstructor
@Data
@DynamoDBTable(tableName = DBConstants.BOARD_TABLE)
public class Board implements BaseDomain {

  @DynamoDBHashKey(attributeName = "id")
  private String id;

  @DynamoDBRangeKey(attributeName = "createdAt")
  private Date createdAt;

  @JsonIgnore
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

  @DynamoDBTypeConvertedEnum
  @DynamoDBAttribute(attributeName = "rules")
  private EnumRules rules;

  /**
   * Current move cursor
   */
  @JsonIgnore
  @DynamoDBAttribute(attributeName = "cursor")
  private boolean cursor;

  @DynamoDBAttribute(attributeName = "blackTurn")
  private boolean blackTurn;

  @DynamoDBAttribute(attributeName = "strokeNumber")
  private int strokeNumber;

  @DynamoDBAttribute(attributeName = "stroke")
  private String stroke;

  @DynamoDBAttribute(attributeName = "notation")
  private String notation;

  public Board(boolean black, EnumRules rules) {
    this.black = black;
    this.rules = rules;
  }

  public String popPreviousBoard() {
    return previousBoards.isEmpty() ? null : previousBoards.pop().getBoardId();
  }

  public void pushPreviousBoard(String boardId, String notation) {
    this.previousBoards.push(new BoardIdNotation(boardId, notation));
  }

  public String popNextBoard() {
    return nextBoards.isEmpty() ? null : nextBoards.pop().getBoardId();
  }

  public void pushNextBoard(String boardId, String notation) {
    nextBoards.push(new BoardIdNotation(boardId, notation));
  }

  public void addBlackDraughts(String notation, Draught draught) {
    blackDraughts.put(notation, draught);
  }

  public void addWhiteDraughts(String notation, Draught draught) {
    whiteDraughts.put(notation, draught);
  }

  public void setSelectedSquare(Square square) {
    this.selectedSquare = square;
    if (square != null) {
      this.notation = square.getNotation();
    }
  }
}
