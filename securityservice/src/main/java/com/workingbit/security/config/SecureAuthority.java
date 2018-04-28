package com.workingbit.security.config;


import com.workingbit.share.model.enumarable.EnumAuthority;
import com.workingbit.share.model.enumarable.IAuthority;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum SecureAuthority implements IAuthority {
  HOME("/", new HashSet<>()),
  REGISTER("/register", Constants.FREE_USER_AUTHORITIES),
  AUTHORIZE("/authorize", Constants.FREE_USER_AUTHORITIES),
  AUTHENTICATE("/authenticate", Constants.SECURE_USER_AUTHORITIES),
  USER_INFO("/user-info", Constants.SECURE_USER_AUTHORITIES),
  SAVE_USER_INFO("/save-user-info", Constants.SECURE_USER_AUTHORITIES),
  LOGOUT("/logout", Constants.SECURE_USER_AUTHORITIES);

  private String path;
  private Set<EnumAuthority> roles;

  SecureAuthority(String path, Set<EnumAuthority> roles) {
    this.path = path;
    this.roles = roles;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public Set<EnumAuthority> getAuthorities() {
    return roles;
  }

  public static class Constants {
    static final HashSet<EnumAuthority> SECURE_USER_AUTHORITIES =
        new HashSet<>(Arrays.asList(EnumAuthority.ADMIN, EnumAuthority.AUTHOR, EnumAuthority.INTERNAL));
    static final HashSet<EnumAuthority> FREE_USER_AUTHORITIES =
        new HashSet<>(Arrays.asList(EnumAuthority.ADMIN, EnumAuthority.AUTHOR, EnumAuthority.ANONYMOUS));
  }
}
