package com.workingbit.share.handler;

import com.workingbit.share.client.SecurityRemoteClient;
import com.workingbit.share.model.AuthUser;
import org.apache.commons.lang3.StringUtils;
import spark.Request;
import spark.Response;

import java.util.Optional;

import static com.workingbit.share.common.RequestConstants.JSESSIONID;

/**
 * Created by Aleksey Popryaduhin on 16:37 01/10/2017.
 */
public interface BaseHandlerFunc {

  default String preprocess(Request request, Response response) {
    String session = request.headers(JSESSIONID);
    String jsessionid = request.cookie(JSESSIONID);
    if (StringUtils.isBlank(jsessionid) && request.session().isNew()
        && (StringUtils.isBlank(session) || !session.equals(jsessionid))) {
      response.cookie(JSESSIONID, request.session(true).id());
    }
    return null;
  }

  default Optional<AuthUser> isAuthenticated(String accessToken, String session) {
    AuthUser authUser = new AuthUser(accessToken, session);
    return SecurityRemoteClient.getInstance().authenticate(authUser);
  }
}
