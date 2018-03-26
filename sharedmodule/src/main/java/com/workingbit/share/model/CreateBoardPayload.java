package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Getter;
import lombok.Setter;

/**
 * CreateBoardPayload
 */
@JsonRootName(value = "createBoardPayload")
@Getter
@Setter
public class CreateBoardPayload implements Payload {
  private String articleId;

  private Boolean fillBoard;
  private Boolean black;
  private EnumRules rules;
  private String boardBoxId;
}
