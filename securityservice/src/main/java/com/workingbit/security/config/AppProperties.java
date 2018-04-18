package com.workingbit.security.config;

import java.net.URL;

/**
 * Created by Aleksey Popryaduhin on 14:17 27/09/2017.
 */
public interface AppProperties {

  String regionDynamoDB();

  URL endpointDynamoDB();

  Boolean test();

  URL origin();

  String methods();

  String headers();

  int port();

  int sessionLength();

  int tokenLength();
}
