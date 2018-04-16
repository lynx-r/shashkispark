package com.workingbit.article.service;

import com.workingbit.article.BaseTest;
import com.workingbit.share.client.SecurityRemoteClient;
import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.RegisterUser;
import com.workingbit.share.util.Utils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.workingbit.share.common.RequestConstants.ACCESS_TOKEN;
import static com.workingbit.share.common.RequestConstants.JSESSIONID;
import static org.junit.Assert.*;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
public class ArticleServiceTest extends BaseTest {


  @Test
  public void auth() {
    String username = Utils.getRandomString();
    String password = Utils.getRandomString();
    RegisterUser registerUser = new RegisterUser(username, password);
    Optional<AuthUser> register = SecurityRemoteClient.getInstance().register(registerUser);
    assertTrue(register.isPresent());
    System.out.println(register.get());
    AuthUser registered = register.get();

    Map<String, String> headers = new HashMap<String, String>() {{
      put(ACCESS_TOKEN, registered.getAccessToken());
      put(JSESSIONID, registered.getSession());
    }};
    Optional<AuthUser> authenticatedOpt = SecurityRemoteClient.getInstance().authenticate( registered, headers);
    assertTrue(authenticatedOpt.isPresent());
    AuthUser authenticated = authenticatedOpt.get();
    assertEquals(registered, authenticated);

    Optional<AuthUser> authorizedOpt = SecurityRemoteClient.getInstance().authorize(registerUser);
    assertTrue(authorizedOpt.isPresent());
    AuthUser authorized = authorizedOpt.get();

    assertNotEquals(registered, authorized);
    assertNotEquals(registered.getAccessToken(), authorized.getAccessToken());
  }
}