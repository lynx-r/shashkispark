package com.workingbit.share.handler;

import com.workingbit.share.client.SecurityRemoteClient;
import com.workingbit.share.model.AuthUser;
import spark.Request;
import spark.Response;

import java.util.Optional;

/**
 * Created by Aleksey Popryaduhin on 16:37 01/10/2017.
 */
public interface BaseHandlerFunc {

  default String preprocess(Request request, Response response) {
    return null;
  }

  default Optional<AuthUser> isAuthenticated(String accessToken, String session) {
    AuthUser authUser = new AuthUser(accessToken, session);
    return SecurityRemoteClient.getInstance().authenticate(authUser);
  }
}
