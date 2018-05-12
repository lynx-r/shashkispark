package com.workingbit.article.controller;

import com.workingbit.article.config.Authority;
import com.workingbit.orchestrate.function.ModelHandlerFunc;
import com.workingbit.orchestrate.function.ParamsHandlerFunc;
import com.workingbit.orchestrate.function.QueryParamsHandlerFunc;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.common.RequestConstants;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.model.*;
import spark.Route;

import static com.workingbit.article.ArticleEmbedded.articleService;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

/**
 * Created by Aleksey Popryaduhin on 13:58 27/09/2017.
 */
public class ArticleController {

  public static Route home = (req, res) -> "Article. Home, sweet home!";

  public static Route findAllArticles = (req, res) ->
      ((QueryParamsHandlerFunc<QueryPayload>) (params, token) ->
          articleService.findAll(params.getQuery().value(RequestConstants.LIMIT), token)
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_NOT_FOUND, ErrorMessages.UNABLE_TO_GET_ARTICLES))
      ).handleRequest(req, res, Authority.ARTICLES);

  public static Route findArticleByHru = (req, res) ->
      ((ParamsHandlerFunc<ParamPayload>) (params, token) ->
          articleService.findByHru(params.getParam().get(RequestConstants.HRU), token)
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_NOT_FOUND, ErrorMessages.ARTICLE_WITH_ID_NOT_FOUND))
      ).handleRequest(req, res, Authority.ARTICLE_BY_HRU);

  public static Route findCachedArticleByHru = (req, res) ->
      ((ParamsHandlerFunc<ParamPayload>) (params, token) ->
          articleService.findByHruCached(params.getParam().get(RequestConstants.HRU), params.getParam().get(RequestConstants.BBID), token)
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_NOT_FOUND, ErrorMessages.ARTICLE_WITH_ID_NOT_FOUND))
      ).handleRequest(req, res, Authority.ARTICLE_BY_HRU_CACHED);

  public static Route removeArticleById = (req, res) ->
      ((ModelHandlerFunc<DomainId>) (params, token) ->
          articleService.removeById(params, token)
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_NOT_FOUND, ErrorMessages.ARTICLE_WITH_ID_NOT_FOUND))
      ).handleRequest(req, res,
          Authority.ARTICLE_REMOVE_PROTECTED,
          DomainId.class);

  public static Route createArticleAndBoard = (req, res) ->
      ((ModelHandlerFunc<CreateArticlePayload>) (data, token) ->
          articleService.createArticle(data, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_CREATE_ARTICLE))
      ).handleRequest(req, res,
          Authority.ARTICLE_PROTECTED,
          CreateArticlePayload.class);

  public static Route saveArticle = (req, res) ->
      ((ModelHandlerFunc<Article>) (article, token) ->
          articleService.save((Article) article)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_SAVE_ARTICLE))
      ).handleRequest(req, res,
          Authority.ARTICLE_PROTECTED,
          Article.class);

  public static Route cacheArticle = (req, res) ->
      ((ModelHandlerFunc<Article>) (article, token) ->
          articleService.cache((Article) article, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_SAVE_ARTICLE))
      ).handleRequest(req, res,
          Authority.ARTICLE_CACHE,
          Article.class);

  public static Route importPdn = (req, res) ->
      ((ModelHandlerFunc<ImportPdnPayload>) (article, token) ->
          articleService.importPdn((ImportPdnPayload) article, token)
              .map(Answer::created)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_IMPORT_NOTATION))
      ).handleRequest(req, res,
          Authority.ARTICLE_IMPORT_PDN_PROTECTED,
          ImportPdnPayload.class);
}
