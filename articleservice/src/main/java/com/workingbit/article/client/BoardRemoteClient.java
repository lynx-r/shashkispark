package com.workingbit.article.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.workingbit.article.exception.ArticleServiceException;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.CreateBoardPayload;
import org.apache.log4j.Logger;

import java.util.Optional;

import static com.workingbit.article.ArticleApplication.appProperties;

/**
 * Created by Aleksey Popryaduhin on 23:59 27/09/2017.
 */
public class BoardRemoteClient {

  private static final BoardRemoteClient INSTANCE = new BoardRemoteClient();

  private static final Logger logger = Logger.getLogger(BoardRemoteClient.class);

  public static BoardRemoteClient getInstance() {
    return INSTANCE;
  }

  public Optional<BoardBox> createBoardBox(CreateBoardPayload boardRequest) {
    HttpResponse<BoardBox> boardBoxHttpResponse = null;
    try {
      boardBoxHttpResponse = Unirest.post(appProperties.boardResource()).body(boardRequest).asObject(BoardBox.class);
    } catch (UnirestException e) {
      logger.error("Unirest exception", e);
      return Optional.empty();
    }
    if (boardBoxHttpResponse.getStatus() == 200) {
      return Optional.of(boardBoxHttpResponse.getBody());
    }
    throw new ArticleServiceException("Invalid response " + boardBoxHttpResponse.getStatus());
  }
}
