package com.workingbit.orchestrate.config;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Aleksey Popryaduhin on 14:17 27/09/2017.
 */
public interface ModuleProperties {

  String preRegisterResource();

  String preAuthorizeResource();

  @NotNull String registerResource();

  @NotNull String authorizeResource();

  @NotNull String authenticateResource();

  @NotNull String articleResource();

  @NotNull String articlesResource();

  @NotNull String boardboxResource();

  String boardboxDeleteByArticleIdResource();

  @NotNull String userInfoResource();

  @NotNull String saveUserInfoResource();

  @NotNull String logoutResource();

  @NotNull String redisHost();

  @NotNull String redisPort();

  @NotNull String parsePdnResource();
}
