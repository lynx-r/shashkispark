package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.model.enumarable.EnumAuthority;
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

  private DomainId userId;
  private String username;

  private Set<EnumAuthority> authorities = new HashSet<>();

  public void addAuthority(EnumAuthority role) {
    this.authorities.add(role);
  }

  public UserInfo authority(EnumAuthority role) {
    this.authorities.add(role);
    return this;
  }
}
