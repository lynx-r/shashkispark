package com.workingbit.share.domain.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.common.DBConstants;
import com.workingbit.share.converter.LocalDateTimeConverter;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.model.DomainId;
import com.workingbit.share.model.Payload;
import com.workingbit.share.model.enumarable.EnumArticleStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
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

  @DynamoDBIndexHashKey(globalSecondaryIndexName = "articleIdIndex")
  @DynamoDBHashKey(attributeName = "id")
  private String id;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBRangeKey(attributeName = "createdAt")
  private LocalDateTime createdAt;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBAttribute(attributeName = "updatedAt")
  private LocalDateTime updatedAt;

  @NotBlank
  @Size(max = 500)
  @DynamoDBIndexHashKey(globalSecondaryIndexName = "humanReadableUrlIndex")
  @DynamoDBAttribute(attributeName = "humanReadableUrl")
  private String humanReadableUrl;

  @NotBlank
  @Size(max = 200)
  @DynamoDBAttribute(attributeName = "author")
  private String author;

  @DynamoDBTyped(value = DynamoDBMapperFieldModel.DynamoDBAttributeType.M)
  @DynamoDBAttribute(attributeName = "userId")
  private DomainId userId;

  @NotBlank
  @Size(max = 1000)
  @DynamoDBAttribute(attributeName = "title")
  private String title;

  @NotBlank
  @Size(max = 50000)
  @DynamoDBAttribute(attributeName = "content")
  private String content;

  @NotBlank
  @Size(max = 1000)
  @DynamoDBAttribute(attributeName = "intro")
  private String intro;

  @DynamoDBTyped(value = DynamoDBMapperFieldModel.DynamoDBAttributeType.M)
  @DynamoDBAttribute(attributeName = "selectedBoardBoxId")
  private DomainId selectedBoardBoxId;

  @DynamoDBAttribute(attributeName = "boardBoxCount")
  private int boardBoxCount;

  @DynamoDBIndexHashKey(globalSecondaryIndexName = "articleStatusIndex")
  @DynamoDBTypeConvertedEnum
  @DynamoDBAttribute(attributeName = "articleStatus")
  private EnumArticleStatus articleStatus;

  @DynamoDBAttribute(attributeName = "readonly")
  private boolean readonly;

  public Article() {
  }

  public Article(String author, String title, String intro, String content) {
    this();
    this.author = author;
    this.title = title;
    this.intro = intro;
    this.content = content;
  }

  @JsonCreator
  public Article(@JsonProperty("id") String id,
                 @JsonProperty("createdAt") LocalDateTime createdAt,
                 @JsonProperty("author") String author,
                 @JsonProperty("title") String title,
                 @JsonProperty("intro") String intro,
                 @JsonProperty("content") String content,
                 @JsonProperty("selectedBoardBoxId") DomainId selectedBoardBoxId,
                 @JsonProperty("articleStatus") EnumArticleStatus articleStatus,
                 @JsonProperty("humanReadableUrl") String humanReadableUrl
  ) {
    this.id = id;
    this.createdAt = createdAt;
    this.author = author;
    this.title = title;
    this.intro = intro;
    this.content = content;
    this.selectedBoardBoxId = selectedBoardBoxId;
    this.articleStatus = articleStatus;
    this.humanReadableUrl = humanReadableUrl;
  }

  @Override
  public boolean equals(@Nullable Object o) {
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
