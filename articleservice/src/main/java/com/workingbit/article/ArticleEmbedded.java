package com.workingbit.article;

import com.workingbit.article.config.AppProperties;
import com.workingbit.article.config.Authority;
import com.workingbit.article.controller.ArticleController;
import com.workingbit.article.dao.ArticleDao;
import com.workingbit.article.service.ArticleService;
import com.workingbit.orchestrate.OrchestrateModule;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.exception.ExceptionHandler;
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

public class ArticleEmbedded {

  private static final Logger LOG = LoggerFactory.getLogger(ArticleEmbedded.class);

  // Declare dependencies
  public static ArticleDao articleDao;
  public static AppProperties appProperties;
  public static ArticleService articleService;

  static {
    OrchestrateModule.loadModule();

    appProperties = configurationProvider("application.yaml").bind("app", AppProperties.class);

    articleDao = new ArticleDao(appProperties);
    articleService = new ArticleService();
  }

  public static void main(String[] args) {
    port(appProperties.port());

    Logger logger = LoggerFactory.getLogger(ArticleApplication.class);
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
    path("/", () -> get(Authority.HOME.getPath(), ArticleController.home));

    path("/api", () ->
        path("/v1", () -> {
          get(Authority.ARTICLES.getPath(), ArticleController.findAllArticles);
          get(Authority.ARTICLE_BY_ID.getPath(), ArticleController.findArticleById);
          delete(Authority.ARTICLE_BY_ID.getPath(), ArticleController.removeArticleById);
          post(Authority.ARTICLE.getPath(), ArticleController.createArticleAndBoard);
          put(Authority.ARTICLE.getPath(), ArticleController.saveArticle);

          exception(RequestException.class, ExceptionHandler.handle);
          notFound((req, res) -> dataToJson(Answer.error(HTTP_NOT_FOUND, ErrorMessages.RESOURCE_NOT_FOUND)));
          internalServerError((req, res) -> dataToJson(Answer.error(HTTP_INTERNAL_ERROR, ErrorMessages.INTERNAL_SERVER_ERROR)));

          after(Filters.addJsonHeader);
          after(Filters.addGzipHeader);
        })
    );
  }
}