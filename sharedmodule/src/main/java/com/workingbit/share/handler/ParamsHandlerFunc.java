package com.workingbit.share.handler;

import com.workingbit.share.model.Answer;
import org.apache.commons.lang3.StringUtils;
import spark.Request;
import spark.Response;

import java.util.Map;

import static com.workingbit.share.util.JsonUtils.dataToJson;

/**
 * Created by Aleksey Popryaduhin on 10:52 29/09/2017.
 */
@FunctionalInterface
public interface ParamsHandlerFunc extends BaseHandlerFunc {

  default String handleRequest(Request request, Response response) {
    String check = preprocess(request, response);
    if (StringUtils.isNotBlank(check)) {
      return check;
    }
    Map<String, String> id = request.params();
    Answer processed = process(id);
    response.status(processed.getStatusCode());
    return dataToJson(processed);
  }

  Answer process(Map<String, String> data);
}
