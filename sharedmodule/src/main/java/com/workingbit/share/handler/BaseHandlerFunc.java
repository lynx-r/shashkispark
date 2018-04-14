package com.workingbit.share.handler;

import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.common.RequestConstants;
import com.workingbit.share.model.Answer;
import org.apache.commons.lang3.StringUtils;
import spark.Request;
import spark.Response;

import static com.workingbit.share.common.ApiConstants.VK_API_KEY_ENV;
import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.Utils.encode;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * Created by Aleksey Popryaduhin on 16:37 01/10/2017.
 */
public interface BaseHandlerFunc {

  String _JSESSIONID = "JSESSIONID";

  default String preprocess(Request request, Response response) {
    String sign = request.headers(RequestConstants.SIGN);
    String signRequest = request.headers(RequestConstants.SIGN_REQUEST);
    String jsessionid = request.cookie(_JSESSIONID);
    if (StringUtils.isBlank(jsessionid) && request.session().isNew()) {
      response.cookie(_JSESSIONID, request.session(true).id());
    }
    try {
      String vkApiKeyEnv = System.getenv(VK_API_KEY_ENV);
      if (StringUtils.isBlank(vkApiKeyEnv)) {
        return null;
      }
      String sig = encode(vkApiKeyEnv, signRequest);
      if (!sig.equals(sign)) {
        return dataToJson(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.MALFORMED_REQUEST));
      }
    } catch (Exception e) {
      return dataToJson(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.MALFORMED_REQUEST));
    }
    return null;
  }
}
