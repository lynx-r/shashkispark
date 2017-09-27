package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.workingbit.share.domain.impl.Article;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * CreateBoardRequest
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-09-14T22:16:30.741+03:00")

public class CreateArticleRequest {
  @JsonProperty("article")
  private Article article = null;

  @JsonProperty("boardRequest")
  private CreateBoardRequest boardRequest = null;

  public CreateArticleRequest fillBoard(Article fillBoard) {
    this.article = fillBoard;
    return this;
  }

   /**
   * Get article
   * @return article
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull


  public Article getArticle() {
    return article;
  }

  public void setArticle(Article article) {
    this.article = article;
  }

  public CreateArticleRequest black(CreateBoardRequest black) {
    this.boardRequest = black;
    return this;
  }

   /**
   * Get boardRequest
   * @return boardRequest
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull


  public CreateBoardRequest getBoardRequest() {
    return boardRequest;
  }

  public void setBoardRequest(CreateBoardRequest boardRequest) {
    this.boardRequest = boardRequest;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateArticleRequest createBoardRequest = (CreateArticleRequest) o;
    return Objects.equals(this.article, createBoardRequest.article) &&
        Objects.equals(this.boardRequest, createBoardRequest.boardRequest);
  }

  @Override
  public int hashCode() {
    return Objects.hash(article, boardRequest);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateBoardRequest {\n");

    sb.append("    article: ").append(toIndentedString(article)).append("\n");
    sb.append("    boardRequest: ").append(toIndentedString(boardRequest)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

