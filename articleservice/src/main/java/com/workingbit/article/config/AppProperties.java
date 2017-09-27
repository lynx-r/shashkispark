package com.workingbit.article.config;

import lombok.Getter;

/**
 * Created by Aleksey Popryaduhin on 14:17 27/09/2017.
 */
@Getter
public class AppProperties {

  private final String region = "eu-west-1";
  private final String endpoint = "http://localhost:8081";
  private final boolean test = true;
  private final int articlesFetchLimit = 50;
  private final String contextPath = "/v1";
}
