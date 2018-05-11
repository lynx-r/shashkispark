package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.workingbit.share.util.Utils;
import lombok.Data;
import lombok.NoArgsConstructor;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

@NoArgsConstructor
@Data
public class Answer {

  private String id;
  private int statusCode;
  private Payload body;
  private AuthUser authUser;
  private MessageResponse message;

  @JsonCreator
  private Answer(@JsonProperty("body") Payload body, @JsonProperty("messages") MessageResponse message) {
    this.body = body;
    this.message = message;
    this.id = Utils.getRandomID();
  }

  public static Answer ok(Payload body) {
    return new Answer(body, MessageResponse.ok())
        .statusCode(HTTP_OK);
  }

  public static Answer created(Payload body) {
    return new Answer(body, MessageResponse.created())
        .statusCode(HTTP_CREATED);
  }

  public static Answer error(int statusCode, String... messages) {
    return new Answer(null, MessageResponse.error(statusCode, messages))
        .statusCode(statusCode);
  }

  public static Answer withMessageResponse(MessageResponse message) {
    return new Answer(null, message);
  }

  public static Answer empty() {
    return new Answer(null, null)
        .statusCode(HTTP_OK);
  }

  public Answer statusCode(int statusCode) {
    setStatusCode(statusCode);
    return this;
  }

  public Answer message(int code, String... messages) {
    setMessage(new MessageResponse(code, messages));
    return this;
  }
}