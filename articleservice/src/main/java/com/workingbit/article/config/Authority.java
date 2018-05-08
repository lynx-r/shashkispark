package com.workingbit.article.config;


import com.workingbit.share.model.enumarable.EnumAuthority;
import com.workingbit.share.model.enumarable.IAuthority;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum Authority implements IAuthority {
  HOME("", new HashSet<>()),

  ARTICLES("/articles", new HashSet<>()),
  ARTICLE("/article", new HashSet<>()),
  ARTICLE_BY_ID("/article/:id", new HashSet<>()),
  ARTICLE_BY_HRU("/article/:hru", new HashSet<>());

  private String path;
  private Set<EnumAuthority> authorities;

  Authority(String path, Set<EnumAuthority> authorities) {
    this.path = path;
    this.authorities = authorities;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public Set<EnumAuthority> getAuthorities() {
    return authorities;
  }

  public Authority setAuthorities(Set<EnumAuthority> authorities) {
    this.authorities.addAll(authorities);
    return this;
  }

  public Authority setAuthorities(EnumAuthority... roles) {
    this.authorities.addAll(Arrays.asList(roles));
    return this;
  }

  public static class Constants {
    public static Set<EnumAuthority> ARTICLE_SECURE_ROLES
        = new HashSet<>(Arrays.asList(EnumAuthority.ADMIN, EnumAuthority.AUTHOR));
  }
}
