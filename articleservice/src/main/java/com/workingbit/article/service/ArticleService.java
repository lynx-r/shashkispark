package com.workingbit.article.service;

import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumArticleStatus;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static com.workingbit.article.ArticleEmbedded.appProperties;
import static com.workingbit.article.ArticleEmbedded.articleDao;
import static com.workingbit.orchestrate.OrchestrateModule.orchestralService;
import static java.net.HttpURLConnection.HTTP_CREATED;

/**
 * Created by Aleksey Popryaduhin on 09:05 28/09/2017.
 */
public class ArticleService {

  private final Logger logger = LoggerFactory.getLogger(ArticleService.class);

  public Optional<CreateArticleResponse> createArticleResponse(CreateArticlePayload articleAndBoard, AuthUser authUser) throws RequestException {
    var article = articleAndBoard.getArticle();
    article.setUserId(authUser.getUserId());

    Optional<Answer> userInfoAnswer = orchestralService.internal(authUser, "userInfoAnswer", authUser);
    userInfoAnswer.ifPresent(answer -> article.setAuthor(((UserInfo) answer.getBody()).getUsername()));

    boolean present = articleDao.findByHru(article.getHumanReadableUrl()).isPresent();
    Utils.setArticleUrlAndIdAndCreatedAt(article, present);
    article.setArticleStatus(EnumArticleStatus.DRAFT);

    CreateBoardPayload boardRequest = articleAndBoard.getBoardRequest();
    boardRequest.setBoardBoxId(article.getSelectedBoardBoxId());
    boardRequest.setArticleId(article.getId());
    boardRequest.setUserId(article.getUserId());

    Optional<Answer> boardBoxAnswer = orchestralService.internal(authUser, "createBoardBoxAnswer", boardRequest, authUser);
    CreateArticleResponse createArticleResponse = CreateArticleResponse.createArticleResponse();
    if (boardBoxAnswer.isPresent() && boardBoxAnswer.get().getStatusCode() == HTTP_CREATED) {
      BoardBox boardBox = (BoardBox) boardBoxAnswer.get().getBody();
      createArticleResponse.setArticle(article);
      createArticleResponse.setBoard(boardBox);

      article.getBoardBoxIds().add(new DomainId(boardBox));
      articleDao.save(article);
    } else {
      logger.error("Unable to create board");
      return Optional.empty();
    }
    return Optional.of(createArticleResponse);
  }

  public Optional<Article> save(Article articleClient) {
    return articleDao.find(articleClient)
        .map(article -> {
          if (article.getArticleStatus().equals(EnumArticleStatus.REMOVED)) {
            return null;
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
          return article;
        });
  }

  public Optional<CreateArticleResponse> importPdn(ImportPdnPayload importPdnPayload, AuthUser authUser) {
    return articleDao.findById(importPdnPayload.getArticleId())
        .map(article -> {
          if (article.getArticleStatus().equals(EnumArticleStatus.REMOVED)) {
            return null;
          }

          Optional<Answer> boardBoxAnswer = orchestralService.internal(authUser, "parsePdnAnswer", importPdnPayload, authUser);
          if (boardBoxAnswer.isPresent() && boardBoxAnswer.get().getStatusCode() == HTTP_CREATED) {
            BoardBox boardBox = (BoardBox) boardBoxAnswer.get().getBody();
            String boardBoxId = boardBox.getId();
            article.setSelectedBoardBoxId(boardBoxId);
            article.getBoardBoxIds().add(new DomainId(boardBox));
            articleDao.save(article);

            CreateArticleResponse articleResponse = CreateArticleResponse.createArticleResponse();
            articleResponse.setArticle(article);
            articleResponse.setBoard(boardBox);
            return articleResponse;
          }
          return null;
        });
  }

  public Optional<Articles> findAll(String limitStr, AuthUser authUser) {
    int limit = appProperties.articlesFetchLimit();
    if (!StringUtils.isBlank(limitStr)) {
      limit = Integer.parseInt(limitStr);
    }
    Articles articles = new Articles();
    List<SimpleFilter> filters = getUserFilter(authUser);
    List<Article> published = articleDao.findPublished(limit, filters);
    articles.setArticles(published);
    return Optional.of(articles);
  }

  public Optional<Article> findById(String articleId) {
    return articleDao.findActiveById(articleId);
  }

  public Optional<Article> findByHru(String articleHru) {
    return articleDao.findByHru(articleHru);
  }

  public Optional<Articles> removeById(String articleId, AuthUser authUser) {
    Optional<Article> articleOpt = articleDao.findActiveById(articleId);
    return articleOpt.map(article -> {
      article.setArticleStatus(EnumArticleStatus.REMOVED);
      articleDao.save(article);

      int limit = appProperties.articlesFetchLimit();
      List<SimpleFilter> filters = getUserFilter(authUser);
      List<Article> published = articleDao.findPublished(limit, filters);
      Articles articles = new Articles();
      articles.setArticles(published);
      return articles;
    });
  }

  private List<SimpleFilter> getUserFilter(AuthUser authUser) {
    List<SimpleFilter> filters = authUser.getFilters();
    if (StringUtils.isNotBlank(authUser.getUserId())) {
      long userIdCount = filters.stream().filter(filter -> filter.getKey().equals("userId")).count();
      if (userIdCount == 0) {
        filters.add(new SimpleFilter("userId", authUser.getUserId(), " = ", "S"));
      }
    }
    return filters;
  }
}
