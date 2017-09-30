package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.domain.Validable;
import lombok.Getter;
import lombok.Setter;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * CreateBoardRequest
 */
@Getter
@Setter
public class CreateBoardRequest implements Validable {
  private String articleId;

  private Boolean fillBoard;
  private Boolean black;
  private EnumRules rules;
  private String boardBoxId;

  @JsonIgnore
  @Override
  public boolean isValid() {
    return isNotBlank(articleId) && isNotBlank(boardBoxId);
  }
}
