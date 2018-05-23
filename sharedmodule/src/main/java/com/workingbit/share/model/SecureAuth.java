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
  private String username;
  private Set<EnumAuthority> authorities;

  private String initVector;
  private String key;
  private int tokenLength;
  private String salt;
  private String digest;

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
