package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
@JsonTypeName("authUser")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthUser implements Payload {

  private String userId;
  private String accessToken;
  private String userSession;
  private EnumSecureRole role;

  public AuthUser(String session) {
    this.userSession = session;
  }

  public AuthUser(String accessToken, String userSession) {
    this.accessToken = accessToken;
    this.userSession = userSession;
  }

  public static AuthUser anonymous() {
    return new AuthUser().role(EnumSecureRole.ANONYMOUS);
  }

  public AuthUser role(EnumSecureRole role) {
    this.role = role;
    return this;
  }
}
