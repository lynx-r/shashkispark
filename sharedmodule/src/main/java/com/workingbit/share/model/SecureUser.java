package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.workingbit.share.common.DBConstants;
import com.workingbit.share.converter.LocalDateTimeConverter;
import com.workingbit.share.domain.BaseDomain;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
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

  @DynamoDBTypeConvertedEnum
  @DynamoDBAttribute(attributeName = "role")
  private SecureRole role;

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
   * unecrypted user token
   */
  @DynamoDBAttribute(attributeName = "token")
  private String token;

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
}
