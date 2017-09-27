package com.workingbit.share.domain.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.workingbit.share.common.DBConstants;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.model.EnumArticleState;

import java.util.Date;

/**
 * Created by Aleksey Popryaduhin on 18:31 09/08/2017.
 */
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

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getBoardBoxId() {
    return boardBoxId;
  }

  public void setBoardBoxId(String boardBoxId) {
    this.boardBoxId = boardBoxId;
  }

  public EnumArticleState getState() {
    return state;
  }

  public void setState(EnumArticleState state) {
    this.state = state;
  }
}
