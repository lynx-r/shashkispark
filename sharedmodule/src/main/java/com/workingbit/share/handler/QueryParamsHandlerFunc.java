package com.workingbit.share.handler;

import com.workingbit.share.model.Answer;
import org.apache.commons.lang3.StringUtils;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;

import static com.workingbit.share.util.JsonUtils.dataToJson;

/**
 * Created by Aleksey Popryaduhin on 16:27 01/10/2017.
 */
@FunctionalInterface
public interface QueryParamsHandlerFunc extends BaseHandlerFunc {

  default String handleRequest(Request request, Response response) {
    String check = preprocess(request, response);
    if (StringUtils.isNotBlank(check)) {
      return check;
    }
    QueryParamsMap queryParamsMap = request.queryMap();
    Answer processed = process(queryParamsMap);
    response.status(processed.getStatusCode());
    return dataToJson(processed);
  }

  Answer process(QueryParamsMap data);
}
