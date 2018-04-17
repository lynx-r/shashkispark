package com.workingbit.share.handler;

import com.workingbit.share.client.ShareRemoteClient;
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
    return null;
  }

  default String getOrCreateSession(Request request, Response response) {
    String clientSession = request.headers(JSESSIONID);
    String newSession = clientSession;
    if (StringUtils.isBlank(clientSession)) {
      newSession = request.session(true).id();
      response.cookie(JSESSIONID, newSession);
    }
    return newSession;
  }

  default Optional<AuthUser> isAuthenticated(String accessToken, String session) {
    if (StringUtils.isBlank(accessToken) || StringUtils.isBlank(session)) {
      return Optional.empty();
    }
    AuthUser authUser = new AuthUser(accessToken, session);
    return ShareRemoteClient.getInstance().authenticate(authUser);
  }
}
