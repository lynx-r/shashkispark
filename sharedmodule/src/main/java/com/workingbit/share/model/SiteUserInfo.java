package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.common.DBConstants;
import com.workingbit.share.converter.AuthoritySetConverter;
import com.workingbit.share.converter.LocalDateTimeConverter;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.model.enumarable.EnumAuthority;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@DynamoDBTable(tableName = DBConstants.SECURE_USER_TABLE)
public class SiteUserInfo extends BaseDomain {
  @DynamoDBHashKey(attributeName = "id")
  private String id;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBRangeKey(attributeName = "createdAt")
  private LocalDateTime createdAt;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBAttribute(attributeName = "updatedAt")
  private LocalDateTime updatedAt;

  @DynamoDBAttribute(attributeName = "firstName")
  private String firstName;
  @DynamoDBAttribute(attributeName = "middleName")
  private String middleName;
  @DynamoDBAttribute(attributeName = "lastName")
  private String lastName;

  /**
   * user name
   */
  @DynamoDBIndexHashKey(globalSecondaryIndexName = "emailIndex")
  @DynamoDBAttribute(attributeName = "email")
  private String email;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  private LocalDateTime loggedOutTime;

  @DynamoDBAttribute(attributeName = "creditCard")
  private String creditCard;

  @DynamoDBTypeConverted(converter = AuthoritySetConverter.class)
  @DynamoDBAttribute(attributeName = "authorities")
  private Set<EnumAuthority> authorities;

  @JsonIgnore
  @DynamoDBIgnore
  @Override
  public boolean isReadonly() {
    return false;
  }

  @Override
  public void setReadonly(boolean readonly) {

  }

  public void addAuthority(EnumAuthority role) {
    if (authorities == null) {
      authorities = new HashSet<>();
    }
    authorities.add(role);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("id", id)
        .append("createdAt", createdAt)
        .append("updatedAt", updatedAt)
        .append("email", email)
        .toString();
  }
}
