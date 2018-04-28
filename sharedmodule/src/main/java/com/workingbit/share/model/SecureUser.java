package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.common.DBConstants;
import com.workingbit.share.converter.LocalDateTimeConverter;
import com.workingbit.share.converter.UserRolesConverter;
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
public class SecureUser extends BaseDomain {
  @DynamoDBHashKey(attributeName = "id")
  private String id;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBRangeKey(attributeName = "createdAt")
  private LocalDateTime createdAt;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBAttribute(attributeName = "updatedAt")
  private LocalDateTime updatedAt;

  /**
   * user name
   */
  @DynamoDBIndexHashKey(globalSecondaryIndexName = "usernameIndex")
  @DynamoDBAttribute(attributeName = "username")
  private String username;

  @DynamoDBTypeConverted(converter = UserRolesConverter.class)
  @DynamoDBAttribute(attributeName = "authorities")
  private Set<EnumAuthority> authorities = new HashSet<>();

  /**
   * hash of user:password:salt
   */
  @DynamoDBAttribute(attributeName = "digest")
  private String digest;

  /**
   * random string with ":" prefix
   */
  @DynamoDBAttribute(attributeName = "salt")
  private String salt;

  /**
   * key for encryption
   */
  @DynamoDBAttribute(attributeName = "key")
  private String key;

  /**
   * init vector for encryption
   */
  @DynamoDBAttribute(attributeName = "initVector")
  private String initVector;

  /**
   * Random token length
   */
  @DynamoDBAttribute(attributeName = "tokenLength")
  private int tokenLength;

  /**
   * unecrypted user token. MUST NOT BE REVEAL
   */
  @DynamoDBAttribute(attributeName = "secureToken")
  private String secureToken;

  /**
   * encrypted user token goes through wires
   */
  @DynamoDBAttribute(attributeName = "accessToken")
  private String accessToken;

  /**
   * user userSession acquire from Spark
   */
  @DynamoDBIndexHashKey(globalSecondaryIndexName = "userSessionIndex")
  @DynamoDBAttribute(attributeName = "userSession")
  private String userSession;

  @JsonIgnore
  @DynamoDBIgnore
  @Override
  public boolean isReadonly() {
    return false;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("id", id)
        .append("createdAt", createdAt)
        .append("updatedAt", updatedAt)
        .append("username", username)
        .append("authorities", authorities)
        .append("accessToken", accessToken)
        .append("userSession", userSession)
        .toString();
  }

  public void addAuthority(EnumAuthority role) {
    authorities.add(role);
  }
}
