package com.workingbit.article.service;

import com.workingbit.share.client.ShareRemoteClient;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.*;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.workingbit.article.ArticleApplication.articleDao;
import static com.workingbit.share.util.Utils.getRandomString;

/**
 * Created by Aleksey Popryaduhin on 09:05 28/09/2017.
 */
public class ArticleService {

  private final static ShareRemoteClient shareRemoteClient = new ShareRemoteClient();

  private final Logger logger = LoggerFactory.getLogger(ArticleService.class);

  public Optional<CreateArticleResponse> createArticleResponse(CreateArticlePayload articleAndBoard, Optional<AuthUser> token) {
    if (!token.isPresent()) {
      return Optional.empty();
    }
    Article article = articleAndBoard.getArticle();
    boolean present = findById(article.getTitle()).isPresent();
    Utils.setArticleIdAndCreatedAt(article, present);
    article.setState(EnumArticleState.newadded);
    article.setBoardBoxId(getRandomString());
    CreateBoardPayload boardRequest = articleAndBoard.getBoardRequest();
    boardRequest.setBoardBoxId(article.getBoardBoxId());
    CreateArticleResponse createArticleResponse = CreateArticleResponse.createArticleResponse();
    boardRequest.setArticleId(article.getId());
    Optional<BoardBox> boardBoxOptional = shareRemoteClient.createBoardBox(boardRequest, token.get());
    if (boardBoxOptional.isPresent()) {
      article.setBoardBoxId(boardBoxOptional.get().getId());
      createArticleResponse.setArticle(article);
      createArticleResponse.setBoard(boardBoxOptional.get());
    } else {
      return Optional.empty();
    }
    save(article, token);
    return Optional.of(createArticleResponse);
  }

  public Optional<Article> save(Article article, Optional<AuthUser> token) {
    articleDao.save(article);
    return Optional.of(article);
  }

  public Optional<Articles> findAll(String limitStr) {
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
}
