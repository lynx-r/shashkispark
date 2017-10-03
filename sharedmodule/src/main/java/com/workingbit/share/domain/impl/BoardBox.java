package com.workingbit.share.domain.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.workingbit.share.common.DBConstants;
import com.workingbit.share.converter.LocalDateConverter;
import com.workingbit.share.domain.BaseDomain;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

/**
 * Created by Aleksey Popryaduhin on 19:54 12/08/2017.
 */
@NoArgsConstructor
@Data
@DynamoDBTable(tableName = DBConstants.BOARD_BOX_TABLE)
public class BoardBox implements BaseDomain{

  @DynamoDBHashKey(attributeName = "id")
  private String id;

  @DynamoDBRangeKey(attributeName = "createdAt")
  private Date createdAt;

  @DynamoDBAttribute(attributeName = "articleId")
  private String articleId;

  @DynamoDBAttribute(attributeName = "boardId")
  private String boardId;

  @DynamoDBIgnore
  private Board board;

  @DynamoDBAttribute(attributeName = "whitePlayer")
  private String whitePlayer;

  @DynamoDBAttribute(attributeName = "blackPlayer")
  private String blackPlayer;

  @DynamoDBAttribute(attributeName = "event")
  private String event;

  @DynamoDBAttribute(attributeName = "site")
  private String site;

  @DynamoDBAttribute(attributeName = "round")
  private String round;

  @DynamoDBTypeConverted(converter = LocalDateConverter.class)
  @DynamoDBAttribute(attributeName = "date")
  private LocalDate date;

  @DynamoDBAttribute(attributeName = "result")
  private String result;

  @DynamoDBAttribute(attributeName = "gameType")
  private String gameType;

  public BoardBox(Board board) {
    this.board = board;
    this.boardId = board.getId();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BoardBox that = (BoardBox) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(articleId, that.articleId) &&
        Objects.equals(boardId, that.boardId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, articleId, boardId);
  }
}
