package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.type.TypeReference;
import com.workingbit.share.dao.DaoFilters;
import com.workingbit.share.domain.DeepClone;
import com.workingbit.share.model.enumarable.EnumAuthority;
import com.workingbit.share.util.SecureUtils;
import com.workingbit.share.util.Utils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static com.workingbit.share.util.JsonUtils.jsonToDataTypeRef;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
@JsonTypeName("AuthUser")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthUser implements Payload, DeepClone {

  private DomainId userId;
  private String username;
  private String accessToken;
  private String userSession;
  private long timestamp;
  private DaoFilters filters = new DaoFilters();
  @NotNull
  private Set<EnumAuthority> authorities = new HashSet<>();
  private String superHash;
  // произвольные данные
  private Object data;

  @JsonIgnore
  private String internalKey;

  public AuthUser(String userSession) {
    this.userSession = userSession;
  }

  public AuthUser(String accessToken, String userSession) {
    this.accessToken = accessToken;
    this.userSession = userSession;
  }

  public static AuthUser anonymous() {
    return new AuthUser(null, null, null, null,
        Utils.getTimestamp(), new DaoFilters(), new HashSet<>(Set.of(EnumAuthority.ANONYMOUS)), null, null, null);
  }

  public static AuthUser simpleAuthor(DomainId userId, String username, String accessToken, String userSession) {
    return new AuthUser(userId, username, accessToken, userSession,
        Utils.getTimestamp(), new DaoFilters(), new HashSet<>(Set.of(EnumAuthority.AUTHOR)), null, null, null);
  }

  public static AuthUser simpleUser(DomainId userId, String username, String accessToken, String userSession, @NotNull Set<EnumAuthority> authorities) {
    return new AuthUser(userId, username, accessToken, userSession,
        Utils.getTimestamp(), new DaoFilters(), authorities, null, null, null);
  }

  public void setAuthorities(@NotNull Set<EnumAuthority> authorities) {
    this.authorities = new HashSet<>(authorities);
  }

  @NotNull
  public AuthUser addAuthorities(EnumAuthority... authorities) {
    this.authorities = new HashSet<>(Set.of(authorities));
    return this;
  }

  @NotNull
  public AuthUser addAuthority(EnumAuthority authority) {
    this.authorities.add(authority);
    return this;
  }

  @NotNull
  public AuthUser setInternalKey(String internalKey) {
    this.internalKey = internalKey;
    return this;
  }

  @NotNull
  @JsonIgnore
  public String getInternalHash() {
    return SecureUtils.digest(userSession + accessToken + internalKey);
  }

  @SuppressWarnings("unchecked")
  public void parseFilters(String filterExpression) {
    if (StringUtils.isNotBlank(filterExpression)) {
      TypeReference<DaoFilters> typeRef = new TypeReference<>() {
      };
      filters = jsonToDataTypeRef(filterExpression, typeRef);
    }
  }
}
