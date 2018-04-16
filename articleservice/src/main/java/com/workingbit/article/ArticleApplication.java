package com.workingbit.article;

import com.workingbit.article.config.AppProperties;
import com.workingbit.article.controller.ArticleController;
import com.workingbit.article.dao.ArticleDao;
import com.workingbit.article.service.ArticleService;
import com.workingbit.article.util.Path;
import com.workingbit.share.service.SecureUserService;
import com.workingbit.share.util.Filters;
import com.workingbit.share.util.SparkUtils;
import com.workingbit.share.util.UnirestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.workingbit.share.common.Config4j.configurationProvider;
import static com.workingbit.share.common.CorsConfig.enableCors;
import static spark.Spark.*;

public class ArticleApplication {

  private static final Logger LOG = LoggerFactory.getLogger(ArticleApplication.class);

  // Declare dependencies
  public static ArticleDao articleDao;
  public static AppProperties appProperties;
  public static ArticleService articleService;
  public static SecureUserService secureUserService;

  static {
    appProperties = configurationProvider().bind("app", AppProperties.class);

    articleDao = new ArticleDao(appProperties);
    articleService = new ArticleService();
    secureUserService = new SecureUserService();
  }

  public static void main(String[] args) {
    port(appProperties.port());
    start();
  }

  public static void start() {
    Logger logger = LoggerFactory.getLogger(ArticleApplication.class);
    SparkUtils.createServerWithRequestLog(logger);

    UnirestUtil.configureSerialization();

    LOG.info("Initializing routes");

    enableCors(appProperties.origin().toString(), appProperties.methods(), appProperties.headers());
    establishRoutes();
  }

  private static void establishRoutes() {
    path("/api", () -> {
      path("/v1", () -> {
        get(Path.ARTICLES, ArticleController.findAllArticles);
        get(Path.ARTICLE_BY_ID, ArticleController.findArticleById);
        post(Path.ARTICLE, ArticleController.createArticleAndBoard);
        put(Path.ARTICLE, ArticleController.saveArticle);

        configure();
      });
      path("/secure", () -> {
        get(Path.REGISTER, ArticleController.register);
        get(Path.AUTHORIZE, ArticleController.authorize);
        get(Path.AUTHENTICATE, ArticleController.authenticate);

        configure();
      });
    });
  }

  private static void configure() {
    notFound((req, res) -> "Not found");
    internalServerError((req, res) -> "Internal server message");

    after(Filters.addJsonHeader);
    after(Filters.addGzipHeader);
  }
}