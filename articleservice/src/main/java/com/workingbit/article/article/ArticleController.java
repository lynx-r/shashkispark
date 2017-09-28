package com.workingbit.article.article;

import spark.Request;
import spark.Response;
import spark.Route;

import static com.workingbit.article.Application.appProperties;
import static com.workingbit.article.util.JsonUtil.dataToJson;

/**
 * Created by Aleksey Popryaduhin on 13:58 27/09/2017.
 */
public class ArticleController {

  public static Route fetchAllArticles = (Request request, Response response) -> {
    String limitStr = request.queryParamOrDefault("limit", "" + appProperties.articlesFetchLimit());
    return dataToJson(ArticleService.getInstance().findAll(Integer.valueOf(limitStr)));
  };

  public static Route createArticleAndBoard = (req, res) ->
      dataToJson(ArticleService.getInstance().createArticleResponse(req.body()));
}
