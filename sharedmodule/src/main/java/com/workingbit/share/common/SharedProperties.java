package com.workingbit.share.common;

import static com.workingbit.share.common.Config4j.configurationProvider;

/**
 * Created by Aleksey Popryadukhin on 14/05/2018.
 */
public class SharedProperties {

  public static ISharedProperties sharedProperties = configurationProvider("sharedproperties.yaml").bind("shared", ISharedProperties.class);
}
