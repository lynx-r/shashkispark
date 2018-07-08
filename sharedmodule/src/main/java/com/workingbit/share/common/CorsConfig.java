package com.workingbit.share.common;

import static spark.Spark.before;
import static spark.Spark.options;

/**
 * Created by Aleksey Popryaduhin on 10:08 28/09/2017.
 */
public class CorsConfig {

  /**
   *  Enables CORS on requests. This method is an initialization method and should be called once.
   * @param origin
   * @param methods
   * @param headers
   */
  public static void enableCors(final String origin, final String methods, final String headers) {

    options("/*", (request, response) -> {

      String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
      if (accessControlRequestHeaders != null) {
        response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
      }

      String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
      if (accessControlRequestMethod != null) {
        response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
      }

      return "OK";
    });

    before((request, response) -> {
      response.header("Access-Control-Allow-Origin", origin);
//      response.header("Access-Control-Allow-Origin", "http://shashki.local # localhost");
      response.header("Access-Control-Request-Method", methods);
      response.header("Access-Control-Allow-Headers", headers);
      response.header("Access-Control-Allow-Credentials", "true");
      response.header("Vary", "Origin");
      // Note: this may or may not be necessary in your particular application
      response.type("application/json");
    });
  }
}
