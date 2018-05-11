package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.model.enumarable.EnumEditBoardBoxMode;
import com.workingbit.share.model.enumarable.EnumRules;
import lombok.Data;

/**
 * CreateBoardPayload
 */
@JsonTypeName("CreateBoardPayload")
@Data
public class CreateBoardPayload implements Payload {
  private DomainId articleId;

  private Boolean fillBoard;
  private Boolean black;
  private EnumRules rules;
  private DomainId boardBoxId;
  private DomainId userId;
  private EnumEditBoardBoxMode editMode;

  private CreateBoardPayload() {
  }

  @JsonCreator
  public CreateBoardPayload(@JsonProperty("articleId") DomainId articleId,
                            @JsonProperty("fillBoard") Boolean fillBoard,
                            @JsonProperty("black") Boolean black,
                            @JsonProperty("rules") EnumRules rules,
                            @JsonProperty("selectedBoardBoxId") DomainId boardBoxId,
                            @JsonProperty("editMode") EnumEditBoardBoxMode editMode
  ) {
    this.articleId = articleId;
    this.fillBoard = fillBoard;
    this.black = black;
    this.rules = rules;
    this.boardBoxId = boardBoxId;
    this.editMode = editMode;
  }

  public static CreateBoardPayload createBoardPayload() {
    return new CreateBoardPayload();
  }
}
