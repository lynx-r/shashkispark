package com.workingbit.share.handler;

import com.workingbit.share.model.Answer;
import com.workingbit.share.model.EnumSecureRole;
import com.workingbit.share.model.ParamPayload;
import com.workingbit.share.model.Payload;
import spark.Request;
import spark.Response;

import java.util.Map;

import static com.workingbit.share.util.JsonUtils.dataToJson;

/**
 * Created by Aleksey Popryaduhin on 10:52 29/09/2017.
 */
@FunctionalInterface
public interface ParamsHandlerFunc<T extends Payload> extends BaseHandlerFunc<T> {

  default String handleRequest(Request request, Response response) {
    logRequest(request);

    Map<String, String> params = request.params();
    String secureStr = params.get(":secure");
    boolean secure;
    try {
      secure = secureStr.toUpperCase().equals(EnumSecureRole.AUTHOR.name());
    } catch (Exception ignore) {
      secure = false;
    }
    ParamPayload paramPayload = new ParamPayload(params);

    @SuppressWarnings("unchecked")
    Answer answer = getAnswer(request, response, secure, (T) paramPayload);
    response.status(answer.getStatusCode());

    return dataToJson(answer);
  }
}
