package com.workingbit.security.service;

import com.workingbit.share.model.DomainId;
import com.workingbit.share.model.SecureAuth;
import com.workingbit.share.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Aleksey Popryadukhin on 23/05/2018.
 */
public class PasswordServiceTest {

  @NotNull
  private PasswordService passwordService = new PasswordService();

  @Test
  public void registerUser() {
    SecureAuth secureAuth = new SecureAuth();
    secureAuth.setUserId(DomainId.getRandomID());
    String username = Utils.getRandomString7();
    secureAuth.setEmail(username);
    secureAuth.setSecureToken("securetoken");
    secureAuth.setAccessToken("accesstoken");
    passwordService.registerUser(secureAuth);
    SecureAuth byUsername = passwordService.findByEmail(username).get();
    assertNotNull(byUsername);

    secureAuth = new SecureAuth();
    secureAuth.setUserId(DomainId.getRandomID());
    username = Utils.getRandomString7();
    secureAuth.setEmail(username);
    System.out.println(Utils.getRandomString(16));
    secureAuth.setSecureToken("securetoken");
    secureAuth.setAccessToken("accesstoken");
    passwordService.registerUser(secureAuth);
    byUsername = passwordService.findByEmail(username).get();
    assertNotNull(byUsername);
  }

  @Test
  public void findByUsername() {
  }

  @Test
  public void replaceSecureAuth() {
    SecureAuth secureAuth = new SecureAuth();
    secureAuth.setUserId(DomainId.getRandomID());
    String username = Utils.getRandomString7();
    secureAuth.setEmail(username);
    secureAuth.setSecureToken("securetoken");
    secureAuth.setAccessToken("accesstoken");
    passwordService.registerUser(secureAuth);
    SecureAuth byUsername = passwordService.findByEmail(username).get();
    assertNotNull(byUsername);

    secureAuth = new SecureAuth();
    secureAuth.setUserId(DomainId.getRandomID());
    username = Utils.getRandomString7();
    secureAuth.setEmail(username);
    secureAuth.setSecureToken("securetoken");
    secureAuth.setAccessToken("accesstoken");
    passwordService.registerUser(secureAuth);
    byUsername = passwordService.findByEmail(username).get();
    assertNotNull(byUsername);

    SecureAuth secureAuthChangeUsername = new SecureAuth();
    secureAuthChangeUsername.setUserId(DomainId.getRandomID());
    String usernameNew = Utils.getRandomString7();
    secureAuthChangeUsername.setEmail(usernameNew);
    System.out.println(Utils.getRandomString(16));
    secureAuthChangeUsername.setSecureToken("securetoken");
    secureAuthChangeUsername.setAccessToken("accesstoken");

    passwordService.save(secureAuthChangeUsername);

    assertFalse(passwordService.findByEmail(username).isPresent());
    assertTrue(passwordService.findByEmail(usernameNew).isPresent());
  }

  @Test
  public void registerUser1() {
  }

  @Test
  public void findByUsername1() {
    SecureAuth shashkionline = passwordService.findByEmail("shashkionline").get();
    System.out.println(shashkionline);
  }

  @Test
  public void replaceSecureAuth1() {
  }
}