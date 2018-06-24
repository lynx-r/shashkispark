package com.workingbit.orchestrate.function;

import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.Answer;
import com.workingbit.share.model.Payload;
import com.workingbit.share.model.QueryPayload;
import com.workingbit.share.model.enumarable.IAuthority;
import org.jetbrains.annotations.NotNull;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;

import static com.workingbit.orchestrate.util.AuthRequestUtil.logRequest;
import static com.workingbit.orchestrate.util.AuthRequestUtil.logResponse;
import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;

/**
 * Created by Aleksey Popryaduhin on 10:52 29/09/2017.
 */
@FunctionalInterface
public interface ModelHandlerFunc<T extends Payload> extends BaseHandlerFunc<T> {

  default String handleRequest(@NotNull Request request, @NotNull Response response, @NotNull IAuthority path, @NotNull Class<T> clazz) throws RequestException {
    logRequest(request);

    String json = request.body();
    T data = jsonToData(json, clazz);

    QueryParamsMap queryParamsMap = request.queryMap();
    QueryPayload query = new QueryPayload(queryParamsMap);

    Answer answer = getAnswer(request, response, path, data, query);
    response.status(answer.getStatusCode());

    logResponse(request.url(), response, answer.getAuthUser());
    return dataToJson(answer);
  }

  default String handleAuthRequest(@NotNull Request request, @NotNull Response response, @NotNull Class<T> clazz)
      throws RequestException {
    logRequest(request);

    String json = request.body();
    T data = jsonToData(json, clazz);

    Answer answer = getAnswerForAuth(request, response, data);
    response.status(answer.getStatusCode());

    logResponse(request.url(), response, answer.getAuthUser());
    return dataToJson(answer);
  }

}
