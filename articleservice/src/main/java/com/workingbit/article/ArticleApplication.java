package com.workingbit.article;

import spark.servlet.SparkApplication;

/**
 * Created by Aleksey Popryadukhin on 19/04/2018.
 */
public class ArticleApplication extends ArticleEmbedded implements SparkApplication {

  @Override
  public void init() {
    start();
  }
}
