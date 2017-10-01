package com.workingbit.article.article;

import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.func.ModelHandlerFunc;
import com.workingbit.share.func.ParamsHandlerFunc;
import com.workingbit.share.func.QueryParamsHandlerFunc;
import com.workingbit.share.model.Answer;
import com.workingbit.share.model.CreateArticlePayload;
import spark.Route;

import static com.workingbit.article.ArticleApplication.articleService;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * Created by Aleksey Popryaduhin on 13:58 27/09/2017.
 */
public class ArticleController {

  public static Route findAllArticles = (req, res) ->
      ((QueryParamsHandlerFunc) params ->
          articleService.findAll(Integer.valueOf(params.value("limit")))
              .map(Answer::okArticleList)
              .orElse(Answer.error(HTTP_BAD_REQUEST, "Unable to get articles"))
      ).handleRequest(req, res);

  public static Route createArticleAndBoard = (req, res) ->
      ((ModelHandlerFunc<CreateArticlePayload>) articlePayload ->
          articleService.createArticleResponse(articlePayload)
              .map(Answer::okArticleCreate)
              .orElse(Answer.error(HTTP_BAD_REQUEST, "Unable to create an article"))
      ).handleRequest(req, res, CreateArticlePayload.class);

  public static Route findArticleById = (req, res) ->
      ((ParamsHandlerFunc) params ->
          articleService.findById(params.get(":id"))
              .map(Answer::okArticle)
              .orElse(Answer.error(HTTP_BAD_REQUEST, String.format("Article with id %s not found", params.get(":id"))))
      ).handleRequest(req, res);

  public static Route saveArticle = (req, res) ->
      ((ModelHandlerFunc<Article>) article ->
          articleService.save((Article) article)
              .map(Answer::okArticle)
              .orElse(Answer.error(HTTP_BAD_REQUEST, "Unable to save article"))
      ).handleRequest(req, res, Article.class);
}
