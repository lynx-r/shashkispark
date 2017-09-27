package com.workingbit.share.common;

import java.util.logging.Logger;

/**
 * Created by Aleksey Popryaduhin on 08:14 12/08/2017.
 */
public class Log {
  private static final Logger LOG = Logger.getAnonymousLogger();

  public static void error(String message) {
    LOG.severe(message);
  }

  public static void debug(String message) {
    LOG.info(message);
  }

  public static void error(String message, Throwable e) {
    LOG.severe(message);
    e.printStackTrace();
  }
}
