package com.workingbit.board.config;

import org.jetbrains.annotations.NotNull;

import java.net.URL;

/**
 * Created by Aleksey Popryaduhin on 14:17 27/09/2017.
 */
public interface AppProperties {

  @NotNull String regionDynamoDB();

  @NotNull URL endpointDynamoDB();

  @NotNull Boolean test();

  @NotNull URL origin();

  @NotNull String methods();

  @NotNull String headers();

  int port();
}
