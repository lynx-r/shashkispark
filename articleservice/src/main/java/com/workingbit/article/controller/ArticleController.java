package com.workingbit.article.controller;

import com.workingbit.article.config.Authority;
import com.workingbit.orchestrate.function.ModelHandlerFunc;
import com.workingbit.orchestrate.function.ParamsHandlerFunc;
import com.workingbit.orchestrate.function.QueryParamsHandlerFunc;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.common.RequestConstants;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.*;
import spark.Route;

import java.util.Arrays;

import static com.workingbit.article.ArticleEmbedded.articleService;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * Created by Aleksey Popryaduhin on 13:58 27/09/2017.
 */
public class ArticleController {

  public static Route home = (req, res) -> "Article. Home, sweet home!";

  public static Route findAllArticles = (req, res) ->
      ((QueryParamsHandlerFunc<QueryPayload>) (params, token) ->
          Answer.ok(articleService.findAll(params.getQuery().value(RequestConstants.LIMIT), token))
      ).handleRequest(req, res, Authority.ARTICLES);

  public static Route findArticleByHru = (req, res) ->
      ((ParamsHandlerFunc<ParamPayload>) (params, token) ->
          Answer.ok(articleService.findByHru(params.getParam().get(RequestConstants.HRU), token))
      ).handleRequest(req, res, Authority.ARTICLE_BY_HRU);

  public static Route findCachedArticleByHru = (req, res) ->
      ((ParamsHandlerFunc<ParamPayload>) (params, token) ->
          Answer.ok(articleService.findByHruCached(params.getParam().get(RequestConstants.HRU), params.getParam().get(RequestConstants.BBID), token))
      ).handleRequest(req, res, Authority.ARTICLE_BY_HRU_CACHED);

  public static Route removeArticleById = (req, res) ->
      ((ModelHandlerFunc<DomainId>) (params, token) ->
          Answer.ok(articleService.removeById(params, token))
      ).handleRequest(req, res,
          Authority.ARTICLE_REMOVE_PROTECTED,
          DomainId.class);

  public static Route createArticleAndBoard = (req, res) ->
      ((ModelHandlerFunc<CreateArticlePayload>) (data, token) ->
          Answer.created(articleService.createArticle(data, token))
      ).handleRequest(req, res,
          Authority.ARTICLE_PROTECTED,
          CreateArticlePayload.class);

  public static Route saveArticle = (req, res) ->
      ((ModelHandlerFunc<Article>) (article, token) ->
          Answer.created(articleService.save((Article) article))
      ).handleRequest(req, res,
          Authority.ARTICLE_PROTECTED,
          Article.class);

  public static Route cacheArticle = (req, res) ->
      ((ModelHandlerFunc<Article>) (article, token) ->
          Answer.created(articleService.cache((Article) article, token))
      ).handleRequest(req, res,
          Authority.ARTICLE_CACHE,
          Article.class);

  public static Route importPdn = (req, res) ->
      ((ModelHandlerFunc<ImportPdnPayload>) (article, token) -> {
        try {
          return Answer.created(articleService.importPdn((ImportPdnPayload) article, token));
        } catch (RequestException e) {
          String[] messages = e.addMessage(ErrorMessages.UNABLE_TO_PARSE_PDN);
          return Answer.error(HTTP_BAD_REQUEST, messages);
        }
      }).handleRequest(req, res,
          Authority.ARTICLE_IMPORT_PDN_PROTECTED,
          ImportPdnPayload.class);
}
