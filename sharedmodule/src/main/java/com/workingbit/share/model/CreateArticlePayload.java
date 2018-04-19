package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.domain.impl.Article;
import lombok.Data;

/**
 * CreateBoardPayload
 */
@JsonTypeName("CreateArticlePayload")
@Data
public class CreateArticlePayload implements Payload {
  private Article article;
  private CreateBoardPayload boardRequest;

  private CreateArticlePayload() {
  }

  @JsonCreator
  public CreateArticlePayload(@JsonProperty("article") Article article,
                              @JsonProperty("boardRequest") CreateBoardPayload boardRequest) {
    this.article = article;
    this.boardRequest = boardRequest;
  }

  public static CreateArticlePayload createArticlePayload() {
    return new CreateArticlePayload();
  }
}
