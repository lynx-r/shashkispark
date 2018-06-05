package com.workingbit.share.util;

import com.workingbit.share.model.QueryPayload;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.AbstractNCSARequestLog;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import spark.embeddedserver.EmbeddedServers;
import spark.embeddedserver.jetty.EmbeddedJettyFactory;

public class SparkUtils {
  public static void createServerWithRequestLog(Logger logger) {
    EmbeddedJettyFactory factory = createEmbeddedJettyFactoryWithRequestLog(logger);
    EmbeddedServers.add(EmbeddedServers.Identifiers.JETTY, factory);
  }

  private static EmbeddedJettyFactory createEmbeddedJettyFactoryWithRequestLog(Logger logger) {
    AbstractNCSARequestLog requestLog = new RequestLogFactory(logger).create();
    return new EmbeddedJettyFactoryConstructor(requestLog).create();
  }

  @NotNull
  public static String getQueryValue(QueryPayload query, String queryParam) {
    String value = query.getQuery().value(queryParam);
    return StringUtils.isNotBlank(value) ? value : "";
  }
}