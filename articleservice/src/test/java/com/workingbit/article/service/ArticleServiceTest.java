package com.workingbit.article.service;

import com.workingbit.article.BaseTest;
import com.workingbit.share.client.ShareRemoteClient;
import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.RegisterUser;
import com.workingbit.share.util.Utils;
import org.junit.Test;

import java.util.Optional;

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
    Optional<AuthUser> register = ShareRemoteClient.getInstance().register(registerUser);
    assertTrue(register.isPresent());
    AuthUser registered = register.get();

    Optional<AuthUser> authenticatedOpt = ShareRemoteClient.getInstance().authenticate(registered);
    assertTrue(authenticatedOpt.isPresent());
    AuthUser authenticated = authenticatedOpt.get();
    assertEquals(registered, authenticated);

    Optional<AuthUser> authorizedOpt = ShareRemoteClient.getInstance().authorize(registerUser);
    assertTrue(authorizedOpt.isPresent());
    AuthUser authorized = authorizedOpt.get();

    assertNotEquals(registered, authorized);
    assertNotEquals(registered.getAccessToken(), authorized.getAccessToken());
  }
}