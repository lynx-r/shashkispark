package com.workingbit.security.service;


import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.enumarable.EnumAuthority;
import com.workingbit.share.model.UserCredentials;
import com.workingbit.share.util.Utils;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Created by Aleksey Popryadukhin on 18/04/2018.
 */
public class SecureUserServiceTest {

  private SecureUserService secureUserService = new SecureUserService();

  @Test
  public void auth() {
    register_authenticate_authorize_authorize();
  }

  private void register_authenticate_authorize_authorize() {
    String username = Utils.getRandomString20();
    String password = Utils.getRandomString20();
    UserCredentials userCredentials = new UserCredentials(username, password);
    AuthUser authUser = new AuthUser();
    authUser.setUserSession(Utils.getRandomString20());
    Optional<AuthUser> register = secureUserService.register(userCredentials);
    assertTrue(register.isPresent());
    AuthUser registered = register.get();
    System.out.println("REGISTERED USER " + registered);

    Optional<AuthUser> authenticatedOpt = secureUserService.authenticate(register.get());
    assertTrue(authenticatedOpt.isPresent());
    AuthUser authenticated = authenticatedOpt.get();
    assertEquals(registered, authenticated);

    authenticatedOpt = secureUserService.authenticate(register.get());
    assertTrue(authenticatedOpt.isPresent());
    authenticated = authenticatedOpt.get();
    assertEquals(registered, authenticated);

    Optional<AuthUser> authorizedOpt = secureUserService.authorize(userCredentials);
    assertTrue(authorizedOpt.isPresent());
    AuthUser authorized = authorizedOpt.get();
    System.out.println("AUTHORIZED USER " + authorized);

    assertNotEquals(registered, authorized);
    assertNotEquals(registered.getAccessToken(), authorized.getAccessToken());

    Optional<AuthUser> authenticated2Opt = secureUserService.authenticate(authorizedOpt.get());
    assertTrue(authenticated2Opt.isPresent());

    Optional<AuthUser> authenticated3Opt = secureUserService.authenticate(authorizedOpt.get());
    assertTrue(authenticated3Opt.isPresent());
    assertEquals(authenticated2Opt.get().getAccessToken(), authenticated3Opt.get().getAccessToken());

    Optional<AuthUser> authenticated4Opt = secureUserService.authenticate(authorizedOpt.get());
    assertTrue(authenticated4Opt.isPresent());
    assertEquals(authenticated2Opt.get().getAccessToken(), authenticated4Opt.get().getAccessToken());
  }

  @Test
  public void batch_register() {
    IntStream.range(0, 10).parallel()
        .forEach(i -> register_authenticate_authorize_authorize());
  }

  @Test
  public void logout_test() {
    String username = Utils.getRandomString20();
    String password = Utils.getRandomString20();
    AuthUser authUser = new AuthUser();
    authUser.setUserSession(Utils.getRandomString20());
    UserCredentials userCredentials = new UserCredentials(username, password);
    Optional<AuthUser> register = secureUserService.register(userCredentials);
    assertTrue(register.isPresent());

    Optional<AuthUser> loggedoutOpt = secureUserService.logout(register.get());
    assertTrue(loggedoutOpt.isPresent());
    AuthUser loggedout = loggedoutOpt.get();
    assertEquals(Collections.singleton(EnumAuthority.ANONYMOUS), loggedout.getAuthorities());

    Optional<AuthUser> forbiddenOpt = secureUserService.authenticate(loggedoutOpt.get());
    assertFalse(forbiddenOpt.isPresent());

    Optional<AuthUser> authorizedOpt = secureUserService.authorize(userCredentials);
    assertTrue(authorizedOpt.isPresent());
    AuthUser authorized = authorizedOpt.get();
    System.out.println("AUTHORIZED USER " + authorized);
  }
}