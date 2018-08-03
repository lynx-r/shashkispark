package com.workingbit.security.controller;

import com.workingbit.orchestrate.function.ModelHandlerFunc;
import com.workingbit.orchestrate.function.ParamsHandlerFunc;
import com.workingbit.security.config.Authority;
import com.workingbit.share.model.*;
import org.jetbrains.annotations.NotNull;
import spark.Route;

import static com.workingbit.security.SecurityEmbedded.secureUserService;

/**
 * Created by Aleksey Popryaduhin on 13:58 27/09/2017.
 */
public class SecurityController {

  @NotNull
  public static Route home = (req, res) -> "Security. Home, sweet home!";

  @NotNull
  public static Route preRegister = (req, res) ->
      ((ModelHandlerFunc<UserCredentials>) (data, token, param) ->
          Answer.ok(secureUserService.preRegister((UserCredentials) data))
      ).handleAuthRequest(req, res, UserCredentials.class);

  @NotNull
  public static Route preAuthorize = (req, res) ->
      ((ModelHandlerFunc<UserCredentials>) (data, token, param) ->
          Answer.ok(secureUserService.preAuthorize((UserCredentials) data))
      ).handleAuthRequest(req, res, UserCredentials.class);

  @NotNull
  public static Route register = (req, res) ->
      ((ModelHandlerFunc<UserCredentials>) (data, token, param) ->
          Answer.ok(secureUserService.register((UserCredentials) data))
      ).handleAuthRequest(req, res, UserCredentials.class);

  @NotNull
  public static Route authorize = (req, res) ->
      ((ModelHandlerFunc<UserCredentials>) (data, token, param) ->
          Answer.ok(secureUserService.authorize((UserCredentials) data))
      ).handleAuthRequest(req, res, UserCredentials.class);

  @NotNull
  public static Route authenticate = (req, res) ->
      ((ParamsHandlerFunc<ParamPayload>) (data, token, param) ->
          Answer.ok(secureUserService.authenticate(token))
      ).handleAuthRequest(req, res);

  @NotNull
  public static Route userInfo = (req, res) ->
      ((ModelHandlerFunc<AuthUser>) (data, token, param) ->
          Answer.ok(secureUserService.userInfo((AuthUser) data))
      ).handleRequest(req, res, Authority.USER_INFO_PROTECTED, AuthUser.class);

  @NotNull
  public static Route saveUserInfo = (req, res) ->
      ((ModelHandlerFunc<UserInfo>) (data, token, param) ->
          Answer.ok(secureUserService.saveUserInfo(data, token))
      ).handleRequest(req, res, Authority.USER_INFO_PROTECTED, UserInfo.class);

  @NotNull
  public static Route logout = (req, res) ->
      ((ParamsHandlerFunc<ParamPayload>) (params, token, param) ->
          Answer.ok(secureUserService.logout(token))
      ).handleRequest(req, res, Authority.LOGOUT_PROTECTED);

  @NotNull
  public static Route resetPassword = (req, res) ->
      ((ModelHandlerFunc<UserCredentials>) (params, token, param) ->
          Answer.ok(secureUserService.resetPassword(params))
      ).handleRequest(req, res, Authority.RESET_PASSWORD, UserCredentials.class);
}
