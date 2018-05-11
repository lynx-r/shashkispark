package com.workingbit.share.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.model.DomainId;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by Aleksey Popryaduhin on 18:06 15/08/2017.
 */
public abstract class BaseDomain implements Serializable, DeepClone, Cloneable {

  public abstract String getId();

  public abstract void setId(String id);

  public abstract LocalDateTime getCreatedAt();

  public abstract void setCreatedAt(LocalDateTime createdAt);

  @DynamoDBIgnore
  @JsonIgnore
  public DomainId getDomainId() {
    return new DomainId(getId(), getCreatedAt());
  }

  public void setDomainId(DomainId domainId) {
    setId(domainId.getId());
    setCreatedAt(domainId.getCreatedAt());
  }

  public abstract LocalDateTime getUpdatedAt();

  public abstract void setUpdatedAt(LocalDateTime createdAt);

  public abstract boolean isReadonly();

  public abstract void setReadonly(boolean readonly);
}
