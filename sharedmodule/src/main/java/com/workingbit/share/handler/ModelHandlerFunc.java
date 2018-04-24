package com.workingbit.share.handler;

import com.workingbit.share.model.Answer;
import com.workingbit.share.model.IPath;
import com.workingbit.share.model.Payload;
import spark.Request;
import spark.Response;

import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;

/**
 * Created by Aleksey Popryaduhin on 10:52 29/09/2017.
 */
@FunctionalInterface
public interface ModelHandlerFunc<T extends Payload> extends BaseHandlerFunc<T> {

  default String handleRequest(Request request, Response response, IPath path, Class<T> clazz) {
    logRequest(request);

    String json = request.body();
    T data = jsonToData(json, clazz);

    Answer answer = getAnswer(request, response, path, data);
    response.status(answer.getStatusCode());

    logResponse(response, answer.getAuthUser());
    return dataToJson(answer);
  }

}
