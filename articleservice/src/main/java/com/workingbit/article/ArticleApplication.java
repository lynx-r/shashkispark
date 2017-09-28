package com.workingbit.article;

import com.workingbit.article.article.ArticleController;
import com.workingbit.article.article.ArticleDao;
import com.workingbit.article.config.AppProperties;
import com.workingbit.article.util.Path;
import com.workingbit.article.util.UnirestUtil;
import com.workingbit.share.util.Filters;
import com.workingbit.share.util.SparkUtils;
import org.apache.log4j.Logger;

import static com.workingbit.share.common.Config4j.configurationProvider;
import static com.workingbit.share.common.CorsConfig.enableCors;
import static spark.Spark.*;

public class ArticleApplication {

  private static final Logger LOG = Logger.getLogger(ArticleApplication.class);

  // Declare dependencies
  public static ArticleDao articleDao;
  public static AppProperties appProperties;

  static {
    appProperties = configurationProvider().bind("app", AppProperties.class);

    articleDao = new ArticleDao(appProperties);
  }

  public static void main(String[] args) {
    port(appProperties.port());
    start();
  }

  static void start() {
    Logger logger = Logger.getLogger(ArticleApplication.class);
    SparkUtils.createServerWithRequestLog(logger);

    UnirestUtil.configureSerialization();

    LOG.info("Initializing routes");

    enableCors(appProperties.origin().toString(), appProperties.methods(), appProperties.headers());

    path("/api", () ->

        path("/v1", () -> {

          get(Path.ARTICLES, ArticleController.findAllArticles);
          get(Path.ARTICLE_BY_ID, ArticleController.findArticleById);
          post(Path.ARTICLE, ArticleController.createArticleAndBoard);

          notFound((req, res) -> "Not found");
          internalServerError((req, res) -> "Internal server error");

          after(Filters.addJsonHeader);
          after(Filters.addGzipHeader);
        }));
  }
}