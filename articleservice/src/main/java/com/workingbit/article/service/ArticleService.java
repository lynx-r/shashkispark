package com.workingbit.article.service;

import com.workingbit.article.client.BoardRemoteClient;
import com.workingbit.share.common.Utils;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.CreateArticlePayload;
import com.workingbit.share.model.CreateArticleResponse;
import com.workingbit.share.model.CreateBoardPayload;
import com.workingbit.share.model.EnumArticleState;

import java.util.List;
import java.util.Optional;

import static com.workingbit.article.ArticleApplication.articleDao;
import static com.workingbit.share.common.Utils.getRandomUUID;

/**
 * Created by Aleksey Popryaduhin on 09:05 28/09/2017.
 */
public class ArticleService {

  public Optional<CreateArticleResponse> createArticleResponse(CreateArticlePayload articleAndBoard) {
    Article article = articleAndBoard.getArticle();
    Utils.setRandomIdAndCreatedAt(article);
    article.setState(EnumArticleState.newadded);
    article.setBoardBoxId(getRandomUUID());
    CreateBoardPayload boardRequest = articleAndBoard.getBoardRequest();
    boardRequest.setBoardBoxId(article.getBoardBoxId());
    CreateArticleResponse createArticleResponse = new CreateArticleResponse();
    boardRequest.setArticleId(article.getId());
    Optional<BoardBox> boardBoxOptional = BoardRemoteClient.getInstance().createBoardBox(boardRequest);
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

  public Optional<List<Article>> findAll(Integer limit) {
    return Optional.of(articleDao.findAll(limit));
  }

  public Optional<Article> findById(String articleId) {
    return articleDao.findByKey(articleId);
  }
}
