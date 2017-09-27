package com.workingbit.share.model;

import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.BoardBox;
import lombok.Getter;
import lombok.Setter;

/**
 * CreateBoardRequest
 */
@Getter
@Setter
public class CreateArticleResponse {
  private Article article ;
  private BoardBox board ;
}
