package com.workingbit.share.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Aleksey Popryadukhin on 19/04/2018.
 */
@Getter
@Setter
abstract class SecurePayload {

  private AuthUser authUser;
}
