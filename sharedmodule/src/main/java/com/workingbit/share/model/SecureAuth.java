package com.workingbit.share.model;

import com.workingbit.share.domain.DeepClone;
import com.workingbit.share.model.enumarable.EnumAuthority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Aleksey Popryadukhin on 23/05/2018.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecureAuth implements DeepClone {

  private DomainId userId;
  private String groupId;
  private String email;
  private String passwordHash;
  private Set<EnumAuthority> authorities;

  /**
   * hash of user:passwordHash:salt
   */
  private String sigma;
  private int cost;
  private int misc;

  private String initVector;
  private String key;
  private int tokenLength;

  private String accessToken;
  private String userSession;
  private String secureToken;

  public void addAuthority(EnumAuthority role) {
    if (authorities == null) {
      authorities = new HashSet<>();
    }
    authorities.add(role);
  }
}
