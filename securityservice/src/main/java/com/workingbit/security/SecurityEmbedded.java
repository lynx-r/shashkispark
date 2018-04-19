package com.workingbit.security;

import com.workingbit.security.config.AppProperties;
import com.workingbit.security.config.Path;
import com.workingbit.security.controller.SecurityController;
import com.workingbit.security.dao.SecureUserDao;
import com.workingbit.security.service.SecureUserService;
import com.workingbit.share.util.Filters;
import com.workingbit.share.util.SparkUtils;
import com.workingbit.share.util.UnirestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.workingbit.share.common.Config4j.configurationProvider;
import static com.workingbit.share.common.CorsConfig.enableCors;
import static spark.Spark.*;

public class SecurityEmbedded {

  private static final Logger LOG = LoggerFactory.getLogger(SecurityEmbedded.class);

  // Declare dependencies
  public static AppProperties appProperties;
  public static SecureUserService secureUserService;
  public static SecureUserDao secureUserDao;

  static {
    appProperties = configurationProvider("application.yaml").bind("app", AppProperties.class);

    secureUserDao = new SecureUserDao(appProperties);

    secureUserService = new SecureUserService();
  }

  public static void main(String[] args) {
    port(appProperties.port());
    start();
  }

  public static void start() {
    Logger logger = LoggerFactory.getLogger(SecurityEmbedded.class);
    SparkUtils.createServerWithRequestLog(logger);

    UnirestUtil.configureSerialization();

    LOG.info("Initializing routes");

    enableCors(appProperties.origin().toString(), appProperties.methods(), appProperties.headers());
    establishRoutes();
  }

  private static void establishRoutes() {
    path("/api", () ->
        path("/v1", () -> {
          before((req, res)-> System.out.println(req.toString() + " " + res.toString()));
          post(Path.REGISTER, SecurityController.register);
          post(Path.AUTHORIZE, SecurityController.authorize);
          post(Path.AUTHENTICATE, SecurityController.authenticate);
          post(Path.USER_INFO, SecurityController.userInfo);
          post(Path.LOGOUT, SecurityController.logout);

          notFound((req, res) -> "Not found");
          internalServerError((req, res) -> "Internal server message");

          after(Filters.addJsonHeader);
          after(Filters.addGzipHeader);
        })
    );
  }
}