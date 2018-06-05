package com.workingbit.share.common;

import org.apache.commons.lang3.StringUtils;
import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.classpath.ClasspathConfigurationSource;
import org.cfg4j.source.context.environment.Environment;
import org.cfg4j.source.context.environment.ImmutableEnvironment;
import org.cfg4j.source.context.filesprovider.ConfigFilesProvider;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.Collections;

/**
 * Created by Aleksey Popryaduhin on 00:19 28/09/2017.
 */
public class Config4j {

  private static Logger logger = LoggerFactory.getLogger(Config4j.class);

  public static ConfigurationProvider configurationProvider(@NotNull String config) {
    // Specify which files to load. Configuration from both files will be merged.
    ConfigFilesProvider configFilesProvider = () -> Collections.singletonList(Paths.get(config));

    // Use classpath repository as configuration store
    ConfigurationSource source = new ClasspathConfigurationSource(configFilesProvider);

    String cfg4J_env = System.getenv("CFG4J_ENV");
    if (StringUtils.isBlank(cfg4J_env)) {
      cfg4J_env = "dev";
    }
    logger.info("Use env " + cfg4J_env);
    Environment environment = new ImmutableEnvironment(cfg4J_env);

    // Create provider
    return new ConfigurationProviderBuilder()
        .withConfigurationSource(source)
        .withEnvironment(environment)
        .build();
  }
}
