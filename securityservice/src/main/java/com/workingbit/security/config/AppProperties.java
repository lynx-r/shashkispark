package com.workingbit.security.config;

import org.jetbrains.annotations.NotNull;

import java.net.URL;

/**
 * Created by Aleksey Popryaduhin on 14:17 27/09/2017.
 */
public interface AppProperties {

  String domain();

  int cost();

  int misc();

  String sysSigma();

  @NotNull String regionDynamoDB();

  @NotNull URL endpointDynamoDB();

  @NotNull Boolean local();

  @NotNull URL origin();

  @NotNull String methods();

  @NotNull String headers();

  int port();

  int sessionLength();

  int tokenLength();

  @NotNull String superHashEnvName();

  @NotNull String passwordFileKey();

  @NotNull String passwordFilename();
}
