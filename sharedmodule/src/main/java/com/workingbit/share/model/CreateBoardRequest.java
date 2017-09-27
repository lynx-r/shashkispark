package com.workingbit.share.model;

import lombok.Getter;

/**
 * CreateBoardRequest
 */
@Getter
public class CreateBoardRequest {
  private String articleId;

  private Boolean fillBoard;
  private Boolean black;
  private EnumRules rules;
  private String boardBoxId;
}
