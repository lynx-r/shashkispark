package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.domain.DeepClone;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
@JsonTypeName("AuthUser")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthUser implements Payload, DeepClone {

  private String userId;
  private String username;
  private String accessToken;
  private String userSession;
  private int counter;
  private Set<EnumSecureRole> roles = new HashSet<>();

  public AuthUser(String userSession) {
    this.userSession = userSession;
  }

  public AuthUser(String accessToken, String userSession) {
    this.accessToken = accessToken;
    this.userSession = userSession;
  }

  public static AuthUser anonymous() {
    return new AuthUser(null, null, null, null, 0,
        Collections.singleton(EnumSecureRole.ANONYMOUS));
  }

  public AuthUser setRole(EnumSecureRole role) {
    this.roles = Collections.singleton(role);
    return this;
  }

  public AuthUser addRole(EnumSecureRole role) {
    this.roles.add(role);
    return this;
  }
}
