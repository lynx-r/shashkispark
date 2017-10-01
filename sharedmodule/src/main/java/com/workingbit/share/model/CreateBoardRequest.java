package com.workingbit.share.model;

import lombok.Getter;
import lombok.Setter;

/**
 * CreateBoardRequest
 */
@Getter
@Setter
public class CreateBoardRequest implements Payload {
  private String articleId;

  private Boolean fillBoard;
  private Boolean black;
  private EnumRules rules;
  private String boardBoxId;
}
