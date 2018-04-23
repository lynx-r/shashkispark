package com.workingbit.board.exception;

/**
 * Created by Aleksey Popryaduhin on 08:25 11/08/2017.
 */
public class BoardServiceException extends RuntimeException {

  public BoardServiceException() {
  }

  public BoardServiceException(String message) {
    super(message);
  }
}
