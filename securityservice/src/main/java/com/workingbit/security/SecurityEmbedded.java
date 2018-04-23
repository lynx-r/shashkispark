package com.workingbit.security;

import com.workingbit.security.config.AppProperties;
import com.workingbit.security.config.Path;
import com.workingbit.security.controller.SecurityController;
import com.workingbit.security.dao.SecureUserDao;
import com.workingbit.security.service.SecureUserService;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.model.Answer;
import com.workingbit.share.util.Filters;
import com.workingbit.share.util.SparkUtils;
import com.workingbit.share.util.UnirestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.workingbit.share.common.Config4j.configurationProvider;
import static com.workingbit.share.common.CorsConfig.enableCors;
import static com.workingbit.share.util.JsonUtils.dataToJson;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
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
    path("/", () -> get(Path.HOME, SecurityController.home));

    path("/api", () ->
        path("/v1", () -> {
          post(Path.REGISTER, SecurityController.register);
          post(Path.AUTHORIZE, SecurityController.authorize);
          post(Path.AUTHENTICATE, SecurityController.authenticate);
          post(Path.USER_INFO, SecurityController.userInfo);
          post(Path.LOGOUT, SecurityController.logout);

          notFound((req, res) -> dataToJson(Answer.error(HTTP_NOT_FOUND, ErrorMessages.RESOURCE_NOT_FOUND)));
          internalServerError((req, res) -> dataToJson(Answer.error(HTTP_INTERNAL_ERROR, ErrorMessages.INTERNAL_SERVER_ERROR)));

          after(Filters.addJsonHeader);
          after(Filters.addGzipHeader);
        })
    );
  }
}