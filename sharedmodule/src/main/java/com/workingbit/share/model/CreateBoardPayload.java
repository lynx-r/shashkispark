package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

/**
 * CreateBoardPayload
 */
@JsonTypeName("createBoardPayload")
@Data
public class CreateBoardPayload implements Payload {
  private String articleId;

  private Boolean fillBoard;
  private Boolean black;
  private EnumRules rules;
  private String boardBoxId;

  private CreateBoardPayload() {
  }

  @JsonCreator
  public CreateBoardPayload(@JsonProperty("articleId") String articleId,
                            @JsonProperty("fillBoard") Boolean fillBoard,
                            @JsonProperty("black") Boolean black,
                            @JsonProperty("rules") EnumRules rules,
                            @JsonProperty("boardBoxId") String boardBoxId
  ) {
    this.articleId = articleId;
    this.fillBoard = fillBoard;
    this.black = black;
    this.rules = rules;
    this.boardBoxId = boardBoxId;
  }

  public static CreateBoardPayload createBoardPayload() {
    return new CreateBoardPayload();
  }
}
