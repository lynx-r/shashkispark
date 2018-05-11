package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.converter.LocalDateTimeConverter;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.util.Utils;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Created by Aleksey Popryaduhin on 09:40 28/09/2017.
 */
@JsonTypeName("DomainId")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DomainId extends BaseDomain implements Payload {

  @DynamoDBAttribute(attributeName = "id")
  private String id;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBAttribute(attributeName = "createdAt")
  private LocalDateTime createdAt;

  public DomainId(BaseDomain domain) {
    this.id = domain.getId();
    this.createdAt = domain.getCreatedAt();
  }

  @DynamoDBIgnore
  @JsonIgnore
  public static DomainId getRandomID() {
    return new DomainId(Utils.getRandomID(), LocalDateTime.now());
  }

  @DynamoDBIgnore
  @Override
  public LocalDateTime getUpdatedAt() {
    return null;
  }

  @Override
  public void setUpdatedAt(LocalDateTime createdAt) {
  }

  @DynamoDBIgnore
  @Override
  public boolean isReadonly() {
    return false;
  }

  @Override
  public void setReadonly(boolean readonly) {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DomainId)) return false;
    DomainId domainId = (DomainId) o;
    return Objects.equals(id, domainId.id);
  }

  @Override
  public int hashCode() {

    return Objects.hash(id);
  }
}
