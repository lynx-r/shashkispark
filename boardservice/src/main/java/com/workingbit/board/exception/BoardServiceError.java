package com.workingbit.board.exception;

import com.workingbit.share.common.Log;

/**
 * Created by Aleksey Popryaduhin on 08:25 11/08/2017.
 */
public class BoardServiceError extends Error {

  public BoardServiceError(String message) {
    super(message);
    Log.error(message);
  }
}
