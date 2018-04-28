package com.workingbit.article.controller;

import com.workingbit.article.config.Path;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.common.RequestConstants;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.handler.ModelHandlerFunc;
import com.workingbit.share.handler.ParamsHandlerFunc;
import com.workingbit.share.handler.QueryParamsHandlerFunc;
import com.workingbit.share.model.Answer;
import com.workingbit.share.model.CreateArticlePayload;
import com.workingbit.share.model.ParamPayload;
import com.workingbit.share.model.QueryPayload;
import spark.Route;

import static com.workingbit.article.ArticleEmbedded.articleService;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

/**
 * Created by Aleksey Popryaduhin on 13:58 27/09/2017.
 */
public class ArticleController {

  public static Route home = (req, res) -> "Home, sweet home!";

  public static Route findAllArticles = (req, res) ->
      ((QueryParamsHandlerFunc<QueryPayload>) (params, token) ->
          articleService.findAll(params.getQuery().value(RequestConstants.LIMIT))
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_NOT_FOUND, ErrorMessages.UNABLE_TO_GET_ARTICLES))
      ).handleRequest(req, res, Path.ARTICLES);

  public static Route findArticleById = (req, res) ->
      ((ParamsHandlerFunc<ParamPayload>) (params, token) ->
          articleService.findById(params.getParam().get(RequestConstants.ID))
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_NOT_FOUND, ErrorMessages.ARTICLE_WITH_ID_NOT_FOUND))
      ).handleRequest(req, res, Path.ARTICLE_BY_ID);

  public static Route createArticleAndBoard = (req, res) ->
      ((ModelHandlerFunc<CreateArticlePayload>) (data, token) ->
          articleService.createArticleResponse(data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_CREATE_ARTICLE))
      ).handleRequest(req, res,
          Path.ARTICLE.setSecure(true).setRoles(Path.Constants.ARTICLE_SECURE_ROLES),
          CreateArticlePayload.class);

  public static Route saveArticle = (req, res) ->
      ((ModelHandlerFunc<Article>) (article, token) ->
          articleService.save((Article) article, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_SAVE_ARTICLE))
      ).handleRequest(req, res,
          Path.ARTICLE.setSecure(true).setRoles(Path.Constants.ARTICLE_SECURE_ROLES),
          Article.class);
}
