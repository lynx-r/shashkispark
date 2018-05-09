package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.workingbit.share.domain.BaseDomain;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Created by Aleksey Popryaduhin on 09:40 28/09/2017.
 */
@NoArgsConstructor
@Getter
@Setter
public class DomainId extends BaseDomain {
  private String id;
  private LocalDateTime createdAt;

  public DomainId(BaseDomain domain) {
    this.id = domain.getId();
    this.createdAt = domain.getCreatedAt();
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
}
