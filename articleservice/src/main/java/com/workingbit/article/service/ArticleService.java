package com.workingbit.article.service;

import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.exception.DaoException;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumArticleStatus;
import com.workingbit.share.model.enumarable.EnumAuthority;
import com.workingbit.share.model.enumarable.EnumEditBoardBoxMode;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.workingbit.article.ArticleEmbedded.*;
import static com.workingbit.orchestrate.OrchestrateModule.orchestralService;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

/**
 * Created by Aleksey Popryaduhin on 09:05 28/09/2017.
 */
public class ArticleService {

  private final Logger logger = LoggerFactory.getLogger(ArticleService.class);

  public CreateArticleResponse createArticle(CreateArticlePayload articleAndBoard, AuthUser authUser) throws RequestException {
    var article = articleAndBoard.getArticle();
    article.setTitle(article.getTitle().trim());
    String humanReadableUrl = article.getHumanReadableUrl().trim();
    if (humanReadableUrl.length() < 4) {
      throw RequestException.badRequest("В заголовке минимум 4 символа");
    }
    if (humanReadableUrl.length() > 2000) {
      throw RequestException.badRequest("В заголовке макимум 2000 символов");
    }
    article.setHumanReadableUrl(humanReadableUrl);
    article.setUserId(authUser.getUserId());

    Optional<Answer> userInfoAnswer = orchestralService.internal(authUser, "userInfoAnswer", authUser);
    userInfoAnswer.ifPresent(answer -> article.setAuthor(((UserInfo) answer.getBody()).getUsername()));

    boolean present = true;
    try {
      articleDao.findByHru(article.getHumanReadableUrl());
    } catch (DaoException e) {
      present = false;
    }
    Utils.setArticleUrlAndIdAndCreatedAt(article, present);
    article.setArticleStatus(EnumArticleStatus.DRAFT);

    CreateBoardPayload boardRequest = articleAndBoard.getBoardRequest();
    boardRequest.setBoardBoxId(article.getSelectedBoardBoxId());
    boardRequest.setArticleId(article.getDomainId());
    boardRequest.setUserId(article.getUserId());
    boardRequest.setIdInArticle(1);
    if (boardRequest.isFillBoard()) {
      boardRequest.setEditMode(EnumEditBoardBoxMode.EDIT);
    } else {
      boardRequest.setEditMode(EnumEditBoardBoxMode.PLACE);
    }

    Optional<Answer> boardBoxAnswer = orchestralService.internal(authUser, "createBoardBoxAnswer", boardRequest, authUser);
    CreateArticleResponse createArticleResponse = CreateArticleResponse.createArticleResponse();
    if (boardBoxAnswer.isPresent() && boardBoxAnswer.get().getStatusCode() == HTTP_CREATED) {
      BoardBox boardBox = (BoardBox) boardBoxAnswer.get().getBody();
      createArticleResponse.setArticle(article);
      createArticleResponse.setBoard(boardBox);

      article.setBoardBoxCount(1);
      articleDao.save(article);
    } else {
      logger.error("Unable to create board");
      throw RequestException.internalServerError(ErrorMessages.UNABLE_TO_CREATE_BOARD);
    }

    // add new article to cache
    Articles all = findAll("50", authUser);
    all.add(article);
    articleStoreService.putAllArticles(all);
    return createArticleResponse;
  }

  public Article save(Article articleClient, AuthUser token) {
    var article = articleDao.find(articleClient);
    if (article.getArticleStatus().equals(EnumArticleStatus.REMOVED)) {
      throw RequestException.badRequest(ErrorMessages.ARTICLE_IS_DELETED);
    }
    if (StringUtils.isNotBlank(articleClient.getTitle())) {
      String title = articleClient.getTitle().trim();
      article.setTitle(title);
    }
    if (StringUtils.isNotBlank(articleClient.getContent())) {
      String content = articleClient.getContent().trim();
      article.setContent(content);
    }
    article.setArticleStatus(articleClient.getArticleStatus());
    article.setSelectedBoardBoxId(articleClient.getSelectedBoardBoxId());
    articleDao.save(article);
    articleStoreService.remove(article.getId());

    // replace edited article
    Articles all = findAll("50", token);
    all.replace(article);
    articleStoreService.putAllArticles(all);
    return article;
  }

  public Article cache(Article articleClient, AuthUser token) {
    var article = articleDao.find(articleClient);
    if (article.getArticleStatus().equals(EnumArticleStatus.REMOVED)) {
      return null;
    }
    article.setSelectedBoardBoxId(articleClient.getSelectedBoardBoxId());
    articleStoreService.put(token.getUserSession(), article);
    return article;
  }

  public CreateArticleResponse importPdn(ImportPdnPayload importPdnPayload, AuthUser authUser) {
    var article = articleDao.findById(importPdnPayload.getArticleId());
    if (article.getArticleStatus().equals(EnumArticleStatus.REMOVED)) {
      throw RequestException.badRequest(ErrorMessages.ARTICLE_IS_DELETED);
    }

    Optional<Answer> boardBoxAnswer;
    int boardBoxCount = article.getBoardBoxCount() + 1;
    if (StringUtils.isBlank(importPdnPayload.getPdn())) {
      CreateBoardPayload boardRequest = CreateBoardPayload.createBoardPayload();
      article.setSelectedBoardBoxId(DomainId.getRandomID());
      boardRequest.setBoardBoxId(article.getSelectedBoardBoxId());
      boardRequest.setArticleId(article.getDomainId());
      boardRequest.setUserId(article.getUserId());
      boardRequest.setRules(importPdnPayload.getRules());
      boardRequest.setIdInArticle(boardBoxCount);
      boardRequest.setEditMode(EnumEditBoardBoxMode.PLACE);
      boardBoxAnswer = orchestralService.internal(authUser, "createBoardBoxAnswer", boardRequest, authUser);
    } else {
      importPdnPayload.setIdInArticle(boardBoxCount);
      importPdnPayload.setEditMode(EnumEditBoardBoxMode.PLACE);
      boardBoxAnswer = orchestralService.internal(authUser, "parsePdnAnswer", importPdnPayload, authUser);
    }

    if (boardBoxAnswer.isPresent() && boardBoxAnswer.get().getStatusCode() == HTTP_CREATED) {
      BoardBox boardBox = (BoardBox) boardBoxAnswer.get().getBody();
      article.setSelectedBoardBoxId(boardBox.getDomainId());
      article.setBoardBoxCount(boardBoxCount);
      articleDao.save(article);

      CreateArticleResponse articleResponse = CreateArticleResponse.createArticleResponse();
      articleResponse.setArticle(article);
      articleResponse.setBoard(boardBox);
      return articleResponse;
    }
    throw RequestException.notFound404();
  }

  public Articles findAll(String limitStr, AuthUser authUser) {
    if (EnumAuthority.hasAuthorAuthorities(authUser)) {
      return findAllDb(limitStr, authUser);
    }

    return articleStoreService
        .getAllArticles()
        .orElseGet(() -> findAllDb(limitStr, authUser));
  }

  public Article findByHru(String articleHru, AuthUser token) {
    try {
      Article byHru = articleDao.findByHru(articleHru);
      articleStoreService.put(token.getUserSession(), byHru);
      return byHru;
    } catch (DaoException e) {
      throw RequestException.notFound404();
    }
  }

  public Article findByHruCached(String articleHru, String selectedBoardBoxId, AuthUser token) {
    return articleStoreService
        .get(token.getUserSession(), articleHru, selectedBoardBoxId)
        .orElseGet(() -> findByHru(articleHru, token));
  }

  public Articles removeById(DomainId articleId, AuthUser authUser) {
    articleStoreService.remove(articleId.getId());
    Article article = articleDao.findById(articleId);
    article.setArticleStatus(EnumArticleStatus.REMOVED);
    articleDao.save(article);

    int limit = appProperties.articlesFetchLimit();
    List<Article> published;
    try {
      published = articleDao.findPublishedBy(limit, authUser);
    } catch (DaoException e) {
      if (e.getCode() != HTTP_NOT_FOUND) {
        throw RequestException.internalServerError();
      }
      published = new ArrayList<>();
    }
    Articles articles = new Articles();
    articles.setArticles(published);
    return articles;
  }

  private Articles findAllDb(String limitStr, AuthUser authUser) {
    int limit = appProperties.articlesFetchLimit();
    if (!StringUtils.isBlank(limitStr)) {
      limit = Integer.parseInt(limitStr);
    }
    Articles articles = new Articles();
    try {
      List<Article> published;
      if (authUser.getUserId() == null) {
        published = articleDao.findPublished(limit);
      } else {
        published = articleDao.findPublishedBy(limit, authUser);
      }
      articles.setArticles(published);
      articleStoreService.putAllArticles(articles);
      return articles;
    } catch (DaoException e) {
      if (e.getCode() == HTTP_NOT_FOUND) {
        logger.info(e.getMessage());
        return articles;
      }
      throw RequestException.badRequest();
    }
  }
}
