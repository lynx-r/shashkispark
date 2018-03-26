package com.workingbit.article.service;

import com.workingbit.article.client.BoardRemoteClient;
import com.workingbit.share.util.Utils;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

import static com.workingbit.article.ArticleApplication.articleDao;
import static com.workingbit.share.util.Utils.getRandomUUID;

/**
 * Created by Aleksey Popryaduhin on 09:05 28/09/2017.
 */
public class ArticleService {

  private final static BoardRemoteClient boardRemoteClient = new BoardRemoteClient();

  public Optional<CreateArticleResponse> createArticleResponse(CreateArticlePayload articleAndBoard) {
    Article article = articleAndBoard.getArticle();
    boolean present = findById(article.getTitle()).isPresent();
    Utils.setArticleIdAndCreatedAt(article, present);
    article.setState(EnumArticleState.newadded);
    article.setBoardBoxId(getRandomUUID());
    CreateBoardPayload boardRequest = articleAndBoard.getBoardRequest();
    boardRequest.setBoardBoxId(article.getBoardBoxId());
    CreateArticleResponse createArticleResponse = new CreateArticleResponse();
    boardRequest.setArticleId(article.getId());
    Optional<BoardBox> boardBoxOptional = boardRemoteClient.createBoardBox(boardRequest);
    if (boardBoxOptional.isPresent()) {
      article.setBoardBoxId(boardBoxOptional.get().getId());
      createArticleResponse.setArticle(article);
      createArticleResponse.setBoard(boardBoxOptional.get());
    } else {
      return Optional.empty();
    }
    save(article);
    return Optional.of(createArticleResponse);
  }

  public Optional<Article> save(Article article) {
    articleDao.save(article);
    return Optional.of(article);
  }

  public Optional<Articles> findAll(String limitStr) {
    Integer limit = null;
    if (!StringUtils.isBlank(limitStr)) {
      limit = Integer.valueOf(limitStr);
    }
    Articles articles = new Articles();
    articles.addAll(articleDao.findAll(limit));
    return Optional.of(articles);
  }

  public Optional<Article> findById(String articleId) {
    return articleDao.findByKey(articleId);
  }
}
