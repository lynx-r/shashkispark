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
@JsonTypeName("messages")
@Data
public class MessageResponse implements MessagePayload {
  private String[] messages;
  private int code;

  @JsonCreator
  public MessageResponse(@JsonProperty("code") Integer code,
                         @JsonProperty("messages") String[] messages
  ) {
    this.code = code;
    this.messages = messages;
  }

  public static MessageResponse error(int statusCode, String... messages) {
    return new MessageResponse(statusCode, messages);
  }

  public static MessageResponse ok() {
    return new MessageResponse(HTTP_OK, new String[]{"Request completed"});
  }

  public static MessageResponse created() {
    return new MessageResponse(HTTP_CREATED, new String[]{"Object created"});
  }
}
