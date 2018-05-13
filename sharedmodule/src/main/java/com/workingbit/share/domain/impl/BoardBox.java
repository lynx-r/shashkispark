package com.workingbit.share.domain.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.workingbit.share.common.DBConstants;
import com.workingbit.share.converter.EnumEditBoardBoxModeConverter;
import com.workingbit.share.converter.LocalDateTimeConverter;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.model.DomainId;
import com.workingbit.share.model.Payload;
import com.workingbit.share.model.enumarable.EnumEditBoardBoxMode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Created by Aleksey Popryaduhin on 19:54 12/08/2017.
 */
@JsonTypeName("BoardBox")
@Getter
@Setter
@ToString
@DynamoDBTable(tableName = DBConstants.BOARD_BOX_TABLE)
public class BoardBox extends BaseDomain implements Payload {

  @DynamoDBIndexHashKey(globalSecondaryIndexName = "boardBoxIndex")
  @DynamoDBHashKey(attributeName = "id")
  private String id;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBRangeKey(attributeName = "createdAt")
  private LocalDateTime createdAt;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBAttribute(attributeName = "updatedAt")
  private LocalDateTime updatedAt;

  @DynamoDBTyped(value = DynamoDBMapperFieldModel.DynamoDBAttributeType.M)
  @DynamoDBAttribute(attributeName = "userId")
  private DomainId userId;

  @DynamoDBTyped(value = DynamoDBMapperFieldModel.DynamoDBAttributeType.M)
  @DynamoDBAttribute(attributeName = "articleId")
  private DomainId articleId;

  @DynamoDBTyped(value = DynamoDBMapperFieldModel.DynamoDBAttributeType.M)
  @DynamoDBAttribute(attributeName = "boardId")
  private DomainId boardId;

  @DynamoDBIgnore
  private Board board;

  @DynamoDBTyped(value = DynamoDBMapperFieldModel.DynamoDBAttributeType.M)
  @DynamoDBAttribute(attributeName = "notationId")
  private DomainId notationId;

  @DynamoDBIgnore
  private Notation notation;

  @JsonDeserialize(using = EnumEditBoardBoxModeConverter.class)
  @DynamoDBTypeConvertedEnum
  @DynamoDBAttribute(attributeName = "editMode")
  private EnumEditBoardBoxMode editMode;

  @DynamoDBAttribute(attributeName = "readonly")
  private boolean readonly;

  /**
   * Показывать ли эту доску в просмотре статьи когда пользователь не залогинен
   */
  @DynamoDBAttribute(attributeName = "visiblePublic")
  private boolean visiblePublic;

  public BoardBox() {
    editMode = EnumEditBoardBoxMode.EDIT;
    visiblePublic = true;
  }

  public BoardBox(Board board) {
    this();
    this.board = board;
    this.boardId = new DomainId(board);
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

  public BoardBox readonly(boolean readonly) {
    this.readonly = readonly;
    return this;
  }
}
