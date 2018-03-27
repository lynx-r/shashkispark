package com.workingbit.share.domain.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.common.DBConstants;
import com.workingbit.share.converter.LocalDateTimeConverter;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.model.EnumArticleState;
import com.workingbit.share.model.Payload;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created by Aleksey Popryaduhin on 18:31 09/08/2017.
 */
@JsonTypeName("article")
//@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@Data
@DynamoDBTable(tableName = DBConstants.ARTICLE_TABLE)
public class Article extends BaseDomain implements Payload {

  @DynamoDBHashKey(attributeName = "id")
  private String id;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBRangeKey(attributeName = "createdAt")
  private LocalDateTime createdAt;

  @DynamoDBAttribute(attributeName = "author")
  private String author;

  @DynamoDBAttribute(attributeName = "title")
  private String title;

  @DynamoDBAttribute(attributeName = "content")
  private String content;

  @DynamoDBAttribute(attributeName = "boardId")
  private String boardBoxId;

  @DynamoDBTypeConvertedEnum
  @DynamoDBAttribute(attributeName = "state")
  private EnumArticleState state;

  public Article() {
  }

  public Article(String author, String title, String content) {
    this.author = author;
    this.title = title;
    this.content = content;
  }

  @JsonCreator
  public Article(@JsonProperty("id") String id,
                 @JsonProperty("createdAt") LocalDateTime createdAt,
                 @JsonProperty("author") String author,
                 @JsonProperty("title") String title,
                 @JsonProperty("content") String content,
                 @JsonProperty("boardBoxId") String boardBoxId,
                 @JsonProperty("state") EnumArticleState state
  ) {
    this.id = id;
    this.createdAt = createdAt;
    this.author = author;
    this.title = title;
    this.content = content;
    this.boardBoxId = boardBoxId;
    this.state = state;
  }
}
