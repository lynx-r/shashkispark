package com.workingbit.share.func;

import com.workingbit.share.model.Answer;
import org.apache.commons.lang3.StringUtils;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;

import static com.workingbit.share.util.JsonUtil.dataToJson;

/**
 * Created by Aleksey Popryaduhin on 16:27 01/10/2017.
 */
@FunctionalInterface
public interface QueryParamsHandlerFunc  extends BaseHandlerFunc{

  default String handleRequest(Request request, Response response) {
    String check = checkSign(request);
    if (StringUtils.isNotBlank(check)) {
      return check;
    }
    QueryParamsMap queryParamsMap = request.queryMap();
    Answer processed = process(queryParamsMap);
    response.status(processed.getCode());
    return dataToJson(processed);
  }

  Answer process(QueryParamsMap data);
}
