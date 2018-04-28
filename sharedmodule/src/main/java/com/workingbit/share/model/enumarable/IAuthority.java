package com.workingbit.share.model.enumarable;

import java.util.Set;

/**
 * Created by Aleksey Popryadukhin on 24/04/2018.
 */
public interface IAuthority {

  String getPath();

  Set<EnumAuthority> getAuthorities();

}
