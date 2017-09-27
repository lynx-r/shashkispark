package com.workingbit.article.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.workingbit.article.exception.ArticleServiceError;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.CreateBoardRequest;
import org.apache.log4j.Logger;

import java.util.Optional;

import static com.workingbit.article.Application.appProperties;

/**
 * Created by Aleksey Popryaduhin on 23:59 27/09/2017.
 */
public class BoardRemoteClient {

  private static Logger logger = Logger.getLogger(BoardRemoteClient.class);

  public static Optional<BoardBox> createBoardBox(CreateBoardRequest boardRequest) throws UnirestException {
    HttpResponse<BoardBox> boardBoxHttpResponse = Unirest.post(appProperties.boardResource()).body(boardRequest).asObject(BoardBox.class);
    if (boardBoxHttpResponse.getStatus() == 201) {
      return Optional.of(boardBoxHttpResponse.getBody());
    }
    throw new ArticleServiceError("Invalid response " + boardBoxHttpResponse.getStatus());
  }
}
