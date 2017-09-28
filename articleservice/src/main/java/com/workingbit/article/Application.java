package com.workingbit.article;

import com.workingbit.article.article.ArticleController;
import com.workingbit.article.article.ArticleDao;
import com.workingbit.article.config.AppProperties;
import com.workingbit.article.util.Filters;
import com.workingbit.article.util.Path;
import com.workingbit.article.util.SparkUtils;
import com.workingbit.article.util.UnirestUtil;
import org.apache.log4j.Logger;

import static com.workingbit.article.config.Config4j.configurationProvider;
import static java.lang.String.format;
import static spark.Spark.*;

public class Application {

  private static final Logger LOG = Logger.getLogger(Application.class);

  // Declare dependencies
  public static ArticleDao articleDao;
  public static AppProperties appProperties;

  static {
    appProperties = configurationProvider().bind("app", AppProperties.class);

    articleDao = new ArticleDao(appProperties);
  }

  public static void main(String[] args) {
    start();
  }

  static void start() {
    Logger logger = Logger.getLogger(Application.class);
    SparkUtils.createServerWithRequestLog(logger);

    UnirestUtil.configureSerialization();

    LOG.info("Initializing routes");

    enableCORS(appProperties.origin().toString(), appProperties.methods(), appProperties.headers());

    path("/api", () ->
        path("/v1", () -> {
          before("/*", (req, res) -> {
            LOG.info(format("%s %s %s",
                req.ip(),
                req.requestMethod(),
                req.url()));
          });

          get(Path.Web.ARTICLES, ArticleController.fetchAllArticles);
          post(Path.Web.ARTICLE, ArticleController.createArticleAndBoard);

          notFound((req, res) -> "Not found");
          internalServerError((req, res) -> "Internal server error");

          after("/*", Filters.addJsonHeader);
          after("/*", Filters.addGzipHeader);
        }));
  }

  // Enables CORS on requests. This method is an initialization method and should be called once.
  private static void enableCORS(final String origin, final String methods, final String headers) {

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
      response.header("Access-Control-Request-Method", methods);
      response.header("Access-Control-Allow-Headers", headers);
      response.header("Vary", "Origin");
      // Note: this may or may not be necessary in your particular application
      response.type("application/json");
    });
  }
}