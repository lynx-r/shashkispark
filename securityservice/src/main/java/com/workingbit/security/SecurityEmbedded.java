package com.workingbit.security;

import com.workingbit.orchestrate.OrchestrateModule;
import com.workingbit.security.config.AppProperties;
import com.workingbit.security.config.Authority;
import com.workingbit.security.controller.SecurityController;
import com.workingbit.security.dao.SiteUserInfoDao;
import com.workingbit.security.service.LoggedInService;
import com.workingbit.security.service.SecureUserService;
import com.workingbit.share.exception.ExceptionHandler;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.exception.RequestException;
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
  public static SiteUserInfoDao siteUserInfoDao;
  public static LoggedInService loggedInService;

  static {
    OrchestrateModule.loadModule();

    appProperties = configurationProvider("application.yaml").bind("app", AppProperties.class);

    siteUserInfoDao = new SiteUserInfoDao(appProperties);

    secureUserService = new SecureUserService();
    loggedInService = new LoggedInService();
  }

  public static void main(String[] args) {
    port(appProperties.port());

    Logger logger = LoggerFactory.getLogger(SecurityEmbedded.class);
    SparkUtils.createServerWithRequestLog(logger);
    start();
  }

  public static void start() {
    UnirestUtil.configureSerialization();

    LOG.info("Initializing routes");

    enableCors(appProperties.origin().toString(), appProperties.methods(), appProperties.headers());
    establishRoutes();
  }

  private static void establishRoutes() {
    path("/", () -> get(Authority.HOME.getPath(), SecurityController.home));

    path("/api", () ->
        path("/v1", () -> {
          // open api
          post(Authority.REGISTER.getPath(), SecurityController.register);
          post(Authority.AUTHORIZE.getPath(), SecurityController.authorize);

          // protected api
          get(Authority.AUTHENTICATE_PROTECTED.getPath(), SecurityController.authenticate);
          post(Authority.USER_INFO_PROTECTED.getPath(), SecurityController.userInfo);
          post(Authority.SAVE_USER_INFO_PROTECTED.getPath(), SecurityController.saveUserInfo);
          get(Authority.LOGOUT_PROTECTED.getPath(), SecurityController.logout);

          exception(RequestException.class, ExceptionHandler.handle);
          notFound((req, res) -> dataToJson(Answer.error(HTTP_NOT_FOUND, ErrorMessages.RESOURCE_NOT_FOUND)));
          internalServerError((req, res) -> dataToJson(Answer.error(HTTP_INTERNAL_ERROR, ErrorMessages.INTERNAL_SERVER_ERROR)));

          after(Filters.addJsonHeader);
          after(Filters.addGzipHeader);
        })
    );
  }
}