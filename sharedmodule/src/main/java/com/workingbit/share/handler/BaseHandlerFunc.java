package com.workingbit.share.handler;

import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.common.RequestConstants;
import com.workingbit.share.model.Answer;
import com.workingbit.share.model.AuthUser;
import com.workingbit.share.service.SecureUserService;
import org.apache.commons.lang3.StringUtils;
import spark.Request;
import spark.Response;

import java.util.Optional;

import static com.workingbit.share.common.ApiConstants.VK_API_KEY_ENV;
import static com.workingbit.share.common.RequestConstants.JSESSIONID;
import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.Utils.encode;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * Created by Aleksey Popryaduhin on 16:37 01/10/2017.
 */
public interface BaseHandlerFunc {

  default String preprocess(Request request, Response response) {
    String sign = request.headers(RequestConstants.SIGN);
    String signRequest = request.headers(RequestConstants.SIGN_REQUEST);
    String jsessionid = request.cookie(JSESSIONID);
    if (StringUtils.isBlank(jsessionid) && request.session().isNew()) {
      response.cookie(JSESSIONID, request.session(true).id());
    }
    try {
      byte[] vkApiKeyEnv = System.getenv(VK_API_KEY_ENV).getBytes("UTF-8");
      if (vkApiKeyEnv.length == 0) {
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

  default Optional<AuthUser> isAuthenticated(String accessToken, String session) {
    AuthUser authUser = new AuthUser(accessToken, session);
    return SecureUserService.getInstance().authenticate(authUser);
  }
}
