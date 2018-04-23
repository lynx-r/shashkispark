package com.workingbit.share.model;

/**
 * Created by Aleksey Popryadukhin on 17/04/2018.
 */
public enum EnumSecureRole {
  AUTHOR,
  ADMIN,
  ANONYMOUS,
  INTERNAL;

  public static boolean isSecure(EnumSecureRole role) {
    switch (role) {
      case ADMIN:
      case AUTHOR:
        return true;
      default:
        return false;
    }
  }
}
