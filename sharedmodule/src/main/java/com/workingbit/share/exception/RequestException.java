package com.workingbit.share.exception;

import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.model.MessageResponse;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;

/**
 * Created by Aleksey Popryadukhin on 05/05/2018.
 */
@Getter
public class RequestException extends RuntimeException {

  private Logger logger = LoggerFactory.getLogger(RequestException.class);

  private int code;
  private String[] messages;

  public RequestException(int code, String... messages) {
    super(Arrays.toString(messages));
    this.code = code;
    this.messages = messages;
    logger.info(toString());
  }

  public RequestException(MessageResponse message) {
    super(Arrays.toString(message.getMessages()));
    this.code = message.getCode();
    this.messages = message.getMessages();
    logger.info(toString());
  }

  public static RequestException forbidden() {
    return new RequestException(HTTP_FORBIDDEN, ErrorMessages.FORBIDDEN);
  }

  public static RequestException invalidInternalRequest() {
    return new RequestException(HTTP_BAD_REQUEST, ErrorMessages.INVALID_INTERNAL_REQUEST);
  }

  @Override
  public String toString() {
    return "UNAUTHORIZED: RequestException " +
        "code=" + code +
        ", messages=" + (messages == null ? null : Arrays.asList(messages));
  }
}
