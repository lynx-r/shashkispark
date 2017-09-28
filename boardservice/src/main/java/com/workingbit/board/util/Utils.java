package com.workingbit.article.util;

import com.workingbit.article.exception.ArticleServiceError;

import java.util.function.Supplier;

/**
 * Created by Aleksey Popryaduhin on 09:19 28/09/2017.
 */
public class Utils {

  public static Supplier<ArticleServiceError> getArticleServiceErrorSupplier(String message) {
    return () -> new ArticleServiceError(message);
  }

}
