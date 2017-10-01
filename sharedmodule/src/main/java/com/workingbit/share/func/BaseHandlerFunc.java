package com.workingbit.share.func;

import com.workingbit.share.model.Answer;
import spark.Request;

import static com.workingbit.share.common.ErrorMessages.INVALID_SIGN_OF_REQUEST;
import static com.workingbit.share.util.JsonUtil.dataToJson;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

/**
 * Created by Aleksey Popryaduhin on 16:37 01/10/2017.
 */
public interface BaseHandlerFunc {

  default String checkSign(Request request) {
    String sign = request.headers("sign");
    System.out.println("sign: " + sign);
    boolean signed = true;
    if (!signed) {
      return dataToJson(Answer.error(HTTP_UNAUTHORIZED, INVALID_SIGN_OF_REQUEST));
    }
    return null;
  }
}
