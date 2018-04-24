package com.workingbit.security.config;


import com.workingbit.share.model.EnumSecureRole;
import com.workingbit.share.model.IPath;

import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

public enum Path implements IPath {
  HOME("/", false, emptySet()),
  REGISTER("/register", false, emptySet()),
  AUTHORIZE("/authorize", false, emptySet()),
  AUTHENTICATE("/authenticate", false, emptySet()),
  USER_INFO("/user-info", true, singleton(EnumSecureRole.ADMIN)),
  LOGOUT("/logout", false, emptySet());

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
