package com.workingbit.article.controller;

import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.common.RequestConstants;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.handler.ModelHandlerFunc;
import com.workingbit.share.handler.ParamsHandlerFunc;
import com.workingbit.share.handler.QueryParamsHandlerFunc;
import com.workingbit.share.model.Answer;
import com.workingbit.share.model.CreateArticlePayload;
import spark.Route;

import static com.workingbit.article.ArticleApplication.articleService;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

/**
 * Created by Aleksey Popryaduhin on 13:58 27/09/2017.
 */
public class ArticleController {

  public static Route findAllArticles = (req, res) ->
      ((QueryParamsHandlerFunc) params ->
          articleService.findAll(params.value(RequestConstants.LIMIT))
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_NOT_FOUND, ErrorMessages.UNABLE_TO_GET_ARTICLES))
      ).handleRequest(req, res);

  public static Route findArticleById = (req, res) ->
      ((ParamsHandlerFunc) params ->
          articleService.findById(params.get(RequestConstants.ID))
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_NOT_FOUND, ErrorMessages.ARTICLE_WITH_ID_NOT_FOUND))
      ).handleRequest(req, res);

  public static Route createArticleAndBoard = (req, res) ->
      ((ModelHandlerFunc<CreateArticlePayload>) articlePayload ->
          articleService.createArticleResponse(articlePayload)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_CREATE_ARTICLE))
      ).handleRequest(req, res, CreateArticlePayload.class);

  public static Route saveArticle = (req, res) ->
      ((ModelHandlerFunc<Article>) article ->
          articleService.save((Article) article)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_SAVE_ARTICLE))
      ).handleRequest(req, res, Article.class);
}
