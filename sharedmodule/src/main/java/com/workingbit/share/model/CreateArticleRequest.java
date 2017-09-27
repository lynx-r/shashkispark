package com.workingbit.share.model;

import com.workingbit.share.domain.impl.Article;
import lombok.Getter;

/**
 * CreateBoardRequest
 */
@Getter
public class CreateArticleRequest {
  private Article article ;
  private CreateBoardRequest boardRequest ;
}
