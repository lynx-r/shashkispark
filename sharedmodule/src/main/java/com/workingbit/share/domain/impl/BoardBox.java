package com.workingbit.share.domain.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.common.DBConstants;
import com.workingbit.share.converter.LocalDateTimeConverter;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.model.Notation;
import com.workingbit.share.model.Payload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Created by Aleksey Popryaduhin on 19:54 12/08/2017.
 */
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("boardBox")
@NoArgsConstructor
@Data
@DynamoDBTable(tableName = DBConstants.BOARD_BOX_TABLE)
public class BoardBox extends BaseDomain implements Payload {

  @DynamoDBHashKey(attributeName = "id")
  private String id;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBRangeKey(attributeName = "createdAt")
  private LocalDateTime createdAt;

  @DynamoDBAttribute(attributeName = "articleId")
  private String articleId;

  @DynamoDBAttribute(attributeName = "boardId")
  private String boardId;

  @DynamoDBIgnore
  private Board board;

  @DynamoDBTypeConvertedJson(targetType = Notation.class)
  @DynamoDBAttribute(attributeName = "notation")
  private Notation notation = new Notation();

  public BoardBox(Board board) {
    this.board = board;
    this.boardId = board.getId();
  }
//
//  @Override
//  public boolean equals(Object o) {
//    if (this == o) return true;
//    if (o == null || getClass() != o.getClass()) return false;
//    BoardBox that = (BoardBox) o;
//    return Objects.equals(id, that.id) &&
//        Objects.equals(articleId, that.articleId) &&
//        Objects.equals(boardId, that.boardId);
//  }
//
//  @Override
//  public int hashCode() {
//    return Objects.hash(id, articleId, boardId);
//  }
}
