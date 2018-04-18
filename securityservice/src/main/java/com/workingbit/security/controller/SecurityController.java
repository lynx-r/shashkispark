package com.workingbit.security.controller;

import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.handler.ModelHandlerFunc;
import com.workingbit.share.model.Answer;
import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.RegisterUser;
import spark.Route;

import static com.workingbit.security.SecurityApplication.secureUserService;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;

/**
 * Created by Aleksey Popryaduhin on 13:58 27/09/2017.
 */
public class SecurityController {

  public static Route register = (req, res) ->
      ((ModelHandlerFunc<RegisterUser>) (data, token) ->
          secureUserService.register((RegisterUser) data, token)
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_REGISTER))
      ).handleRequest(req, res, false, RegisterUser.class);

  public static Route authorize = (req, res) ->
      ((ModelHandlerFunc<RegisterUser>) (data, token) ->
          secureUserService.authorize((RegisterUser) data)
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_AUTHORIZE))
      ).handleRequest(req, res, false, RegisterUser.class);

  public static Route authenticate = (req, res) ->
      ((ModelHandlerFunc<AuthUser>) (data, token) ->
          secureUserService.authenticate((AuthUser) data)
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_FORBIDDEN, ErrorMessages.UNABLE_TO_AUTHENTICATE))
      ).handleRequest(req, res, false, AuthUser.class);

  public static Route userInfo = (req, res) ->
      ((ModelHandlerFunc<AuthUser>) (data, token) ->
          secureUserService.userInfo((AuthUser) data)
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_AUTHENTICATE))
      ).handleRequest(req, res, false, AuthUser.class);

  public static Route logout = (req, res) ->
    ((ModelHandlerFunc<AuthUser>) (data, token) ->
        secureUserService.logout((AuthUser) data)
            .map(Answer::ok)
            .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_ASSIGN_ROLE))
    ).handleRequest(req, res, false, AuthUser.class);

}
