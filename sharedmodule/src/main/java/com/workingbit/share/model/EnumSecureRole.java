package com.workingbit.share.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Aleksey Popryadukhin on 17/04/2018.
 */
public enum EnumSecureRole {
  AUTHOR,
  ADMIN,
  ANONYMOUS,
  INTERNAL,
  BAN;

  public static boolean isSecure(EnumSecureRole role) {
    if (BAN.equals(role)) {
      return false;
    }
    switch (role) {
      case ADMIN:
      case AUTHOR:
        return true;
      default:
        return false;
    }
  }

  public static boolean isSecure(Set<EnumSecureRole> roles) {
    if (roles.contains(BAN)) {
      return false;
    }
    return roles.stream().anyMatch(EnumSecureRole::isSecure);
  }

  public static boolean isAuthorRole(AuthUser authUser) {
    return authUser.getRoles()
        .stream()
        .anyMatch((role) ->
            Arrays.asList(EnumSecureRole.ADMIN, EnumSecureRole.AUTHOR).contains(role)
        );
  }

  public static Set<EnumSecureRole> parseRoles(String roles) {
    String[] rolesArr = roles.toUpperCase().split(",");
    Set<EnumSecureRole> roleSet = new HashSet<>();
    for (String role : rolesArr) {
      roleSet.add(EnumSecureRole.valueOf(role));
    }
    return roleSet;
  }
}
