package com.workingbit.share.model.enumarable;

import com.workingbit.share.model.AuthUser;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Aleksey Popryadukhin on 17/04/2018.
 */
public enum EnumAuthority {
  AUTHOR,
  VIEWER,
  ADMIN,
  ANONYMOUS,
  INTERNAL,
  REMOVED;

  EnumAuthority() {
  }

  public static boolean hasAuthorities(@NotNull Set<EnumAuthority> clientAuthorities, @NotNull Set<EnumAuthority> allowedAuthorities) {
    // находим пересечение множеств доступов, так чтобы разрешенные доступы содержали
    // в себе все клиентские
    Set<EnumAuthority> intersection = new HashSet<>(allowedAuthorities);
    intersection.retainAll(clientAuthorities);
    return !intersection.isEmpty() && intersection.containsAll(clientAuthorities);
  }

  public static boolean hasAuthorAuthorities(AuthUser authUser) {
    return hasAuthorities(authUser.getAuthorities(), Set.of(AUTHOR, ADMIN, INTERNAL));
  }

  public static boolean isAnonymous(AuthUser authUser) {
    return hasAuthorities(authUser.getAuthorities(), Set.of(ANONYMOUS));
  }

  public static boolean isBanned(@NotNull Set<EnumAuthority> authorities) {
    return hasAuthorities(authorities, Set.of(REMOVED));
  }
}
