package com.workingbit.article.service;

import com.workingbit.article.BaseTest;
import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.RegisterUser;
import com.workingbit.share.util.SecureUtils;
import com.workingbit.share.util.Utils;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
public class ArticleServiceTest extends BaseTest {


  @Test
  public void auth() {
    String username = Utils.getRandomString();
    String password = Utils.getRandomString();
    RegisterUser registerUser = new RegisterUser(username, password);
    String session = Utils.getRandomString();
    Optional<AuthUser> authUserOptional = Optional.of(new AuthUser("", session));
    Optional<AuthUser> register = SecureUtils.register(registerUser, authUserOptional);
    assertTrue(register.isPresent());
    System.out.println(register.get());
    AuthUser registered = register.get();
    Optional<AuthUser> authenticatedOpt = SecureUtils.authenticate(registered);
    assertTrue(authenticatedOpt.isPresent());
    AuthUser authenticated = authenticatedOpt.get();
    assertEquals(registered, authenticated);
  }
}