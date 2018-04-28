package com.workingbit.article.service;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumArticleStatus;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.workingbit.article.ArticleEmbedded.articleDao;
import static com.workingbit.orchestrate.OrchestrateModule.orchestralService;
import static com.workingbit.share.util.Utils.getRandomUUID;

/**
 * Created by Aleksey Popryaduhin on 09:05 28/09/2017.
 */
public class ArticleService {

  private final Logger logger = LoggerFactory.getLogger(ArticleService.class);

  public Optional<CreateArticleResponse> createArticleResponse(CreateArticlePayload articleAndBoard, AuthUser authUser) throws RequestException {
    var article = articleAndBoard.getArticle();
    article.setUserId(authUser.getUserId());

    Optional<Answer> userInfoAnswer = orchestralService.internal(authUser,
        (au, internalKey) -> orchestralService.userInfoAnswer(au.setInternalKey(internalKey))
    );
    userInfoAnswer.ifPresent(answer -> article.setAuthor(((UserInfo) answer.getBody()).getUsername()));

    boolean present = articleDao.findById(article.getTitle()).isPresent();
    Utils.setArticleIdAndCreatedAt(article, present);

    article.setArticleStatus(EnumArticleStatus.NEW_ADDED);
    article.setBoardBoxId(getRandomUUID());

    CreateBoardPayload boardRequest = articleAndBoard.getBoardRequest();
    boardRequest.setBoardBoxId(article.getBoardBoxId());

    CreateArticleResponse createArticleResponse = CreateArticleResponse.createArticleResponse();
    boardRequest.setArticleId(article.getId());

    Optional<BoardBox> boardBoxOptional = orchestralService.createBoardBox(boardRequest, authUser);
    if (boardBoxOptional.isPresent()) {
      article.setBoardBoxId(boardBoxOptional.get().getId());
      createArticleResponse.setArticle(article);
      createArticleResponse.setBoard(boardBoxOptional.get());
    } else {
      logger.error("Unable to create board");
      return Optional.empty();
    }
    articleDao.save(article);
    return Optional.of(createArticleResponse);
  }

  public Optional<Article> save(Article articleClient) {
    return articleDao.find(articleClient)
        .map(article -> {
          if (article.getArticleStatus().equals(EnumArticleStatus.REMOVED)) {
            return null;
          }
          String title = articleClient.getTitle().trim();
          article.setTitle(title);
          String content = articleClient.getContent().trim();
          article.setContent(content);
          article.setArticleStatus(articleClient.getArticleStatus());
          articleDao.save(article);
          return article;
        });
  }

  public Optional<Articles> findAll(String limitStr, AuthUser authUser) {
    int limit = 50;
    if (!StringUtils.isBlank(limitStr)) {
      limit = Integer.parseInt(limitStr);
    }
    Articles articles = new Articles();
    List<SimpleFilter> filters = authUser.getFilters();
    if (StringUtils.isNotBlank(authUser.getUserId())) {
      long userIdCount = filters.stream().filter(filter -> filter.getKey().equals("userId")).count();
      if (userIdCount == 0) {
        filters.add(new SimpleFilter("userId", authUser.getUserId()));
      }
    }
    List<Article> published = articleDao.findPublished(limit, filters);
    articles.setArticles(published);
    return Optional.of(articles);
  }

  private boolean isValidFilters(String filter, Map<String, AttributeValue> values) {
    return true;
  }

  public Optional<Article> findById(String articleId) {
    return articleDao.findActiveById(articleId);
  }

  public Optional<Articles> removeById(String articleId) {
    Optional<Article> articleOpt = articleDao.findActiveById(articleId);
    return articleOpt.map(article -> {
      article.setArticleStatus(EnumArticleStatus.REMOVED);
      articleDao.save(article);

      List<Article> published = articleDao.findPublished(50);
      Articles articles = new Articles();
      articles.setArticles(published);
      return articles;
    });
  }
}
