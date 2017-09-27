package com.workingbit.article.config;

import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.classpath.ClasspathConfigurationSource;
import org.cfg4j.source.context.filesprovider.ConfigFilesProvider;

import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by Aleksey Popryaduhin on 00:19 28/09/2017.
 */
public class Config4j {

  public static ConfigurationProvider configurationProvider() {
    // Specify which files to load. Configuration from both files will be merged.
    ConfigFilesProvider configFilesProvider = () -> Arrays.asList(Paths.get("application.yaml"));

    // Use classpath repository as configuration store
    ConfigurationSource source = new ClasspathConfigurationSource(configFilesProvider);

    // Create provider
    return new ConfigurationProviderBuilder()
        .withConfigurationSource(source)
        .build();
  }
}
