package com.workingbit.article.article;

import com.workingbit.share.model.CreateArticleRequest;
import spark.Request;
import spark.Response;
import spark.Route;

import static com.workingbit.article.ArticleApplication.appProperties;
import static com.workingbit.share.util.JsonUtil.dataToJson;
import static com.workingbit.share.util.JsonUtil.jsonToData;

/**
 * Created by Aleksey Popryaduhin on 13:58 27/09/2017.
 */
public class ArticleController {

  public static Route findAllArticles = (Request request, Response response) -> {
    String limitStr = request.queryParamOrDefault("limit", "" + appProperties.articlesFetchLimit());
    return dataToJson(ArticleService.getInstance().findAll(Integer.valueOf(limitStr)));
  };

  public static Route createArticleAndBoard = (req, res) ->
      dataToJson(ArticleService.getInstance().createArticleResponse(jsonToData(req.body(), CreateArticleRequest.class)));

  public static Route findArticleById = (req, res) ->
      dataToJson(ArticleService.getInstance().findById(req.params(":id")));
}
