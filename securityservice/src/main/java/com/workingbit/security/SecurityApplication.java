package com.workingbit.security;

import spark.servlet.SparkApplication;

/**
 * Created by Aleksey Popryadukhin on 19/04/2018.
 */
public class SecurityApplication extends SecurityEmbedded implements SparkApplication {

  @Override
  public void init() {
    start();
  }
}
