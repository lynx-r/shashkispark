//package com.workingbit.article.service;
//
//import com.workingbit.article.repo.ReactiveArticleRepository;
//import com.workingbit.share.common.ErrorMessages;
//import com.workingbit.share.domain.impl.BoardBox;
//import com.workingbit.share.exception.DaoException;
//import com.workingbit.share.exception.RequestException;
//import com.workingbit.share.model.*;
//import com.workingbit.share.model.enumarable.EnumArticleStatus;
//import com.workingbit.share.model.enumarable.EnumEditBoardBoxMode;
//import com.workingbit.share.util.Utils;
//import org.jetbrains.annotations.NotNull;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//
//import static com.workingbit.orchestrate.OrchestrateModule.orchestralService;
//import static java.net.HttpURLConnection.HTTP_CREATED;
//
///**
// * Created by Aleksey Popryaduhin on 09:05 28/09/2017.
// */
//@Service
//public class ArticleService {
//
//  private final Logger logger = LoggerFactory.getLogger(ArticleService.class);
//
//  private ReactiveArticleRepository articleRepository;
//
//  public ArticleService(ReactiveArticleRepository articleRepository) {
//    this.articleRepository = articleRepository;
//  }
//
//  @NotNull
//  public CreateArticleResponse createArticle(@NotNull CreateArticlePayload articleAndBoard) throws RequestException {
//    var article = articleAndBoard.getArticle();
//    article.setTitle(article.getTitle().trim());
//    String humanReadableUrl = article.getHumanReadableUrl().trim();
//    if (humanReadableUrl.length() < 4) {
//      throw RequestException.badRequest("В заголовке минимум 4 символа");
//    }
//    if (humanReadableUrl.length() > 2000) {
//      throw RequestException.badRequest("В заголовке макимум 2000 символов");
//    }
//    article.setHumanReadableUrl(humanReadableUrl);
////    article.setUserId(authUser.getUserId());
//
//    boolean present = true;
//    try {
//      articleRepository.findByHru(article.getHumanReadableUrl());
//    } catch (DaoException e) {
//      present = false;
//    }
//    Utils.setArticleUrlAndIdAndCreatedAt(article, present);
//
////    Optional<Answer> userInfoAnswer = orchestralService.internal(authUser, "userInfoAnswer", authUser);
////    userInfoAnswer.ifPresent(answer -> article.setAuthor(((UserInfo) answer.getBody()).getUsername()));
//
//    articleRepository.save(article);
//
//    article.setArticleStatus(EnumArticleStatus.DRAFT);
//
//    CreateBoardPayload boardRequest = articleAndBoard.getBoardRequest();
//    boardRequest.setBoardBox(article.getSelectedBoardBox());
//    boardRequest.setArticle(article.getDomainId());
//    boardRequest.setUserId(article.getUserId());
//    boardRequest.setIdInArticle(1);
//    boardRequest.setRules(boardRequest.getRules());
//    if (boardRequest.isFillBoard()) {
//      boardRequest.setEditMode(EnumEditBoardBoxMode.EDIT);
//    } else {
//      boardRequest.setEditMode(EnumEditBoardBoxMode.PLACE);
//    }
//
//    Optional<Answer> boardBoxAnswer = orchestralService.internal(authUser, "createBoardBoxAnswer", boardRequest, authUser);
//    CreateArticleResponse createArticleResponse = CreateArticleResponse.createArticleResponse();
//    if (boardBoxAnswer.isPresent() && boardBoxAnswer.get().getStatusCode() == HTTP_CREATED) {
//      BoardBox boardBox = (BoardBox) boardBoxAnswer.get().getBody();
//      createArticleResponse.setArticle(article);
//      createArticleResponse.setBoard(boardBox);
//
//      article.setBoardBoxCount(1);
//      articleRepository.save(article);
//    } else {
//      logger.error("Unable to createWithoutRoot board");
//      throw RequestException.internalServerError(ErrorMessages.UNABLE_TO_CREATE_BOARD);
//    }
//
//    // add new article to cache
////    Articles all = findAll("50", authUser);
////    all.add(article);
////    articleStoreService.putAllArticles(all);
//    return createArticleResponse;
//  }
//
////  public Article save(@NotNull Article articleClient, @NotNull AuthUser token) {
////    var article = articleRepository.find(articleClient);
////    if (article.getArticleStatus().equals(EnumArticleStatus.REMOVED)) {
////      throw RequestException.badRequest(ErrorMessages.ARTICLE_IS_DELETED);
////    }
////    if (StringUtils.isNotBlank(articleClient.getTitle())) {
////      String title = articleClient.getTitle().trim();
////      article.setTitle(title);
////    }
////    if (StringUtils.isNotBlank(articleClient.getContent())) {
////      String content = articleClient.getContent().trim();
////      article.setContent(content);
////    }
////    if (StringUtils.isNotBlank(articleClient.getIntro())) {
////      String intro = articleClient.getIntro().trim();
////      article.setIntro(intro);
////    }
////    article.setBoardBoxCount(articleClient.getBoardBoxCount());
////    article.setArticleStatus(articleClient.getArticleStatus());
////    article.setSelectedBoardBox(articleClient.getSelectedBoardBox());
////    articleRepository.save(article);
//////    articleStoreService.remove(article);
////
////    if (EnumArticleStatus.PUBLISHED.equals(article.getArticleStatus())) {
////      subscriberService.notifySubscribersAboutArticle(articleClient);
////    }
////
////    // replace edited article
////    replaceArticleInAllArticlesCache(article, token);
////    return article;
////  }
////
////  @Nullable
////  public Article cache(@NotNull Article articleClient, @NotNull AuthUser token) {
////    var article = articleRepository.find(articleClient);
////    if (article.getArticleStatus().equals(EnumArticleStatus.REMOVED)) {
////      return null;
////    }
////    article.setSelectedBoardBox(articleClient.getSelectedBoardBox());
//////    articleStoreService.put(token.getUserSession(), article);
////    return article;
////  }
////
////  @NotNull
////  public CreateArticleResponse importPdn(@NotNull ImportPdnPayload importPdnPayload, @NotNull AuthUser authUser) {
////    var article = articleRepository.findById(importPdnPayload.getArticle());
////    if (article.getArticleStatus().equals(EnumArticleStatus.REMOVED)) {
////      throw RequestException.badRequest(ErrorMessages.ARTICLE_IS_DELETED);
////    }
////
////    Optional<Answer> boardBoxAnswer;
////    int boardBoxCount = article.getBoardBoxCount() + 1;
////    if (StringUtils.isBlank(importPdnPayload.getPdn())) {
////      CreateBoardPayload boardRequest = new CreateBoardPayload();
////      article.setSelectedBoardBox(DomainId.getRandomID());
////      boardRequest.setBoardBox(article.getSelectedBoardBox());
////      boardRequest.setArticle(article.getDomainId());
////      boardRequest.setUserId(article.getUserId());
////      boardRequest.setRules(importPdnPayload.getRules());
////      boardRequest.setIdInArticle(boardBoxCount);
////      boardRequest.setEditMode(EnumEditBoardBoxMode.PLACE);
////      boardBoxAnswer = orchestralService.internal(authUser, "createBoardBoxAnswer", boardRequest, authUser);
////    } else {
////      importPdnPayload.setIdInArticle(boardBoxCount);
////      importPdnPayload.setEditMode(EnumEditBoardBoxMode.PLACE);
////      boardBoxAnswer = orchestralService.internal(authUser, "parsePdnAnswer", importPdnPayload, authUser);
////    }
////
////    if (boardBoxAnswer.isPresent() && boardBoxAnswer.get().getStatusCode() == HTTP_CREATED) {
////      BoardBox boardBox = (BoardBox) boardBoxAnswer.get().getBody();
////      article.setSelectedBoardBox(boardBox.getDomainId());
////      article.setBoardBoxCount(boardBoxCount);
////      articleRepository.save(article);
//////      articleStoreService.put(authUser.getUserSession(), article);
////
////      CreateArticleResponse articleResponse = CreateArticleResponse.createArticleResponse();
////      articleResponse.setArticle(article);
////      articleResponse.setBoard(boardBox);
////      return articleResponse;
////    }
////    throw RequestException.notFound404();
////  }
////
////  public Articles findAll(@NotNull String limitStr, @NotNull AuthUser authUser) {
//////    if (!authUser.getFilters().isEmpty()) {
////    int limit = appProperties.articlesFetchLimit();
////    if (!StringUtils.isBlank(limitStr)) {
////      limit = Integer.parseInt(limitStr);
////    }
////    Articles articles = new Articles();
////    try {
////      if (authUser.getUserId() == null) {
////        articles = articleRepository.findPublished(limit);
////      } else {
////        articles = articleRepository.findPublishedBy(limit, authUser);
////      }
//////      articleStoreService.putAllArticles(articles);
////      return articles;
////    } catch (DaoException e) {
////      if (e.getCode() == HTTP_NOT_FOUND) {
////        logger.info(e.getMessage());
////        return articles;
////      }
////      throw RequestException.badRequest();
////    }
//////    }
//////    boolean secure = EnumAuthority.hasAuthorAuthorities(authUser);
//////    String userId = authUser.getUserId() != null ? authUser.getUserId().getId() : "";
//////    return findAllDb(limitStr, authUser);
////  }
////
////  public Article findByHru(String articleHru, @NotNull AuthUser token) {
////    try {
////      return articleRepository.findByHru(articleHru);
////    } catch (DaoException e) {
////      throw RequestException.notFound404();
////    }
////  }
////
////  public Article findByHruCached(String articleHru, String selectedBoardBox, @NotNull AuthUser token) {
////    return findByHru(articleHru, token);
////  }
////
////  @NotNull
////  public Articles deleteById(DomainId article, @NotNull AuthUser authUser) {
////    Article article = articleRepository.findById(article);
////    Optional<ResultPayload> resultPayload = orchestralService.deleteBoardBoxesByArticleId(article.getDomainId(), authUser);
////    if (resultPayload.isPresent()) {
////      boolean boardBoxesDeleted = resultPayload.get().isSuccess();
////      if (!boardBoxesDeleted) {
////        throw RequestException.internalServerError(ErrorMessages.UNABLE_TO_REMOVE_BOARDBOXES);
////      }
////    }
////    articleRepository.delete(article.getDomainId());
//////    articleStoreService.remove(article);
////
////    int limit = appProperties.articlesFetchLimit();
////    Articles published;
////    try {
////      published = articleRepository.findPublishedBy(limit, authUser);
////    } catch (DaoException e) {
////      if (e.getCode() != HTTP_NOT_FOUND) {
////        throw RequestException.internalServerError();
////      }
////      published = new Articles();
////    }
//////    articleStoreService.putAllArticles(published);
////    return published;
////  }
////
////  public Subscribed subscribe(Subscriber subscriber) {
////    // todo move in another place
////    return subscriberService.subscribe(subscriber);
////  }
////
////  private void replaceArticleInAllArticlesCache(Article article, @NotNull AuthUser token) {
////    Articles all = findAll(appProperties.articlesFetchLimit().toString(), token);
////    all.replace(article);
//////    articleStoreService.putAllArticles(all);
////  }
//}
