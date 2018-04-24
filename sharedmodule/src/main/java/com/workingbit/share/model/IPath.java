package com.workingbit.share.model;

import java.util.Set;

/**
 * Created by Aleksey Popryadukhin on 24/04/2018.
 */
public interface IPath {

  boolean isSecure();

  String getPath();

  Set<EnumSecureRole> getRoles();
}
