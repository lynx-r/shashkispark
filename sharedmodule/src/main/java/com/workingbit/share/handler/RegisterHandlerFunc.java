package com.workingbit.share.handler;

import com.workingbit.share.model.Answer;
import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.Payload;
import org.apache.commons.lang3.StringUtils;
import spark.Request;
import spark.Response;

import java.util.Optional;

import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;

/**
 * Created by Aleksey Popryaduhin on 10:52 29/09/2017.
 */
@FunctionalInterface
public interface RegisterHandlerFunc<T extends Payload> extends BaseHandlerFunc {

  default String handleRequest(Request request, Response response, Class<T> clazz) {
    String check = preprocess(request, response);
    if (StringUtils.isNotBlank(check)) {
      return check;
    }
    String session = getOrCreateSession(request, response);
    String json = request.body();
    T data = jsonToData(json, clazz);
    AuthUser authUser = new AuthUser("", session);
    Answer answer = process(data, Optional.of(authUser));
    response.status(answer.getStatusCode());
    return dataToJson(answer);
  }

  Answer process(T data, Optional<AuthUser> token);
}
