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

/**
 * Created by Aleksey Popryaduhin on 16:27 01/10/2017.
 */
@FunctionalInterface
public interface QueryParamsHandlerFunc<T extends Payload> extends BaseHandlerFunc<T> {

  default String handleRequest(@NotNull Request request, @NotNull Response response, @NotNull IAuthority path) throws RequestException {
    logRequest(request);

    QueryParamsMap queryParamsMap = request.queryMap();
    QueryPayload query = new QueryPayload(queryParamsMap);

    @SuppressWarnings("unchecked")
    Answer answer = getAnswer(request, response, path, (T) query, query);
    response.status(answer.getStatusCode());

    logResponse(request.url(), response, answer.getAuthUser());
    return dataToJson(answer);
  }
}
