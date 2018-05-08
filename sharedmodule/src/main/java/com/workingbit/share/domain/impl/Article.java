package com.workingbit.share.domain.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.common.DBConstants;
import com.workingbit.share.converter.LocalDateTimeConverter;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.model.enumarable.EnumArticleStatus;
import com.workingbit.share.model.Payload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Created by Aleksey Popryaduhin on 18:31 09/08/2017.
 */
@JsonTypeName("Article")
@Getter
@Setter
@ToString
@DynamoDBTable(tableName = DBConstants.ARTICLE_TABLE)
public class Article extends BaseDomain implements Payload {

  @DynamoDBIndexHashKey(globalSecondaryIndexName = "articleIndex")
  @DynamoDBHashKey(attributeName = "id")
  private String id;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBRangeKey(attributeName = "createdAt")
  private LocalDateTime createdAt;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBAttribute(attributeName = "updatedAt")
  private LocalDateTime updatedAt;

  @DynamoDBIndexHashKey(globalSecondaryIndexName = "humanReadableUrlIndex")
  @DynamoDBAttribute(attributeName = "humanReadableUrl")
  private String humanReadableUrl;

  @DynamoDBAttribute(attributeName = "author")
  private String author;

  @DynamoDBAttribute(attributeName = "userId")
  private String userId;

  @DynamoDBAttribute(attributeName = "title")
  private String title;

  @DynamoDBAttribute(attributeName = "content")
  private String content;

  @DynamoDBAttribute(attributeName = "boardBoxId")
  private String boardBoxId;

  @DynamoDBTypeConvertedEnum
  @DynamoDBAttribute(attributeName = "articleStatus")
  private EnumArticleStatus articleStatus;

  @DynamoDBAttribute(attributeName = "readonly")
  private boolean readonly;

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
                 @JsonProperty("articleStatus") EnumArticleStatus articleStatus,
                 @JsonProperty("humanReadableUrl") String humanReadableUrl
  ) {
    this.id = id;
    this.createdAt = createdAt;
    this.author = author;
    this.title = title;
    this.content = content;
    this.boardBoxId = boardBoxId;
    this.articleStatus = articleStatus;
    this.humanReadableUrl = humanReadableUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Article article = (Article) o;
    return Objects.equals(id, article.id);
  }

  @Override
  public int hashCode() {

    return Objects.hash(id);
  }
}
