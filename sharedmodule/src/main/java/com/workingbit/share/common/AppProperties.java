package com.workingbit.share.common;

import java.net.URL;

/**
 * Created by Aleksey Popryaduhin on 14:17 27/09/2017.
 */
public interface AppProperties {

  String regionDynamoDB();

  URL endpointDynamoDB();

  Boolean test();

  int port();
}
