package com.workingbit.article.exception;

/**
 * Created by Aleksey Popryaduhin on 19:43 17/09/2017.
 */
public class ArticleServiceError extends Error{
  public ArticleServiceError(String message) {
    super(message);
  }
}
