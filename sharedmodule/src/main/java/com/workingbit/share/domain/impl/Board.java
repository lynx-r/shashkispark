package com.workingbit.share.domain.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.common.BoardIdNotationConverter;
import com.workingbit.share.common.DBConstants;
import com.workingbit.share.common.DraughtMapConverter;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.model.BoardIdNotation;
import com.workingbit.share.model.EnumRules;

import java.util.*;

/**
 * Created by Aleksey Popryaduhin on 23:21 21/09/2017.
 */
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

  public Board() {
  }

  public Board(boolean black, EnumRules rules) {
    this.black = black;
    this.rules = rules;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public boolean isCursor() {
    return cursor;
  }

  public void setCursor(boolean cursor) {
    this.cursor = cursor;
  }

  public String getBoardBoxId() {
    return boardBoxId;
  }

  public void setBoardBoxId(String boardBoxId) {
    this.boardBoxId = boardBoxId;
  }

  public LinkedList<BoardIdNotation> getPreviousBoards() {
    return previousBoards;
  }

  public void setPreviousBoards(LinkedList<BoardIdNotation> previousBoards) {
    this.previousBoards = previousBoards;
  }

  public String popPreviousBoard() {
    return previousBoards.isEmpty() ? null : previousBoards.pop().getBoardId();
  }

  public void pushPreviousBoard(String boardId, String notation) {
    this.previousBoards.push(new BoardIdNotation(boardId, notation));
  }

  public LinkedList<BoardIdNotation> getNextBoards() {
    return nextBoards;
  }

  public void setNextBoards(LinkedList<BoardIdNotation> nextBoards) {
    this.nextBoards = nextBoards;
  }

  public String popNextBoard() {
    return nextBoards.isEmpty() ? null : nextBoards.pop().getBoardId();
  }

  public void pushNextBoard(String boardId, String notation) {
    nextBoards.push(new BoardIdNotation(boardId, notation));
  }

  public Map<String, Draught> getBlackDraughts() {
    return blackDraughts;
  }

  public void setBlackDraughts(Map<String, Draught> blackDraughts) {
    this.blackDraughts = blackDraughts;
  }

  public Map<String, Draught> getWhiteDraughts() {
    return whiteDraughts;
  }

  public void setWhiteDraughts(Map<String, Draught> whiteDraughts) {
    this.whiteDraughts = whiteDraughts;
  }

  public Square getSelectedSquare() {
    return selectedSquare;
  }

  public void setSelectedSquare(Square selectedSquare) {
    this.selectedSquare = selectedSquare;
  }

  public Square getNextSquare() {
    return nextSquare;
  }

  public void setNextSquare(Square nextSquare) {
    this.nextSquare = nextSquare;
  }

  public List<Square> getSquares() {
    return squares;
  }

  public void setSquares(List<Square> squares) {
    this.squares = squares;
  }

  public List<Square> getAssignedSquares() {
    return assignedSquares;
  }

  public void setAssignedSquares(List<Square> assignedSquares) {
    this.assignedSquares = assignedSquares;
  }

  public boolean isBlack() {
    return black;
  }

  public void setBlack(boolean black) {
    this.black = black;
  }

  public EnumRules getRules() {
    return rules;
  }

  public void setRules(EnumRules rules) {
    this.rules = rules;
  }

  public void addBlackDraughts(String notation, Draught draught) {
    blackDraughts.put(notation, draught);
  }

  public void addWhiteDraughts(String notation, Draught draught) {
    whiteDraughts.put(notation, draught);
  }

  public void setPreviousSquare(Square previousSquare) {
    this.previousSquare = previousSquare;
  }

  public Square getPreviousSquare() {
    return previousSquare;
  }
}
