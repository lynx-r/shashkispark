package com.workingbit.security.controller;

import com.workingbit.orchestrate.function.ModelHandlerFunc;
import com.workingbit.orchestrate.function.ParamsHandlerFunc;
import com.workingbit.security.config.Authority;
import com.workingbit.share.model.*;
import spark.Route;

import static com.workingbit.security.SecurityEmbedded.secureUserService;

/**
 * Created by Aleksey Popryaduhin on 13:58 27/09/2017.
 */
public class SecurityController {

  public static Route home = (req, res) -> "Security. Home, sweet home!";

  public static Route register = (req, res) ->
      ((ModelHandlerFunc<UserCredentials>) (data, token) ->
          Answer.ok(secureUserService.register((UserCredentials) data))
      ).handleAuthRequest(req, res, UserCredentials.class);

  public static Route authorize = (req, res) ->
      ((ModelHandlerFunc<UserCredentials>) (data, token) ->
          Answer.ok(secureUserService.authorize((UserCredentials) data))
      ).handleAuthRequest(req, res, UserCredentials.class);

  public static Route authenticate = (req, res) ->
      ((ParamsHandlerFunc<ParamPayload>) (data, token) ->
          Answer.ok(secureUserService.authenticate(token))
      ).handleAuthRequest(req, res);

  public static Route userInfo = (req, res) ->
      ((ModelHandlerFunc<AuthUser>) (data, token) ->
          Answer.ok(secureUserService.userInfo((AuthUser) data))
      ).handleRequest(req, res, Authority.USER_INFO_PROTECTED, AuthUser.class);

  public static Route saveUserInfo = (req, res) ->
      ((ModelHandlerFunc<UserInfo>) (data, token) ->
          Answer.ok(secureUserService.saveUserInfo(data, token))
      ).handleRequest(req, res, Authority.USER_INFO_PROTECTED, UserInfo.class);

  public static Route logout = (req, res) ->
      ((ParamsHandlerFunc<ParamPayload>) (params, token) ->
          Answer.ok(secureUserService.logout(token))
      ).handleRequest(req, res, Authority.LOGOUT_PROTECTED);
}
