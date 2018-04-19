package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.BoardBox;
import lombok.Data;

/**
 * CreateBoardPayload
 */
@JsonTypeName("CreateArticleResponse")
@Data
public class CreateArticleResponse implements Payload {
  private Article article ;
  private BoardBox board ;

  private CreateArticleResponse() {
  }

  @JsonCreator
  public CreateArticleResponse(@JsonProperty("article") Article article,
                               @JsonProperty("board") BoardBox board
  ) {
    this.article = article;
    this.board = board;
  }

  public static CreateArticleResponse createArticleResponse() {
    return new CreateArticleResponse();
  }
}
