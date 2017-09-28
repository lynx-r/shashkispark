package com.workingbit.article.exception;

/**
 * Created by Aleksey Popryaduhin on 19:43 17/09/2017.
 */
public class ArticleServiceException extends RuntimeException {
  public ArticleServiceException(String message) {
    super(message);
  }
}
