package com.workingbit.article.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.Answer;
import com.workingbit.share.model.CreateBoardPayload;
import org.apache.log4j.Logger;

import java.util.Optional;

import static com.workingbit.article.ArticleApplication.appProperties;

/**
 * Created by Aleksey Popryaduhin on 23:59 27/09/2017.
 */
public class BoardRemoteClient {

  private static final Logger logger = Logger.getLogger(BoardRemoteClient.class);

  public Optional<BoardBox> createBoardBox(CreateBoardPayload boardRequest) {
    try {
      HttpResponse<Answer> response = Unirest.post(appProperties.boardResource()).body(boardRequest).asObject(Answer.class);
      if (response.getStatus() == 201) {
        Answer body = response.getBody();
        return Optional.of((BoardBox) body.getBody());
      }
      logger.error("Invalid status " + response.getStatus());
    } catch (UnirestException e) {
      logger.error("Unirest exception", e);
    }
    return Optional.empty();
  }
}
