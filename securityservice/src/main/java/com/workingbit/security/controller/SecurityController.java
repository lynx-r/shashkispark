package com.workingbit.security.controller;

import com.workingbit.orchestrate.function.ModelHandlerFunc;
import com.workingbit.orchestrate.function.ParamsHandlerFunc;
import com.workingbit.security.config.Authority;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.model.*;
import spark.Route;

import static com.workingbit.security.SecurityEmbedded.secureUserService;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;

/**
 * Created by Aleksey Popryaduhin on 13:58 27/09/2017.
 */
public class SecurityController {

  public static Route home = (req, res) -> "Security. Home, sweet home!";

  public static Route register = (req, res) ->
      ((ModelHandlerFunc<UserCredentials>) (data, token) ->
          secureUserService.register((UserCredentials) data)
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_BAD_REQUEST, String.format(ErrorMessages.UNABLE_TO_REGISTER, data.getUsername())))
      ).handleAuthRequest(req, res, UserCredentials.class);

  public static Route authorize = (req, res) ->
      ((ModelHandlerFunc<UserCredentials>) (data, token) ->
          secureUserService.authorize((UserCredentials) data)
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_FORBIDDEN, ErrorMessages.UNABLE_TO_AUTHORIZE))
      ).handleAuthRequest(req, res, UserCredentials.class);

  public static Route authenticate = (req, res) ->
      ((ParamsHandlerFunc<ParamPayload>) (data, token) ->
          secureUserService.authenticate(token)
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_FORBIDDEN, ErrorMessages.UNABLE_TO_AUTHENTICATE))
      ).handleAuthRequest(req, res);

  public static Route userInfo = (req, res) ->
      ((ModelHandlerFunc<AuthUser>) (data, token) ->
          secureUserService.userInfo((AuthUser) data)
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_AUTHENTICATE))
      ).handleRequest(req, res, Authority.USER_INFO_PROTECTED, AuthUser.class);

  public static Route saveUserInfo = (req, res) ->
      ((ModelHandlerFunc<UserInfo>) (data, token) ->
          secureUserService.saveUserInfo(data, token)
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_AUTHENTICATE))
      ).handleRequest(req, res, Authority.USER_INFO_PROTECTED, UserInfo.class);

  public static Route logout = (req, res) ->
      ((ParamsHandlerFunc<ParamPayload>) (params, token) ->
          secureUserService.logout(token)
              .map(Answer::ok)
              .orElse(Answer.error(HTTP_BAD_REQUEST, ErrorMessages.UNABLE_TO_LOGOUT))
      ).handleRequest(req, res, Authority.LOGOUT_PROTECTED);
}
