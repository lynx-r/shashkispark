package com.workingbit.share.handler;

import com.workingbit.share.model.Answer;
import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.Payload;
import org.apache.commons.lang3.StringUtils;
import spark.Request;
import spark.Response;

import java.util.Optional;

import static com.workingbit.share.common.RequestConstants.ACCESS_TOKEN;
import static com.workingbit.share.common.RequestConstants.JSESSIONID;
import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;

/**
 * Created by Aleksey Popryaduhin on 10:52 29/09/2017.
 */
@FunctionalInterface
public interface ModelHandlerFunc<T extends Payload> extends BaseHandlerFunc {

  default String handleRequest(Request request, Response response, boolean secure, Class<T> clazz) {
    String check = preprocess(request, response);
    if (StringUtils.isNotBlank(check)) {
      return check;
    }
    String json = request.body();
    T data = jsonToData(json, clazz);
    String token = request.headers(ACCESS_TOKEN);
    String session = request.headers(JSESSIONID);
    Answer answer;
    if (secure) {
      answer = secureCheck(data, token, session);
    } else {
      answer = process(data, Optional.of(new AuthUser("", session)));
    }
    response.status(answer.getStatusCode());
    return dataToJson(answer);
  }

  default Answer secureCheck(T data, String token, String session) {
    Optional<AuthUser> authUser = isAuthenticated(token, session);
    if (authUser.isPresent()) {
      return process(data, authUser);
    } else {
      return Answer.error(HTTP_FORBIDDEN, "Вы не авторизованы");
    }
  }

  Answer process(T data, Optional<AuthUser> token);
}
