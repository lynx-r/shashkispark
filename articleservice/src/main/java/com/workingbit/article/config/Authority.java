package com.workingbit.article.config;


import com.workingbit.share.model.enumarable.EnumAuthority;
import com.workingbit.share.model.enumarable.IAuthority;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum Authority implements IAuthority {
  HOME("", new HashSet<>()),

  // open api
  ARTICLES("/articles", Constants.INSECURE_ROLES),
  ARTICLE_BY_HRU("/article/:hru", Constants.INSECURE_ROLES),
  ARTICLE_BY_HRU_CACHED("/article-cache/:hru/:bbid", Constants.INSECURE_ROLES),
  ARTICLE_CACHE("/article-cache", Constants.INSECURE_ROLES),

  // must be protected
  ARTICLE_PROTECTED("/article", Constants.SECURE_ROLES),
  ARTICLE_IMPORT_PDN_PROTECTED("/import-pdn", Constants.SECURE_ROLES),
  ARTICLE_REMOVE_PROTECTED("/article-remove", Constants.SECURE_ROLES)
  ;

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
    static Set<EnumAuthority> SECURE_ROLES
        = new HashSet<>(Arrays.asList(EnumAuthority.ADMIN, EnumAuthority.AUTHOR));
    static Set<EnumAuthority> INSECURE_ROLES
        = new HashSet<>();
  }
}
