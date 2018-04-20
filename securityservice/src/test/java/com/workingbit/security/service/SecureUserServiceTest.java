package com.workingbit.security.service;


import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.EnumSecureRole;
import com.workingbit.share.model.RegisterUser;
import com.workingbit.share.util.Utils;
import org.junit.Test;

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
    String username = Utils.getRandomString();
    String password = Utils.getRandomString();
    RegisterUser registerUser = new RegisterUser(username, password);
    AuthUser authUser = new AuthUser();
    authUser.setUserSession(Utils.getRandomString());
    Optional<AuthUser> register = secureUserService.register(registerUser);
    assertTrue(register.isPresent());
    AuthUser registered = register.get();
    System.out.println("REGISTERED USER " + registered);

    Optional<AuthUser> authenticatedOpt = secureUserService.authenticate(register);
    assertTrue(authenticatedOpt.isPresent());
    AuthUser authenticated = authenticatedOpt.get();
    assertEquals(registered, authenticated);

     authenticatedOpt = secureUserService.authenticate(register);
    assertTrue(authenticatedOpt.isPresent());
     authenticated = authenticatedOpt.get();
    assertEquals(registered, authenticated);

    Optional<AuthUser> authorizedOpt = secureUserService.authorize(registerUser);
    assertTrue(authorizedOpt.isPresent());
    AuthUser authorized = authorizedOpt.get();
    System.out.println("AUTHORIZED USER " + authorized);

    assertNotEquals(registered, authorized);
    assertNotEquals(registered.getAccessToken(), authorized.getAccessToken());

    Optional<AuthUser> authenticated2Opt = secureUserService.authenticate(authorizedOpt);
    assertTrue(authenticated2Opt.isPresent());

    Optional<AuthUser> authenticated3Opt = secureUserService.authenticate(authorizedOpt);
    assertTrue(authenticated3Opt.isPresent());
    assertEquals(authenticated2Opt.get().getAccessToken(), authenticated3Opt.get().getAccessToken());

    Optional<AuthUser> authenticated4Opt = secureUserService.authenticate(authorizedOpt);
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
    String username = Utils.getRandomString();
    String password = Utils.getRandomString();
    AuthUser authUser = new AuthUser();
    authUser.setUserSession(Utils.getRandomString());
    RegisterUser registerUser = new RegisterUser(username, password);
    Optional<AuthUser> register = secureUserService.register(registerUser);
    assertTrue(register.isPresent());
    AuthUser registered = register.get();

    Optional<AuthUser> loggedoutOpt = secureUserService.logout(register);
    assertTrue(loggedoutOpt.isPresent());
    AuthUser loggedout = loggedoutOpt.get();
    assertEquals(EnumSecureRole.ANONYMOUS, loggedout.getRole());

    Optional<AuthUser> forbiddenOpt = secureUserService.authenticate(loggedoutOpt);
    assertFalse(forbiddenOpt.isPresent());

    Optional<AuthUser> authorizedOpt = secureUserService.authorize(registerUser);
    assertTrue(authorizedOpt.isPresent());
    AuthUser authorized = authorizedOpt.get();
    System.out.println("AUTHORIZED USER " + authorized);
  }
}