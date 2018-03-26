package com.workingbit.share.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.workingbit.share.converter.LocalDateTimeConverter;
import com.workingbit.share.model.Payload;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by Aleksey Popryaduhin on 18:06 15/08/2017.
 */
public interface BaseDomain extends Serializable, DeepClone, Cloneable, Payload {

  String getId();

  void setId(String id);

  LocalDateTime getCreatedAt();

  void setCreatedAt(LocalDateTime createdAt);
}
