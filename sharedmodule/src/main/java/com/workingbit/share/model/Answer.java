package com.workingbit.share.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.workingbit.share.deserializer.AnswerDeserializer;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonDeserialize(using = AnswerDeserializer.class)
@NoArgsConstructor
@Data
public class Answer {

  private int code;
  private Object body;
  private String error;
  private Type classType;

  public Answer(int code) {
    this.code = code;
  }

  public Answer(int code, String error) {
    this.code = code;
    this.error = error;
    this.classType = Type.ERROR;
  }

  public Answer(int code, Object body, Type classType) {
    this.code = code;
    this.body = body;
    this.classType = classType;
  }

  public static Answer error(int code, String message) {
    return new Answer(code, message);
  }

  public static Answer okBoardBox(Object body) {
    return new Answer(200, body, Type.BOARD_BOX);
  }

  public static Answer okArticle(Object body) {
    return new Answer(200, body, Type.ARTICLE);
  }

  public static Answer okArticleList(Object body) {
    return new Answer(200, body, Type.ARTICLE_LIST);
  }

  public static Answer okArticleCreate(CreateArticleResponse createArticleResponse) {
    return new Answer(201, createArticleResponse, Type.ARTICLE_CREATE);
  }

  public enum Type {
    ARTICLE, ARTICLE_LIST, BOARD_BOX, BOARD_BOX_LIST, ARTICLE_CREATE, ERROR
  }
}