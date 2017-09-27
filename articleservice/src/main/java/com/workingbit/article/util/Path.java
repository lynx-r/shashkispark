package com.workingbit.article.util;


import lombok.Getter;

public class Path {

  // The @Getter methods are needed in order to access
  // the variables from Velocity Templates
  public static class Web {
    @Getter
    public static final String INDEX = "/";
    @Getter
    public static final String ARTICLES = "/articles";
    public static final String ARTICLE = "/article";
  }
}