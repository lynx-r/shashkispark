package com.workingbit.share.domain.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.workingbit.share.common.DBConstants;
import com.workingbit.share.converter.EnumEditBoardBoxModeConverter;
import com.workingbit.share.converter.LocalDateTimeConverter;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.model.EnumEditBoardBoxMode;
import com.workingbit.share.model.Notation;
import com.workingbit.share.model.Payload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Created by Aleksey Popryaduhin on 19:54 12/08/2017.
 */
@JsonTypeName("boardBox")
@Getter
@Setter
@ToString
@DynamoDBTable(tableName = DBConstants.BOARD_BOX_TABLE)
public class BoardBox extends BaseDomain implements Payload {

  @DynamoDBHashKey(attributeName = "id")
  private String id;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBRangeKey(attributeName = "createdAt")
  private LocalDateTime createdAt;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBAttribute(attributeName = "updatedAt")
  private LocalDateTime updatedAt;

  @DynamoDBAttribute(attributeName = "articleId")
  private String articleId;

  @DynamoDBAttribute(attributeName = "boardId")
  private String boardId;

  @DynamoDBIgnore
  private Board board;

  @DynamoDBTypeConvertedJson(targetType = Notation.class)
  @DynamoDBAttribute(attributeName = "notation")
  private Notation notation;

  @JsonDeserialize(using = EnumEditBoardBoxModeConverter.class)
  @DynamoDBTypeConvertedEnum
  @DynamoDBAttribute(attributeName = "editMode")
  private EnumEditBoardBoxMode editMode;

  public BoardBox() {
    notation = new Notation();
    editMode = EnumEditBoardBoxMode.EDIT;
  }

  public BoardBox(Board board) {
    this();
    this.board = board;
    this.boardId = board.getId();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    BoardBox boardBox = (BoardBox) o;
    return Objects.equals(id, boardBox.id);
  }

  @Override
  public int hashCode() {

    return Objects.hash(super.hashCode(), id);
  }
}
