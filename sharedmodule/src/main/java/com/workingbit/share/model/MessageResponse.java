package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * MessageResponse
 */
@JsonTypeName("message")
@Data
public class MessageResponse implements MessagePayload {
  private String message;
  private int code;

  @JsonCreator
  public MessageResponse(@JsonProperty("code") Integer code,
                         @JsonProperty("message") String message
  ) {
    this.code = code;
    this.message = message;
  }

  @Override
  public void setCode(int code) {
    this.code = code;
  }

  public static MessageResponse error(int statusCode, String message) {
    return new MessageResponse(statusCode, message);
  }

  public static MessageResponse ok() {
    return new MessageResponse(HTTP_OK, "Request completed");
  }

  public static MessageResponse created() {
    return new MessageResponse(HTTP_CREATED, "Object created");
  }
}
