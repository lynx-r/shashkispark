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
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Optional;

import static com.workingbit.article.Application.appProperties;
import static com.workingbit.article.Application.articleDao;
import static com.workingbit.article.util.JsonUtil.dataToJson;
import static com.workingbit.share.common.Utils.getRandomUUID;

/**
 * Created by Aleksey Popryaduhin on 13:58 27/09/2017.
 */
public class ArticleController {

  private static final ObjectMapper mapper = new ObjectMapper();

  public static Route fetchAllArticles = (Request request, Response response) -> {
    String limitStr = request.queryParamOrDefault("limit", "" + appProperties.articlesFetchLimit());
    return dataToJson(articleDao.findAll(Integer.valueOf(limitStr)));
  };

  public static Route createArticleAndBoard = (req, res) -> {
    CreateArticleRequest articleAndBoard = mapper.readValue(req.body(), CreateArticleRequest.class);
    Article article = articleAndBoard.getArticle();
    Utils.setRandomIdAndCreatedAt(article);
    article.setState(EnumArticleState.newadded);
    article.setBoardBoxId(getRandomUUID());
    CreateBoardRequest boardRequest = articleAndBoard.getBoardRequest();
    boardRequest.setBoardBoxId(article.getBoardBoxId());
    CreateArticleResponse createArticleResponse = new CreateArticleResponse();
    boardRequest.setArticleId(article.getId());
    Optional<BoardBox> boardBoxOptional = BoardRemoteClient.createBoardBox(boardRequest);
    if (boardBoxOptional.isPresent()) {
      article.setBoardBoxId(boardBoxOptional.get().getId());
      createArticleResponse.setArticle(article);
      createArticleResponse.setBoard(boardBoxOptional.get());
    } else {
      throw new ArticleServiceError("Unable to create board");
    }
    articleDao.save(article);
    return dataToJson(createArticleResponse);
  };
}
