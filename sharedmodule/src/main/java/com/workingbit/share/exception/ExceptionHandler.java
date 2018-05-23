package com.workingbit.share.exception;

import com.workingbit.share.model.Answer;
import org.jetbrains.annotations.NotNull;

import static com.workingbit.share.util.JsonUtils.dataToJson;

/**
 * Created by Aleksey Popryadukhin on 05/05/2018.
 */
public class ExceptionHandler {

  @NotNull
  public static spark.ExceptionHandler<? super RequestException> handle = (e, req, res) -> {
    res.status(e.getCode());
    res.body(dataToJson(Answer.error(e.getCode(), e.getMessages())));
  };
}
