package com.workingbit.share.handler;

import com.workingbit.share.model.Answer;
import com.workingbit.share.model.Payload;
import org.apache.commons.lang3.StringUtils;
import spark.Request;
import spark.Response;

import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;

/**
 * Created by Aleksey Popryaduhin on 10:52 29/09/2017.
 */
@FunctionalInterface
public interface ModelHandlerFunc<T extends Payload> extends BaseHandlerFunc<T> {

  default String handleRequest(Request request, Response response, boolean secure, Class<T> clazz) {
    String check = preprocess(request, response);
    if (StringUtils.isNotBlank(check)) {
      return check;
    }
    String json = request.body();
    T data = jsonToData(json, clazz);
    Answer answer = createAnswer(request, response, secure, data);
    response.status(answer.getStatusCode());
    return dataToJson(answer);
  }

}
