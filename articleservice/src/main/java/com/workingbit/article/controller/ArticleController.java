package com.workingbit.article.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Aleksey Popryaduhin on 13:58 27/09/2017.
 */
@CrossOrigin(origins = "http://shashki.local")
@RestController
@RequestMapping(value = "/api/v1")
public class ArticleController {

//  private ArticleService articleService;

//  public ArticleController(ArticleService articleService) {
//    this.articleService = articleService;
//  }

  @GetMapping("/home")
  public String home() {
    return "Article. Home, sweet home!";
  }

//  public Mono<CreateArticleResponse> createArticleAndBoard(CreateArticlePayload articleAndBoard) {
//    return articleService.createArticle(articleAndBoard);
//  }

//  public Flux<> findAllArticles(ServerRequest request) {
//    return articleService.findAll(request.queryParam(RequestConstants.LIMIT).orElse("50"), null);
//  }

//  @NotNull
//  public static Route findAllArticles = (req, res) ->
//      ((QueryParamsHandlerFunc<QueryPayload>) (params, token, param) ->
//          Answer.ok(articleService.findAll(params.getQuery().value(RequestConstants.LIMIT), token))
//      ).handleRequest(req, res, Authority.ARTICLES);
//
//  @NotNull
//  public static Route findArticleByHru = (req, res) ->
//      ((ParamsHandlerFunc<ParamPayload>) (params, token, param) ->
//          Answer.ok(articleService.findByHru(params.getParam().get(RequestConstants.HRU), token))
//      ).handleRequest(req, res, Authority.ARTICLE_BY_HRU);
//
//  @NotNull
//  public static Route findCachedArticleByHru = (req, res) ->
//      ((ParamsHandlerFunc<ParamPayload>) (params, token, param) ->
//          Answer.ok(articleService.findByHruCached(params.getParam().get(RequestConstants.HRU), params.getParam().get(RequestConstants.BBID), token))
//      ).handleRequest(req, res, Authority.ARTICLE_BY_HRU_CACHED);
//
//  @NotNull
//  public static Route removeArticleById = (req, res) ->
//      ((ModelHandlerFunc<DomainId>) (params, token, param) ->
//          Answer.ok(articleService.deleteById(params, token))
//      ).handleRequest(req, res,
//          Authority.ARTICLE_DELETE_PROTECTED,
//          DomainId.class);
//
//  @NotNull
//  public static Route createArticleAndBoard = (req, res) ->
//      ((ModelHandlerFunc<CreateArticlePayload>) (data, token, param) ->
//          Answer.created(articleService.createArticle(data, token))
//      ).handleRequest(req, res,
//          Authority.ARTICLE_PROTECTED,
//          CreateArticlePayload.class);
//
//  @NotNull
//  public static Route saveArticle = (req, res) ->
//      ((ModelHandlerFunc<Article>) (article, token, param) ->
//          Answer.created(articleService.save((Article) article, token))
//      ).handleRequest(req, res,
//          Authority.ARTICLE_PROTECTED,
//          Article.class);
//
//  @NotNull
//  public static Route cacheArticle = (req, res) ->
//      ((ModelHandlerFunc<Article>) (article, token, param) ->
//          Answer.created(articleService.cache((Article) article, token))
//      ).handleRequest(req, res,
//          Authority.ARTICLE_CACHE,
//          Article.class);
//
//  @NotNull
//  public static Route importPdn = (req, res) ->
//      ((ModelHandlerFunc<ImportPdnPayload>) (article, token, param) -> {
//        try {
//          return Answer.created(articleService.importPdn((ImportPdnPayload) article, token));
//        } catch (RequestException e) {
//          String[] messages = e.addMessage(ErrorMessages.UNABLE_TO_PARSE_PDN);
//          return Answer.error(HTTP_BAD_REQUEST, messages);
//        }
//      }).handleRequest(req, res,
//          Authority.ARTICLE_IMPORT_PDN_PROTECTED,
//          ImportPdnPayload.class);
//
//  @NotNull
//  public static Route subscribe = (req, res) ->
//      ((ModelHandlerFunc<Subscriber>) (params, token, param) ->
//          Answer.ok(articleService.subscribe(params))
//      ).handleRequest(req, res,
//          Authority.ARTICLES,
//          Subscriber.class);
}
