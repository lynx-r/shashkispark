package com.workingbit.board.board;

import com.workingbit.share.domain.impl.BoardBox;
import spark.Request;

import static com.workingbit.share.util.JsonUtil.dataToJson;
import static com.workingbit.share.util.JsonUtil.jsonToData;

/**
 * Created by Aleksey Popryaduhin on 10:52 29/09/2017.
 */
@FunctionalInterface
public interface BoardHandlerFunc {

  default String handleRequest(Request request) {
    String json = request.body();
    BoardBox data = jsonToData(json, BoardBox.class);
    Object processed = process(data);
    return dataToJson(processed);
  }

  Object process(BoardBox data);
}
