package com.workingbit.share.handler;

import com.workingbit.share.model.Answer;
import com.workingbit.share.model.ParamPayload;
import com.workingbit.share.model.Payload;
import org.apache.commons.lang3.StringUtils;
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
    String check = preprocess(request, response);
    if (StringUtils.isNotBlank(check)) {
      return check;
    }
    Map<String, String> id = request.params();
    ParamPayload params = new ParamPayload(id);
    Answer answer = createAnswer(request, response, false, (T) params);
    response.status(answer.getStatusCode());
    return dataToJson(answer);
  }
}
