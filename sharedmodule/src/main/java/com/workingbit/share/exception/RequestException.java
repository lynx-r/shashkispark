package com.workingbit.share.exception;

import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.model.MessageResponse;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static java.net.HttpURLConnection.*;

/**
 * Created by Aleksey Popryadukhin on 05/05/2018.
 */
@Getter
public class RequestException extends RuntimeException {

  private int code;
  private String[] messages;

  public RequestException(int code, String... messages) {
    super(Arrays.toString(messages));
    this.code = code;
    this.messages = messages;
  }

  public RequestException(MessageResponse message) {
    super(Arrays.toString(message.getMessages()));
    this.code = message.getCode();
    this.messages = message.getMessages();
  }

  public static RequestException forbidden() {
    return new RequestException(HTTP_FORBIDDEN, ErrorMessages.FORBIDDEN);
  }

  public static RequestException notFound404() {
    return new RequestException(HTTP_NOT_FOUND, ErrorMessages.ENITY_NOT_FOUND);
  }

  public static RequestException badRequest() {
    return new RequestException(HTTP_BAD_REQUEST, ErrorMessages.INVALID_INTERNAL_REQUEST);
  }

  public static RequestException badRequest(String message) {
    return new RequestException(HTTP_BAD_REQUEST, message);
  }

  public static RequestException noContent() {
    return new RequestException(HTTP_NO_CONTENT, ErrorMessages.NO_CONTENT);
  }

  public static RequestException forbidden(String message) {
    return new RequestException(HTTP_FORBIDDEN, message);
  }

  public static RequestException internalServerError() {
    return new RequestException(HTTP_INTERNAL_ERROR, ErrorMessages.INTERNAL_SERVER_ERROR);
  }

  public static RequestException internalServerError(String message) {
    return new RequestException(HTTP_INTERNAL_ERROR, message);
  }

  public static RequestException internalServerError(String... messages) {
    return new RequestException(HTTP_INTERNAL_ERROR, messages);
  }

  public static RequestException requestException(@NotNull MessageResponse message) {
    return new RequestException(message);
  }

  public static RequestException notFound404(String message) {
    return new RequestException(HTTP_NOT_FOUND, message);
  }

  @Nullable
  @Override
  public String toString() {
    return "UNAUTHORIZED: RequestException " +
        "code=" + code +
        ", messages=" + (messages == null ? null : Arrays.asList(messages));
  }

  public String[] addMessage(String message) {
    for (String m : messages) {
      if (m.equals(message)) {
        return messages;
      }
    }
    String[] strings = Arrays.copyOf(messages, messages.length + 1);
    strings[messages.length] = message;
    return strings;
  }
}
