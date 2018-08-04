package com.workingbit.security.service;


import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.EnumRank;
import com.workingbit.share.model.UserCredentials;
import com.workingbit.share.model.enumarable.EnumAuthority;
import com.workingbit.share.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Collections;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Created by Aleksey Popryadukhin on 18/04/2018.
 */
public class SiteUserInfoServiceTest {

  @NotNull
  private SecureUserService secureUserService = new SecureUserService();

  @Test
  public void auth() {
    register_authenticate_authorize_authorize();
  }

  private void register_authenticate_authorize_authorize() {
    String username = Utils.getRandomString7();
    String password = Utils.getRandomString7();
    UserCredentials registeredUser = new UserCredentials(Utils.getRandomEmail(), Utils.getRandomString7(), Utils.getRandomString7(),
        Utils.getRandomString7(), Utils.getRandomString7(), EnumRank.III);
    AuthUser authUser = new AuthUser();
    authUser.setUserSession(Utils.getRandomString7());
    AuthUser register = secureUserService.register(registeredUser);
    assertTrue(register != null);
    AuthUser registered = register;
    System.out.println("REGISTERED USER " + registered);

    AuthUser authenticatedOpt = secureUserService.authenticate(register);
    assertTrue(authenticatedOpt != null);
    AuthUser authenticated = authenticatedOpt;
    assertEquals(registered, authenticated);

    authenticatedOpt = secureUserService.authenticate(register);
    assertTrue(authenticatedOpt != null);
    authenticated = authenticatedOpt;
    assertEquals(registered, authenticated);

    UserCredentials userCredentials = new UserCredentials(username, password, Utils.getRandomString7(),
        Utils.getRandomString7(), Utils.getRandomString7(), EnumRank.III);
    AuthUser authorizedOpt = secureUserService.authorize(userCredentials);
    assertTrue(authorizedOpt != null);
    AuthUser authorized = authorizedOpt;
    System.out.println("AUTHORIZED USER " + authorized);

    assertNotEquals(registered, authorized);
    assertNotEquals(registered.getAccessToken(), authorized.getAccessToken());

    AuthUser authenticated2Opt = secureUserService.authenticate(authorizedOpt);
    assertTrue(authenticated2Opt != null);

    AuthUser authenticated3Opt = secureUserService.authenticate(authorizedOpt);
    assertTrue(authenticated3Opt != null);
    assertEquals(authenticated2Opt.getAccessToken(), authenticated3Opt.getAccessToken());

    AuthUser authenticated4Opt = secureUserService.authenticate(authorizedOpt);
    assertTrue(authenticated4Opt != null);
    assertEquals(authenticated2Opt.getAccessToken(), authenticated4Opt.getAccessToken());
  }

  @Test
  public void batch_register() {
    IntStream.range(0, 10).parallel()
        .forEach(i -> register_authenticate_authorize_authorize());
  }

  @Test
  public void logout_test() {
    String username = Utils.getRandomString7();
    String password = Utils.getRandomString7();
    AuthUser authUser = new AuthUser();
    authUser.setUserSession(Utils.getRandomString7());
    UserCredentials userCredentials = new UserCredentials(Utils.getRandomEmail(), Utils.getRandomString7(), Utils.getRandomString7(),
        Utils.getRandomString7(), Utils.getRandomString7(), EnumRank.III);
    AuthUser register = secureUserService.register(userCredentials);
    assertTrue(register != null);

    AuthUser loggedoutOpt = secureUserService.logout(register);
    assertTrue(loggedoutOpt != null);
    AuthUser loggedout = loggedoutOpt;
    assertEquals(Collections.singleton(EnumAuthority.ANONYMOUS), loggedout.getAuthorities());

    AuthUser forbiddenOpt = secureUserService.authenticate(loggedoutOpt);
    assertFalse(forbiddenOpt != null);

    userCredentials = new UserCredentials(username, password, Utils.getRandomString7(),
        Utils.getRandomString7(), Utils.getRandomString7(), EnumRank.III);
    AuthUser authorizedOpt = secureUserService.authorize(userCredentials);
    assertTrue(authorizedOpt != null);
    AuthUser authorized = authorizedOpt;
    System.out.println("AUTHORIZED USER " + authorized);
  }
}