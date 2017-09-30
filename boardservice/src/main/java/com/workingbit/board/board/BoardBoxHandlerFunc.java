package com.workingbit.board.board;

import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.Answer;
import spark.Request;
import spark.Response;

import static com.workingbit.share.util.JsonUtil.dataToJson;
import static com.workingbit.share.util.JsonUtil.jsonToData;

/**
 * Created by Aleksey Popryaduhin on 10:52 29/09/2017.
 */
@FunctionalInterface
public interface BoardBoxHandlerFunc {

  default String handleRequest(Request request, Response response) {
    String json = request.body();
    BoardBox data = jsonToData(json, BoardBox.class);
    Answer processed = process(data);
    response.status(processed.getCode());
    return dataToJson(processed);
  }

  Answer process(BoardBox data);
}
