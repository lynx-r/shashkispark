package com.workingbit.share.handler;

import com.workingbit.share.model.Answer;
import com.workingbit.share.model.Payload;
import com.workingbit.share.model.QueryPayload;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;

import static com.workingbit.share.util.JsonUtils.dataToJson;

/**
 * Created by Aleksey Popryaduhin on 16:27 01/10/2017.
 */
@FunctionalInterface
public interface QueryParamsHandlerFunc<T extends Payload> extends BaseHandlerFunc<T> {

  default String handleRequest(Request request, Response response) {
    logRequest(request);

    QueryParamsMap queryParamsMap = request.queryMap();
    QueryPayload query = new QueryPayload(queryParamsMap);

    @SuppressWarnings("unchecked")
    Answer answer = getAnswer(request, response, false, (T) query);
    response.status(answer.getStatusCode());

    return dataToJson(answer);
  }
}
