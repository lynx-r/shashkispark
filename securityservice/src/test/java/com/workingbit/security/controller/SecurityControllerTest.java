package com.workingbit.security.controller;

import com.despegar.http.client.HttpClientException;
import com.despegar.http.client.HttpResponse;
import com.despegar.http.client.PostMethod;
import com.despegar.sparkjava.test.SparkServer;
import com.workingbit.security.SecurityEmbedded;
import com.workingbit.security.config.Authority;
import com.workingbit.share.common.ErrorMessages;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumAuthority;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.ClassRule;
import org.junit.Test;
import spark.servlet.SparkApplication;

import java.util.Arrays;
import java.util.Collections;

import static com.workingbit.orchestrate.OrchestrateModule.orchestralService;
import static com.workingbit.orchestrate.util.AuthRequestUtil.hasAuthorities;
import static com.workingbit.share.common.RequestConstants.*;
import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;
import static com.workingbit.share.util.Utils.getRandomString7;
import static java.net.HttpURLConnection.*;
import static org.junit.Assert.*;

/**
 * Created by Aleksey Popryaduhin on 17:56 30/09/2017.
 */
public class SecurityControllerTest {

  @NotNull
  private static String boardUrl = "/api/v1";
  private static Integer randomPort = RandomUtils.nextInt(1000, 65000);

  public static class SecurityControllerTestSparkApplication implements SparkApplication {

    @Override
    public void init() {
      SecurityEmbedded.start();
    }
  }

  @NotNull
  @ClassRule
  public static SparkServer<SecurityControllerTestSparkApplication> testServer = new SparkServer<>(SecurityControllerTestSparkApplication.class, randomPort);

  private AuthUser register(String password) {
    String email = Utils.getRandomEmail();
    UserCredentials userCredentials = new UserCredentials(email, password, Utils.getRandomString7(),
        Utils.getRandomString7(), Utils.getRandomString7(), EnumRank.III);
    AuthUser authUser = orchestralService.preRegister(userCredentials).get();
    assertNotNull(authUser.getSalt());
    assertEquals(10000, authUser.getCost());
    assertEquals(8, authUser.getMisc());
    RegisteredUser registeredUser = new RegisteredUser(Utils.getRandomString7(), Utils.getRandomString7(),
        Utils.getRandomString7(), EnumRank.MS, email, password);
    AuthUser registered = orchestralService.register(registeredUser).get();
    assertNotNull(registered);

    return registered;
  }

  @NotNull
  private AuthUser register() throws RequestException {
    String password = Utils.getRandomString(64);
    return register(password);
  }

  @Test
  public void reg_test() throws RequestException {
    AuthUser registered = register();
    assertNotNull(registered);
  }

  @Test
  public void reg_with_empty_credentials() throws HttpClientException {
    String[] credentErrors = {ErrorMessages.FIRSTNAME_NOT_NULL, ErrorMessages.PASSWORD_NOT_NULL};
    UserCredentials userCredentials = new UserCredentials(null, null, Utils.getRandomString7(),
        Utils.getRandomString7(), Utils.getRandomString7(), EnumRank.III);
    post("/register", userCredentials, AuthUser.anonymous(), HTTP_BAD_REQUEST, credentErrors);
  }

  @Test
  public void reg_with_invalid_credentials() throws HttpClientException {
    String[] credentErrors = {ErrorMessages.FIRSTNAME_CONSTRAINTS, ErrorMessages.PASSWORD_CONSTRAINTS};
    UserCredentials userCredentials = new UserCredentials("12", "123", Utils.getRandomString7(),
        Utils.getRandomString7(), Utils.getRandomString7(), EnumRank.III);
    post("/register", userCredentials, AuthUser.anonymous(), HTTP_BAD_REQUEST, credentErrors);
  }

  @Test
  public void register_twice_with_same_credentials() {
    RegisteredUser userCredentials = new RegisteredUser(Utils.getRandomString7(), Utils.getRandomString7(),
        Utils.getRandomString7(), EnumRank.MS, Utils.getRandomString7(), Utils.getRandomString7());
    orchestralService.register(userCredentials).get();

    int code = 0;
    try {
      orchestralService.register(userCredentials).get();
    } catch (RequestException e) {
      code = e.getCode();
    }
    assertEquals(HTTP_FORBIDDEN, code);
  }

  @Test
  public void authorize_not_registered() throws Exception {
    UserCredentials userCredentials = new UserCredentials(getRandomString7(), getRandomString7(), Utils.getRandomString7(),
        Utils.getRandomString7(), Utils.getRandomString7(), EnumRank.III);
    post("/authorize", userCredentials, AuthUser.anonymous(), HTTP_FORBIDDEN);
  }

  @Test
  public void auth_test() throws Exception {
    String username = getRandomString7();
    String password = getRandomString7();
    UserCredentials userCredentials = new UserCredentials(username, password, Utils.getRandomString7(),
        Utils.getRandomString7(), Utils.getRandomString7(), EnumRank.III);
    AuthUser anonym = AuthUser.anonymous();
    AuthUser authUser = (AuthUser) post("/register", userCredentials, anonym, HTTP_OK).getBody();
    assertNotNull(authUser);

    AuthUser authed = (AuthUser) get("/authenticate", authUser, HTTP_OK).getBody();
    assertFalse(hasAuthorities(authed.getAuthorities(), Collections.singleton(EnumAuthority.ANONYMOUS)));
    System.out.println("AT1 " + authed.getAccessToken());

    authed = (AuthUser) get("/authenticate", authed, HTTP_OK).getBody();
    assertFalse(hasAuthorities(authed.getAuthorities(), Collections.singleton(EnumAuthority.ANONYMOUS)));
    System.out.println("AT1 " + authed.getAccessToken());

    AuthUser author = (AuthUser) post("/authorize", userCredentials, authed, HTTP_OK).getBody();
    assertFalse(hasAuthorities(authed.getAuthorities(), Collections.singleton(EnumAuthority.ANONYMOUS)));

    authed = (AuthUser) get("/authenticate", author, HTTP_OK).getBody();
    assertFalse(hasAuthorities(authed.getAuthorities(), Collections.singleton(EnumAuthority.ANONYMOUS)));
    System.out.println("AT1 " + authed.getAccessToken());
  }

  @Test
  public void authorize_after_logout() throws Exception {
    UserCredentials userCredentials = new UserCredentials(getRandomString7(), getRandomString7(), Utils.getRandomString7(),
        Utils.getRandomString7(), Utils.getRandomString7(), EnumRank.III);
    post("/register", userCredentials, AuthUser.anonymous(), HTTP_OK);

    var answerAuthorize = post("/authorize", userCredentials, AuthUser.anonymous(), HTTP_OK);

    AuthUser authUser = answerAuthorize.getAuthUser();
    get("/logout", authUser, HTTP_OK);

    post("/authorize", userCredentials, AuthUser.anonymous(), HTTP_FORBIDDEN);
  }

  @Test
  public void authenticate_anonymous() throws Exception {
    get("/authenticate", AuthUser.anonymous(), HTTP_FORBIDDEN);
  }

  @Test
  public void authenticate_after_registration() throws Exception {
    AuthUser register = register();
    get("/authenticate", register, HTTP_OK);
  }

  @Test
  public void authenticate() throws Exception {
    UserCredentials userCredentials = new UserCredentials(getRandomString7(), getRandomString7(), Utils.getRandomString7(),
        Utils.getRandomString7(), Utils.getRandomString7(), EnumRank.III);
    var registerResult = post("/register", userCredentials, AuthUser.anonymous(), HTTP_OK);
    var answerAuthorize = post("/authorize", userCredentials, AuthUser.anonymous(), HTTP_OK);

    AuthUser authUser = answerAuthorize.getAuthUser();
    Answer answerAuthenticate = get("/authenticate", authUser, HTTP_OK);

    get("/authenticate", (AuthUser) answerAuthenticate.getBody(), HTTP_OK);
  }

  @Test
  public void authenticate_after_logout() throws Exception {
    String password = Utils.getRandomString(64);
    var authUser = register(password);
    UserCredentials userCredentials = new UserCredentials(authUser.getEmail(), password, Utils.getRandomString7(),
        Utils.getRandomString7(), Utils.getRandomString7(), EnumRank.III);
    Answer answer = post(Authority.PRE_AUTHORIZE.getPath(), userCredentials, authUser, HTTP_OK);
    var answerAuthorize = post("/authorize", userCredentials, AuthUser.anonymous(), HTTP_OK);

    authUser = answerAuthorize.getAuthUser();
    Answer answerAuthenticate = get("/authenticate", authUser, HTTP_OK);

    answerAuthenticate = get("/authenticate", (AuthUser) answerAuthenticate.getBody(), HTTP_OK);

    authUser = answerAuthenticate.getAuthUser();
    var answerLogout = get("/logout", authUser, HTTP_OK);

    get("/authenticate", (AuthUser) answerLogout.getBody(), HTTP_FORBIDDEN);
  }

  @Test
  public void authenticate_after_registration_twice() throws Exception {
    AuthUser authUser = register();
    Answer answerAuthenticate = get("/authenticate", authUser, HTTP_OK);
    get("/authenticate", (AuthUser) answerAuthenticate.getBody(), HTTP_OK);
  }

  @Test
  public void logout_test() throws Exception {
    String username = getRandomString7();
    String password = getRandomString7();
    UserCredentials userCredentials = new UserCredentials(username, password, Utils.getRandomString7(),
        Utils.getRandomString7(), Utils.getRandomString7(), EnumRank.III);
    AuthUser anonym = AuthUser.anonymous();
    AuthUser authUser = post("/register", userCredentials, anonym, HTTP_OK).getAuthUser();
    assertNotNull(authUser);

    AuthUser authed = get("/authenticate", authUser, HTTP_OK).getAuthUser();
    assertNotNull(authed);
    System.out.println("AT1 " + authed.getAccessToken());

    authed = get("/authenticate", authed, HTTP_OK).getAuthUser();
    assertNotNull(authed);
    System.out.println("AT1 " + authed.getAccessToken());

    AuthUser author = post("/authorize", userCredentials, authed, HTTP_OK).getAuthUser();
    assertNotNull(author);

    authed = get("/authenticate", author, HTTP_OK).getAuthUser();
    assertNotNull(authed);
    System.out.println("AT1 " + authed.getAccessToken());

    authed = get("/authenticate", authed, HTTP_OK).getAuthUser();
    assertNotNull(authed);
    System.out.println("AT1 " + authed.getAccessToken());

    authed = get("/logout", authed, HTTP_OK).getAuthUser();
    assertNotNull(authed);
    System.out.println("LOGGED OUT " + authed.getAccessToken());

    authed = get("/authenticate", authed, HTTP_FORBIDDEN).getAuthUser();
    assertNull(authed);
  }

  @Test
  public void logout_after_logout() throws Exception {
    UserCredentials userCredentials = new UserCredentials(getRandomString7(), getRandomString7(), Utils.getRandomString7(),
        Utils.getRandomString7(), Utils.getRandomString7(), EnumRank.III);
    var registerResult = post("/register", userCredentials, AuthUser.anonymous(), HTTP_OK);
    var answerAuthorize = post("/authorize", userCredentials, AuthUser.anonymous(), HTTP_OK);

    AuthUser authUser = answerAuthorize.getAuthUser();
    Answer answerAuthenticate = get("/authenticate", authUser, HTTP_OK);

    answerAuthenticate = get("/authenticate", (AuthUser) answerAuthenticate.getBody(), HTTP_OK);

    authUser = answerAuthenticate.getAuthUser();
    var answerLogout = get("/logout", authUser, HTTP_OK);

    answerLogout = get("/logout", authUser, HTTP_FORBIDDEN);
  }

//  @Test
//  public void test_banned() throws Exception {
//    UserCredentials userCredentials = new UserCredentials(getRandomString7(), getRandomString7());
//    var registerResult = post("/register", userCredentials, AuthUser.anonymous(), HTTP_OK);
//
//    AuthUser authUser = registerResult.getAuthUser();
//    Answer answer = post("/user-info", authUser, authUser, HTTP_OK);
//    authUser = answer.getAuthUser();
//    UserInfo userInfo = (UserInfo) answer.getBody();
//    // remove user
//    userInfo.addAuthority(EnumAuthority.REMOVED);
//    answer = post("/save-user-info", userInfo, authUser, HTTP_OK);
//    AuthUser authUserBanned = answer.getAuthUser();
//
//    // cant update
//    answer = post("/save-user-info", userInfo, authUserBanned, HTTP_FORBIDDEN);
//    answer = get("/user-info", authUserBanned, HTTP_FORBIDDEN);
//
//    // createWithoutRoot user
//    userCredentials = new UserCredentials(getRandomString7(), getRandomString7());
//    registerResult = post("/register", userCredentials, AuthUser.anonymous(), HTTP_OK);
//    authUser = registerResult.getAuthUser();
//    answer = post("/user-info", authUserBanned, authUser, HTTP_OK);
//
//    // save user
//    userInfo.addAuthority(EnumAuthority.ADMIN);
//    authUser.addAuthority(EnumAuthority.ADMIN);
//    answer = post("/save-user-info", userInfo, authUser, HTTP_FORBIDDEN);
//
//    userInfo.addAuthority(EnumAuthority.ADMIN);
//    authUser.addAuthority(EnumAuthority.ADMIN);
//    String shashki_super_user = System.getenv("SHASHKI_SUPER_USER");
//    assertNotNull("Супер пароль не может быть пустым", shashki_super_user);
//    authUser.setSuperHash(SecureUtils.digest(shashki_super_user));
//    answer = post("/save-user-info", userInfo, authUser, HTTP_OK);
//
//    authUser = answer.getAuthUser();
//    UserInfo userInfoBanned = (UserInfo) answer.getBody();
//    userInfoBanned.setAuthorities(authUser.getAuthorities());
//    answer = post("/save-user-info", userInfoBanned, authUser, HTTP_OK);
//
//    AuthUser unbanned = answer.getAuthUser();
//    answer = post("/save-user-info", userInfoBanned, unbanned, HTTP_OK);
//  }

  private Answer post(String path, Object payload, @Nullable AuthUser authUser, int expectCode) throws HttpClientException {
    PostMethod resp = testServer.post(boardUrl + path, dataToJson(payload), false);
    if (authUser != null) {
      if (StringUtils.isNotBlank(authUser.getAccessToken())) {
        resp.addHeader(ACCESS_TOKEN_HEADER, authUser.getAccessToken());
      }
      if (StringUtils.isNotBlank(authUser.getUserSession())) {
        resp.addHeader(USER_SESSION_HEADER, authUser.getUserSession());
      }
      if (StringUtils.isNotBlank(authUser.getSuperHash())) {
        resp.addHeader(SUPER_HASH_HEADER, authUser.getSuperHash());
      }
    }
    HttpResponse execute = testServer.execute(resp);
    assertEquals(expectCode, execute.code());
    Answer answer = jsonToData(new String(execute.body()), Answer.class);
    assertEquals(expectCode, answer.getStatusCode());
    return answer;
  }

  private Answer post(String path, Object payload, @Nullable AuthUser authUser, int expectCode, @NotNull String[] errors) throws HttpClientException {
    PostMethod resp = testServer.post(boardUrl + path, dataToJson(payload), false);
    if (authUser != null) {
      if (StringUtils.isNotBlank(authUser.getAccessToken())) {
        resp.addHeader(ACCESS_TOKEN_HEADER, authUser.getAccessToken());
      }
      if (StringUtils.isNotBlank(authUser.getUserSession())) {
        resp.addHeader(USER_SESSION_HEADER, authUser.getUserSession());
      }
    }
    HttpResponse execute = testServer.execute(resp);
    assertEquals(expectCode, execute.code());
    Answer answer = jsonToData(new String(execute.body()), Answer.class);
    assertEquals(expectCode, answer.getStatusCode());
    Arrays.sort(errors);
    String[] actualErrors = answer.getMessage().getMessages();
    Arrays.sort(actualErrors);
    assertArrayEquals(errors, actualErrors);
    return answer;
  }

  private Answer get(String path, @Nullable AuthUser authUser, int expectCode) throws HttpClientException {
    var resp = testServer.get(boardUrl + path, false);
    if (authUser != null) {
      if (StringUtils.isNotBlank(authUser.getAccessToken())) {
        resp.addHeader(ACCESS_TOKEN_HEADER, authUser.getAccessToken());
      }
      if (StringUtils.isNotBlank(authUser.getUserSession())) {
        resp.addHeader(USER_SESSION_HEADER, authUser.getUserSession());
      }
    }
    HttpResponse execute = testServer.execute(resp);
    assertEquals(expectCode, execute.code());
    Answer answer = jsonToData(new String(execute.body()), Answer.class);
    assertEquals(expectCode, answer.getStatusCode());
    return answer;
  }
}