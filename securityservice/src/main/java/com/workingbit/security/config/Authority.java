package com.workingbit.security.config;


import com.workingbit.share.model.enumarable.EnumAuthority;
import com.workingbit.share.model.enumarable.IAuthority;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum Authority implements IAuthority {
  HOME("/", new HashSet<>()),

  // open api
  PRE_REGISTER("/pre-register", Constants.FREE_USER_AUTHORITIES),
  PRE_AUTHORIZE("/pre-authorize", Constants.FREE_USER_AUTHORITIES),
  REGISTER("/register", Constants.FREE_USER_AUTHORITIES),
  AUTHORIZE("/authorize", Constants.FREE_USER_AUTHORITIES),
  RESET_PASSWORD("/reset-password", Constants.FREE_USER_AUTHORITIES),

  // must be protected
  AUTHENTICATE_PROTECTED("/authenticate", Constants.SECURE_USER_AUTHORITIES),
  USER_INFO_PROTECTED("/user-info", Constants.SECURE_USER_AUTHORITIES),
  SAVE_USER_INFO_PROTECTED("/save-user-info", Constants.SECURE_USER_AUTHORITIES),
  LOGOUT_PROTECTED("/logout", Constants.SECURE_USER_AUTHORITIES);

  private String path;
  private Set<EnumAuthority> roles;

  Authority(String path, Set<EnumAuthority> roles) {
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
