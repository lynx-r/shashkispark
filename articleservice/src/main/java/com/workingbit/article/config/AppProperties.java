package com.workingbit.article.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Aleksey Popryaduhin on 14:17 27/09/2017.
 */
@Getter
public class AppProperties {

  private final String region = "eu-west-1";
  private final String endpoint = "http://localhost:8081";
  @Setter
  private boolean test = false;
  private final int articlesFetchLimit = 50;

  @Setter
  private String origin = "https://wiki.shashki.online";

  private final String methods = "GET,POST,PUT,OPTIONS,HEADER,DELETE";

  private final String headers = "Content-Type,Content-Length,Connection,Date,x-amzn-RequestId,Access-Control-Allow-Origin,Content-Encoding,Vary,X-Amzn-Trace-Id,X-Cache,Via,X-Amz-Cf-Id";
}
