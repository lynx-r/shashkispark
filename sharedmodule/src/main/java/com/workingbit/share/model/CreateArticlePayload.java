package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.workingbit.share.domain.impl.Article;
import lombok.Getter;
import lombok.Setter;

/**
 * CreateBoardPayload
 */
@JsonRootName(value = "createArticlePayload")
@Getter
@Setter
public class CreateArticlePayload implements Payload {
  private Article article ;
  private CreateBoardPayload boardRequest ;
}
