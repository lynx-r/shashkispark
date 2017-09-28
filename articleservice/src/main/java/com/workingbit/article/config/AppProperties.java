package com.workingbit.article.config;

import java.net.URL;

/**
 * Created by Aleksey Popryaduhin on 14:17 27/09/2017.
 */
public interface AppProperties {

  String regionDynamoDB();

  URL endpointDynamoDB();

  Boolean test();

  Integer articlesFetchLimit();

  URL origin();

  String methods();

  String headers();

  String boardResource();

  int port();
}
