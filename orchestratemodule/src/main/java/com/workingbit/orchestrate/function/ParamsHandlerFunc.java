package com.workingbit.orchestrate.function;

import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.Answer;
import com.workingbit.share.model.ParamPayload;
import com.workingbit.share.model.Payload;
import com.workingbit.share.model.enumarable.IAuthority;
import org.jetbrains.annotations.NotNull;
import spark.Request;
import spark.Response;

import java.util.Map;

import static com.workingbit.orchestrate.util.AuthRequestUtil.logRequest;
import static com.workingbit.orchestrate.util.AuthRequestUtil.logResponse;
import static com.workingbit.share.util.JsonUtils.dataToJson;

/**
 * Created by Aleksey Popryaduhin on 10:52 29/09/2017.
 */
@FunctionalInterface
public interface ParamsHandlerFunc<T extends Payload> extends BaseHandlerFunc<T> {

  default String handleRequest(@NotNull Request request, @NotNull Response response, @NotNull IAuthority authority) throws RequestException {
    logRequest(request);

    Map<String, String> params = request.params();
    ParamPayload paramPayload = new ParamPayload(params);

    @SuppressWarnings("unchecked")
    Answer answer = getAnswer(request, response, authority, (T) paramPayload);
    response.status(answer.getStatusCode());

    logResponse(request.url(), response, answer.getAuthUser());
    return dataToJson(answer);
  }

  default String handleAuthRequest(@NotNull Request request, @NotNull Response response) throws RequestException {
    logRequest(request);

    Map<String, String> params = request.params();
    ParamPayload paramPayload = new ParamPayload(params);

    @SuppressWarnings("unchecked")
    Answer answer = getAnswerForAuth(request, response, (T) paramPayload);
    response.status(answer.getStatusCode());

    logResponse(request.url(), response, answer.getAuthUser());
    return dataToJson(answer);
  }
}
