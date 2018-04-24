package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
@JsonTypeName("UserInfo")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserInfo implements Payload {

  private String username;

  private Set<EnumSecureRole> roles = new HashSet<>();

  public void addRole(EnumSecureRole role) {
    this.roles.add(role);
  }

  public UserInfo role(EnumSecureRole role) {
    this.roles.add(role);
    return this;
  }
}
