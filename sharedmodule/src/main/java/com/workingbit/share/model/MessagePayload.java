package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by Aleksey Popryaduhin on 16:13 01/10/2017.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = MessageResponse.class, name = "message"),
})
public interface MessagePayload {
  int getCode();

  void setCode(int statusCode);

  String getMessage();

  void setMessage(String message);

  default MessagePayload message(String message) {
    setMessage(message);
    return this;
  }
}
