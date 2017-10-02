package com.workingbit.share.domain.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.common.DBConstants;
import com.workingbit.share.domain.BaseDomain;

import java.util.Date;
import java.util.Objects;

/**
 * Created by Aleksey Popryaduhin on 19:54 12/08/2017.
 */
@DynamoDBTable(tableName = DBConstants.BOARD_BOX_TABLE)
public class BoardBox implements BaseDomain{

  @DynamoDBHashKey(attributeName = "id")
  private String id;

  @DynamoDBRangeKey(attributeName = "createdAt")
  private Date createdAt;

  @DynamoDBAttribute(attributeName = "articleId")
  private String articleId;

  @JsonIgnore
  @DynamoDBAttribute(attributeName = "boardId")
  private String boardId;

  @DynamoDBIgnore
  private Board board;

  public BoardBox() {
  }

  public BoardBox(Board board) {
    this.board = board;
    this.boardId = board.getId();
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

  public String getArticleId() {
    return articleId;
  }

  public void setArticleId(String articleId) {
    this.articleId = articleId;
  }

  public String getBoardId() {
    return boardId;
  }

  public void setBoardId(String boardId) {
    this.boardId = boardId;
  }

  /**
   * For backward compatibility
   */
  public Board getBoard() {
    return board;
  }

  public void setBoard(Board board) {
    this.board = board;
  }

  /**
   * END For backward compatibility
   */

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
