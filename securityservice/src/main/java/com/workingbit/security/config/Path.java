package com.workingbit.security.config;


import com.workingbit.share.model.EnumSecureRole;
import com.workingbit.share.model.IPath;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.singletonList;

public enum Path implements IPath {
  HOME("/", false, new HashSet()),
  REGISTER("/register", false, new HashSet()),
  AUTHORIZE("/authorize", false, new HashSet()),
  AUTHENTICATE("/authenticate", false, new HashSet()),
  USER_INFO("/user-info", true, new HashSet<>(singletonList(EnumSecureRole.ADMIN))),
  SAVE_USER_INFO("/save-user-info", true, new HashSet<>(Arrays.asList(EnumSecureRole.ADMIN, EnumSecureRole.AUTHOR))),
  LOGOUT("/logout", false, new HashSet());

  private String path;
  private boolean secure;
  private Set<EnumSecureRole> roles;

  Path(String path, boolean secure, Set<EnumSecureRole> roles) {
    this.path = path;
    this.secure = secure;
    this.roles = roles;
  }

  @Override
  public String getPath() {
    return path;
  }

  public Path setPath(String path) {
    this.path = path;
    return this;
  }

  @Override
  public boolean isSecure() {
    return secure;
  }

  public Path setSecure(boolean secure) {
    this.secure = secure;
    return this;
  }

  @Override
  public Set<EnumSecureRole> getRoles() {
    return roles;
  }

  public Path setRoles(Set<EnumSecureRole> roles) {
    this.roles = roles;
    return this;
  }
}
