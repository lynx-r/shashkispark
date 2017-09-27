package com.workingbit.article.util;


import lombok.Getter;

import static com.workingbit.article.Application.appProperties;

public class Path {

  // The @Getter methods are needed in order to access
  // the variables from Velocity Templates
  public static class Web {
    @Getter
    public static final String INDEX = appProperties.getContextPath() + "/";
    @Getter
    public static final String ARTICLES = appProperties.getContextPath() + "/articles";
  }
}