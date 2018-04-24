package com.workingbit.article.config;


import com.workingbit.share.model.EnumSecureRole;
import com.workingbit.share.model.IPath;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;

public enum Path implements IPath {
  HOME("", false, emptySet()),

  ARTICLES("/articles", false, emptySet()),
  ARTICLE("/article", false, emptySet()),
  ARTICLE_BY_ID("/article/:id", false, emptySet());

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

  public Path setRoles(List<EnumSecureRole> roles) {
    this.roles.addAll(roles);
    return this;
  }
}
