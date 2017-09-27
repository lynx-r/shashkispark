package com.workingbit.article;

import com.workingbit.article.article.ArticleController;
import com.workingbit.article.article.ArticleDao;
import com.workingbit.article.config.AppProperties;
import com.workingbit.article.util.Path;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.debug.DebugScreen.enableDebugScreen;

public class Application {

  public static AppProperties appProperties = new AppProperties();
  // Declare dependencies
  public static ArticleDao articleDao = new ArticleDao(appProperties);

  public static void main(String[] args) {

    // Instantiate your dependencies
//        bookDao = new BookDao();
//        userDao = new UserDao();

    // Configure Spark
    port(4567);
//    staticFiles.location("/v1");
//    staticFiles.expireTime(600L);
    enableDebugScreen();

    // Set up before-filters (called before each get/post)

    // Set up routes
//    get(Path.Web.INDEX, IndexController.serveIndexPage);
    get(Path.Web.ARTICLES, ArticleController.fetchAllArticles);

    //Set up after-filters (called after each get/post)
//    after("*", Filters.addGzipHeader);

  }

}