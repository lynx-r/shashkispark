package com.workingbit.article.service;

import com.workingbit.share.client.ShareRemoteClient;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.*;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.workingbit.article.ArticleEmbedded.articleDao;
import static com.workingbit.share.util.Utils.getRandomUUID;

/**
 * Created by Aleksey Popryaduhin on 09:05 28/09/2017.
 */
public class ArticleService {

  private final static ShareRemoteClient shareRemoteClient = ShareRemoteClient.Singleton.getInstance();

  private final Logger logger = LoggerFactory.getLogger(ArticleService.class);

  public Optional<CreateArticleResponse> createArticleResponse(CreateArticlePayload articleAndBoard, Optional<AuthUser> token) {
    if (!token.isPresent()) {
      return Optional.empty();
    }
    AuthUser authUser = token.get();
    Article article = articleAndBoard.getArticle();
    article.setUserId(authUser.getUserId());

    ShareRemoteClient.Singleton.getInstance().userInfo(authUser)
        .ifPresent(userInfo ->
            article.setAuthor(userInfo.getUsername())
        );

    boolean present = findById(article.getTitle()).isPresent();
    Utils.setArticleIdAndCreatedAt(article, present);

    article.setState(EnumArticleState.NEW_ADDED);
    article.setBoardBoxId(getRandomUUID());

    CreateBoardPayload boardRequest = articleAndBoard.getBoardRequest();
    boardRequest.setBoardBoxId(article.getBoardBoxId());

    CreateArticleResponse createArticleResponse = CreateArticleResponse.createArticleResponse();
    boardRequest.setArticleId(article.getId());

    Optional<BoardBox> boardBoxOptional = shareRemoteClient.createBoardBox(boardRequest, authUser);
    if (boardBoxOptional.isPresent()) {
      article.setBoardBoxId(boardBoxOptional.get().getId());
      createArticleResponse.setArticle(article);
      createArticleResponse.setBoard(boardBoxOptional.get());
    } else {
      return Optional.empty();
    }
    articleDao.save(article);
    return Optional.of(createArticleResponse);
  }

  public Optional<Article> save(Article article, Optional<AuthUser> token) {
    return token.map(authUser -> {
      if (!isOwn(authUser, article)) {
        logger.error(ErrorMessages.NOT_OWNER);
        return null;
      }
      String title = article.getTitle().trim();
      article.setTitle(title);
      String content = article.getContent().trim();
      article.setContent(content);
      if (StringUtils.isBlank(article.getContent())) {
        article.setContent("Новая статья про шашки от " + article.getAuthor());
      }
      articleDao.save(article);
      return article;
    });
  }

  public Optional<Articles> findAll(String limitStr, Optional<AuthUser> token) {
    Integer limit = null;
    if (!StringUtils.isBlank(limitStr)) {
      limit = Integer.valueOf(limitStr);
    }
    Articles articles = new Articles();
    articles.setArticles(articleDao.findAll(limit));
    return Optional.of(articles);
  }

  public Optional<Article> findById(String articleId) {
    return articleDao.findById(articleId);
  }

  private boolean isOwn(AuthUser authUser, Article article) {
    // authUser == null when board is created from notation pdn
    return authUser == null || authUser.getUserId().equals(article.getUserId());
  }
}
