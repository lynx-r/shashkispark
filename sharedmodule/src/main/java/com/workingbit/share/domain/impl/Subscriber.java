//package com.workingbit.share.domain.impl;
//
//import com.amazonaws.services.dynamodbv2.datamodeling.*;
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import com.fasterxml.jackson.annotation.JsonTypeName;
//import com.workingbit.share.common.DBConstants;
//import com.workingbit.share.converter.LocalDateTimeConverter;
//import com.workingbit.share.domain.BaseDomain;
//import com.workingbit.share.model.Payload;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.jetbrains.annotations.Nullable;
//
//import javax.validation.constraints.Email;
//import javax.validation.constraints.Size;
//import java.time.LocalDateTime;
//
///**
// * Created by Aleksey Popryadukhin on 18/06/2018.
// */
//@NoArgsConstructor
//@DynamoDBTable(tableName = DBConstants.SUBSCRIBER)
//@JsonTypeName("Subscriber")
//@Data
//public class Subscriber extends BaseDomain implements Payload {
//
//
//
//  @DynamoDBHashKey(attributeName = "id")
//  private String id;
//
//  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
//  @DynamoDBRangeKey(attributeName = "createdAt")
//  private LocalDateTime createdAt;
//
//  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
//  @DynamoDBAttribute(attributeName = "updatedAt")
//  private LocalDateTime updatedAt;
//
//  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
//  @DynamoDBAttribute(attributeName = "unsubscribeDate")
//  private LocalDateTime unsubscribeDate;
//
//  @Size(max = 200)
//  @DynamoDBAttribute(attributeName = "name")
//  private String name;
//
//  @Size(max = 200)
//  @Email(message = "Не верный адрес электронной почты")
//  @DynamoDBIndexHashKey(globalSecondaryIndexName = "emailIndex")
//  @DynamoDBAttribute(attributeName = "email")
//  private String email;
//
//  @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.BOOL)
//  @DynamoDBAttribute(attributeName = "subscribed")
//  private boolean subscribed;
//
//  @JsonCreator
//  public Subscriber(@JsonProperty("email") String email) {
//    this.email = email;
//  }
//
//  @Nullable
//  @Override
//  public String getId() {
//    return id;
//  }
//
//  @Override
//  public void setId(String id) {
//    this.id = id;
//  }
//
//  @Nullable
//  @Override
//  public LocalDateTime getCreatedAt() {
//    return createdAt;
//  }
//
//  @Override
//  public void setCreatedAt(LocalDateTime createdAt) {
//    this.createdAt = createdAt;
//  }
//
//  @Nullable
//  @Override
//  public LocalDateTime getUpdatedAt() {
//    return updatedAt;
//  }
//
//  @Override
//  public void setUpdatedAt(LocalDateTime updatedAt) {
//    this.updatedAt = updatedAt;
//  }
//
//  @Override
//  public boolean isReadonly() {
//    return false;
//  }
//
//  @Override
//  public void setReadonly(boolean readonly) {
//
//  }
//
//  @Override
//  public String toString() {
//    return name + "<" + email + ">" + (subscribed ? " подписан" : " отписан");
//  }
//}
