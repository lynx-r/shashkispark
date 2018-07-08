package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.common.DBConstants;
import com.workingbit.share.converter.LocalDateTimeConverter;
import com.workingbit.share.domain.BaseDomain;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.LocalDateTime;

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

  /**
   * user name
   */
  @DynamoDBIndexHashKey(globalSecondaryIndexName = "usernameIndex")
  @DynamoDBAttribute(attributeName = "email")
  private String username;

  @DynamoDBAttribute(attributeName = "creditCard")
  private String creditCard;

  @JsonIgnore
  @DynamoDBIgnore
  @Override
  public boolean isReadonly() {
    return false;
  }

  @Override
  public void setReadonly(boolean readonly) {

  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("id", id)
        .append("createdAt", createdAt)
        .append("updatedAt", updatedAt)
        .append("email", username)
        .toString();
  }
}
