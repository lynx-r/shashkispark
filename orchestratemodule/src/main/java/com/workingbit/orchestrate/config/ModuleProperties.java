package com.workingbit.orchestrate.config;

/**
 * Created by Aleksey Popryaduhin on 14:17 27/09/2017.
 */
public interface ModuleProperties {

  String registerResource();

  String authorizeResource();

  String authenticateResource();

  String articleResource();

  String articlesResource();

  String boardboxResource();

  String userInfoResource();

  String saveUserInfoResource();

  String logoutResource();

  String redisHost();

  String redisPort();

  String parsePdnResource();
}
