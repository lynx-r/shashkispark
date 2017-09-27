package com.workingbit.article;

import com.workingbit.article.article.ArticleController;
import com.workingbit.article.article.ArticleDao;
import com.workingbit.article.config.AppProperties;
import com.workingbit.article.index.IndexController;
import com.workingbit.article.util.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;
import static spark.Spark.*;

public class Application {

  private static final Logger LOG = LoggerFactory.getLogger(Application.class);

  public static AppProperties appProperties;
  // Declare dependencies
  public static ArticleDao articleDao ;

  public static void main(String[] args) {
    // Instantiate your dependencies
    int port = getPort();
    if (port >= 0) {
      port(port);
    }

    start();
    LOG.info(format("Listening on port %d", port()));
  }

  public static void start() {

    LOG.info("Init dependencies");

    init();

    LOG.info("Initializing routes");

    path("/api", () -> {

      path("/v1", () -> {

        before("/*", (req, res) -> {
          LOG.info(format("%s %s %s",
              req.ip(),
              req.requestMethod(),
              req.url()));
        });

        get(Path.Web.INDEX, IndexController.serveIndexPage);
        get(Path.Web.ARTICLES, ArticleController.fetchAllArticles);

        notFound((req, res) -> "Not found");
        internalServerError((req, res) -> "Internal server error");

        after("/*", (req, res) -> res.type("application/json"));
      });
    });
  }

  private static void init() {
    appProperties  = new AppProperties();
    articleDao = new ArticleDao(appProperties);
  }

  private static int getPort() {
    String portFromEnv = System.getenv("FM_API_PORT");
    return portFromEnv == null ? -1 : Integer.parseInt(portFromEnv);
  }
}