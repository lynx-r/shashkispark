package com.workingbit.share.exception;

import com.workingbit.share.common.ErrorMessages;
import lombok.Getter;
import lombok.Setter;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

/**
 * Created by Aleksey Popryadukhin on 13/05/2018.
 */
@Getter
@Setter
public class DaoException extends RuntimeException {

  private int code;

  public DaoException(int code, String message) {
    super(message);
    this.code = code;
  }

  public static DaoException notFound() {
    return new DaoException(HTTP_NOT_FOUND, ErrorMessages.ENITY_NOT_FOUND);
  }

  public static DaoException unableToSave(String message) {
    return new DaoException(0, message);
  }

  public static DaoException unableToSave() {
    return new DaoException(0, ErrorMessages.UNABLE_TO_SAVE_ENTITY);
  }
}
