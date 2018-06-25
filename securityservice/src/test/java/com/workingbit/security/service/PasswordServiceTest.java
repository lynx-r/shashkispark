package com.workingbit.security.service;

import com.workingbit.share.exception.CryptoException;
import com.workingbit.share.model.DomainId;
import com.workingbit.share.model.SecureAuth;
import com.workingbit.share.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by Aleksey Popryadukhin on 23/05/2018.
 */
public class PasswordServiceTest {

  @NotNull
  private PasswordService passwordService = new PasswordService();

  @Test
  public void registerUser() throws IOException, CryptoException {
    SecureAuth secureAuth = new SecureAuth();
    secureAuth.setUserId(DomainId.getRandomID());
    String username = Utils.getRandomString20();
    secureAuth.setUsername(username);
    secureAuth.setSecureToken("securetoken");
    secureAuth.setAccessToken("accesstoken");
    passwordService.registerUser(secureAuth);
    SecureAuth byUsername = passwordService.findByUsername(username).get();
    assertNotNull(byUsername);

    secureAuth = new SecureAuth();
    secureAuth.setUserId(DomainId.getRandomID());
    username = Utils.getRandomString20();
    secureAuth.setUsername(username);
    System.out.println(Utils.getRandomString(16));
    secureAuth.setSecureToken("securetoken");
    secureAuth.setAccessToken("accesstoken");
    passwordService.registerUser(secureAuth);
    byUsername = passwordService.findByUsername(username).get();
    assertNotNull(byUsername);
  }

  @Test
  public void findByUsername() {
  }

  @Test
  public void replaceSecureAuth() throws CryptoException, IOException {
    SecureAuth secureAuth = new SecureAuth();
    secureAuth.setUserId(DomainId.getRandomID());
    String username = Utils.getRandomString20();
    secureAuth.setUsername(username);
    secureAuth.setSecureToken("securetoken");
    secureAuth.setAccessToken("accesstoken");
    passwordService.registerUser(secureAuth);
    SecureAuth byUsername = passwordService.findByUsername(username).get();
    assertNotNull(byUsername);

    secureAuth = new SecureAuth();
    secureAuth.setUserId(DomainId.getRandomID());
    username = Utils.getRandomString20();
    secureAuth.setUsername(username);
    secureAuth.setSecureToken("securetoken");
    secureAuth.setAccessToken("accesstoken");
    passwordService.registerUser(secureAuth);
    byUsername = passwordService.findByUsername(username).get();
    assertNotNull(byUsername);

    SecureAuth secureAuthChangeUsername = new SecureAuth();
    secureAuthChangeUsername.setUserId(DomainId.getRandomID());
    String usernameNew = Utils.getRandomString20();
    secureAuthChangeUsername.setUsername(usernameNew);
    System.out.println(Utils.getRandomString(16));
    secureAuthChangeUsername.setSecureToken("securetoken");
    secureAuthChangeUsername.setAccessToken("accesstoken");

    passwordService.replaceSecureAuth(secureAuth, secureAuthChangeUsername);

    assertFalse(passwordService.findByUsername(username).isPresent());
    assertTrue(passwordService.findByUsername(usernameNew).isPresent());
  }
}