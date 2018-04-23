package com.workingbit.board;

import spark.servlet.SparkApplication;

/**
 * Created by Aleksey Popryadukhin on 19/04/2018.
 */
public class BoardApplication extends BoardEmbedded implements SparkApplication {

  @Override
  public void init() {
    start();
  }
}
