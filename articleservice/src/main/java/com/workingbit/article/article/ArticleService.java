package com.workingbit.article.article;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workingbit.article.client.BoardRemoteClient;
import com.workingbit.article.exception.ArticleServiceError;
import com.workingbit.share.common.Utils;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.CreateArticleRequest;
import com.workingbit.share.model.CreateArticleResponse;
import com.workingbit.share.model.CreateBoardRequest;
import com.workingbit.share.model.EnumArticleState;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.workingbit.article.Application.articleDao;
import static com.workingbit.share.common.Utils.getRandomUUID;

/**
 * Created by Aleksey Popryaduhin on 09:05 28/09/2017.
 */
public class ArticleService {

  private static final ArticleService INSTANCE = new ArticleService();

  private static final Logger logger = Logger.getLogger(BoardRemoteClient.class);

  private static final ObjectMapper mapper = new ObjectMapper();

  public static ArticleService getInstance() {
    return INSTANCE;
  }

  public CreateArticleResponse createArticleResponse(String body) {
    CreateArticleRequest articleAndBoard;
    try {
      articleAndBoard = mapper.readValue(body, CreateArticleRequest.class);
    } catch (IOException e) {
      logger.error(e);
      throw new ArticleServiceError(e.getMessage());
    }
    Article article = articleAndBoard.getArticle();
    Utils.setRandomIdAndCreatedAt(article);
    article.setState(EnumArticleState.newadded);
    article.setBoardBoxId(getRandomUUID());
    CreateBoardRequest boardRequest = articleAndBoard.getBoardRequest();
    boardRequest.setBoardBoxId(article.getBoardBoxId());
    CreateArticleResponse createArticleResponse = new CreateArticleResponse();
    boardRequest.setArticleId(article.getId());
    Optional<BoardBox> boardBoxOptional = BoardRemoteClient.getInstance().createBoardBox(boardRequest);
    if (boardBoxOptional.isPresent()) {
      article.setBoardBoxId(boardBoxOptional.get().getId());
      createArticleResponse.setArticle(article);
      createArticleResponse.setBoard(boardBoxOptional.get());
    } else {
      throw new ArticleServiceError("Unable to create board");
    }
    articleDao.save(article);
    return createArticleResponse;
  }

  public List<Article> findAll(Integer limit) {
    return articleDao.findAll(limit);
  }
}
