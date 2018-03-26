package com.workingbit.share.domain.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.workingbit.share.common.DBConstants;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.model.EnumArticleState;
import lombok.Data;

import java.util.Date;

/**
 * Created by Aleksey Popryaduhin on 18:31 09/08/2017.
 */
@Data
@DynamoDBTable(tableName = DBConstants.ARTICLE_TABLE)
public class Article implements BaseDomain {

  @DynamoDBHashKey(attributeName = "id")
  private String id;

  @DynamoDBRangeKey(attributeName = "createdAt")
  private Date createdAt;

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
}
