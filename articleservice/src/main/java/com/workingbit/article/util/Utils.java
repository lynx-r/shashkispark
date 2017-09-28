package com.workingbit.article.util;

import com.workingbit.article.exception.ArticleServiceException;

import java.util.function.Supplier;

/**
 * Created by Aleksey Popryaduhin on 09:19 28/09/2017.
 */
public class Utils {

  public static Supplier<ArticleServiceException> getArticleServiceErrorSupplier(String message) {
    return () -> new ArticleServiceException(message);
  }

}
