package com.workingbit.security.controller;

import com.despegar.http.client.*;
import com.despegar.sparkjava.test.SparkServer;
import com.workingbit.security.SecurityEmbedded;
import com.workingbit.share.client.ShareRemoteClient;
import com.workingbit.share.model.Answer;
import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.RegisterUser;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.ClassRule;
import org.junit.Test;
import spark.servlet.SparkApplication;

import static com.workingbit.share.common.RequestConstants.ACCESS_TOKEN_HEADER;
import static com.workingbit.share.common.RequestConstants.USER_SESSION_HEADER;
import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by Aleksey Popryaduhin on 17:56 30/09/2017.
 */
public class SecurityControllerTest {

  private static String boardUrl = "/api/v1";
  private static Integer randomPort = RandomUtils.nextInt(1000, 65000);

  public static class SecurityControllerTestSparkApplication implements SparkApplication {

    @Override
    public void init() {
      SecurityEmbedded.start();
    }
  }

  @ClassRule
  public static SparkServer<SecurityControllerTestSparkApplication> testServer = new SparkServer<>(SecurityControllerTestSparkApplication.class, randomPort);

  private AuthUser register() {
    String username = Utils.getRandomString();
    String password = Utils.getRandomString();
    RegisterUser registerUser = new RegisterUser(username, password);
    AuthUser registered = ShareRemoteClient.Singleton.getInstance().register(registerUser).get();
    assertNotNull(registered);

    return registered;
  }

  @Test
  public void reg_test() {
    AuthUser registered = register();
    assertNotNull(registered);
  }

  @Test
  public void auth_test() throws Exception {
    String username = Utils.getRandomString();
    String password = Utils.getRandomString();
    RegisterUser registerUser = new RegisterUser(username, password);
    AuthUser anonym = AuthUser.anonymous();
    AuthUser authUser = (AuthUser) post("/register", registerUser, anonym).getBody();
    assertNotNull(authUser);

    AuthUser authed = (AuthUser) post("/authenticate", authUser, authUser).getBody();
    assertNotNull(authed);
    System.out.println("AT1 " + authed.getAccessToken());

    authed = (AuthUser) post("/authenticate", authUser, authed).getBody();
    assertNotNull(authed);
    System.out.println("AT1 " + authed.getAccessToken());

    AuthUser author = (AuthUser) post("/authorize", registerUser, authed).getBody();
    assertNotNull(author);

    authed = (AuthUser) post("/authenticate", author, author).getBody();
    assertNotNull(authed);
    System.out.println("AT1 " + authed.getAccessToken());
  }

  @Test
  public void logout_test() throws Exception {
    String username = Utils.getRandomString();
    String password = Utils.getRandomString();
    RegisterUser registerUser = new RegisterUser(username, password);
    AuthUser anonym = AuthUser.anonymous();
    AuthUser authUser = (AuthUser) post("/register", registerUser, anonym).getBody();
    assertNotNull(authUser);

    AuthUser authed = (AuthUser) post("/authenticate", authUser, authUser).getBody();
    assertNotNull(authed);
    System.out.println("AT1 " + authed.getAccessToken());

    authed = (AuthUser) post("/authenticate", authUser, authed).getBody();
    assertNotNull(authed);
    System.out.println("AT1 " + authed.getAccessToken());

    AuthUser author = (AuthUser) post("/authorize", registerUser, authed).getBody();
    assertNotNull(author);

    authed = (AuthUser) post("/authenticate", author, author).getBody();
    assertNotNull(authed);
    System.out.println("AT1 " + authed.getAccessToken());

    authed = (AuthUser) post("/logout", author, author).getBody();
    assertNotNull(authed);
    System.out.println("LOGGED OUT " + authed.getAccessToken());

    authed = (AuthUser) post("/authenticate", author, author).getBody();
    assertNull(authed);
  }

  private Answer post(String path, Object payload, AuthUser authUser) throws HttpClientException {
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
    return jsonToData(new String(execute.body()), Answer.class);
  }

  private Answer put(String path, Object payload, AuthUser authUser) throws HttpClientException {
    PutMethod resp = testServer.put(boardUrl + path, dataToJson(payload), false);
    if (authUser != null) {
      resp.addHeader(ACCESS_TOKEN_HEADER, authUser.getAccessToken());
      resp.addHeader(USER_SESSION_HEADER, authUser.getUserSession());
    }
    HttpResponse execute = testServer.execute(resp);
    return jsonToData(new String(execute.body()), Answer.class);
  }

  private Answer get(String params) throws HttpClientException {
    GetMethod resp = testServer.get(boardUrl + "/" + params, false);
    HttpResponse execute = testServer.execute(resp);
    return jsonToData(new String(execute.body()), Answer.class);
  }
}