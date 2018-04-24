package com.workingbit.article.config;


import com.workingbit.share.model.EnumSecureRole;
import com.workingbit.share.model.IPath;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum Path implements IPath {
  HOME("", false, new HashSet()),

  ARTICLES("/articles", false, new HashSet()),
  ARTICLE("/article", false, new HashSet()),
  ARTICLE_BY_ID("/article/:id", false, new HashSet());

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

  @Override
  public boolean isSecure() {
    return secure;
  }

  @Override
  public Set<EnumSecureRole> getRoles() {
    return roles;
  }

  public Path setPath(String path) {
    this.path = path;
    return this;
  }

  public Path setSecure(boolean secure) {
    this.secure = secure;
    return this;
  }

  public Path setRoles(Set<EnumSecureRole> roles) {
    this.roles.addAll(roles);
    return this;
  }

  public static class Constants {
    public static Set<EnumSecureRole> ARTICLE_SECURE_ROLES
        = new HashSet<>(Arrays.asList(EnumSecureRole.ADMIN, EnumSecureRole.AUTHOR));
  }
}
