package com.workingbit.share.model;

import lombok.Getter;
import lombok.Setter;

/**
 * CreateBoardPayload
 */
@Getter
@Setter
public class CreateBoardPayload implements Payload {
  private String articleId;

  private Boolean fillBoard;
  private Boolean black;
  private EnumRules rules;
  private String boardBoxId;
}
