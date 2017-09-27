package com.workingbit.share.model;

import com.workingbit.share.domain.impl.Article;
import lombok.Getter;
import lombok.Setter;

/**
 * CreateBoardRequest
 */
@Getter
@Setter
public class CreateArticleRequest {
  private Article article ;
  private CreateBoardRequest boardRequest ;
}
