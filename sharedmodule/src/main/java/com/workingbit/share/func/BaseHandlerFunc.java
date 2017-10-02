package com.workingbit.share.func;

import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.common.Log;
import com.workingbit.share.common.RequestConstants;
import com.workingbit.share.model.Answer;
import org.apache.commons.lang3.StringUtils;
import spark.Request;

import static com.workingbit.share.common.ApiConstants.VK_API_KEY_ENV;
import static com.workingbit.share.util.JsonUtil.dataToJson;
import static com.workingbit.share.util.Utils.encode;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * Created by Aleksey Popryaduhin on 16:37 01/10/2017.
 */
public interface BaseHandlerFunc {

  default String checkSign(Request request) {
    String sign = request.headers(RequestConstants.SIGN);
    String signRequest = request.headers(RequestConstants.SIGN_REQUEST);
    System.out.println(String.format("SIGN %s, SIGN_REQUEST %s", sign, signRequest));
    try {
      String vkApiKeyEnv = System.getenv(VK_API_KEY_ENV);
      if (StringUtils.isBlank(vkApiKeyEnv)) {
        Log.error(ErrorMessages.IGNORE_VK_API_SIGN);
        return null;
      }
      String sig = encode(vkApiKeyEnv, signRequest);
      if (!sig.equals(sign)) {
        return dataToJson(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.MALFORMED_REQUEST));
      }
    } catch (Exception e) {
      Log.error(e.getMessage(), e);
      return dataToJson(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.MALFORMED_REQUEST));
    }
    return null;
  }
}
