package com.workingbit.share.model;

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
}
