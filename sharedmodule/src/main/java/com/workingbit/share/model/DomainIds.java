package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

import java.util.LinkedList;

/**
 * Created by Aleksey Popryaduhin on 09:40 28/09/2017.
 */
@JsonTypeName("DomainIds")
@Data
public class DomainIds implements Payload {
  private LinkedList<DomainId> ids;

  public DomainIds() {
    this.ids = new LinkedList<>();
  }

  public DomainIds(LinkedList<DomainId> domainIds) {
    this.ids = new LinkedList<>(domainIds);
  }

  public int size() {
    return ids.size();
  }

  public DomainId get(int index) {
    return ids.get(index);
  }

  public boolean add(DomainId elem) {
    return ids.add(elem);
  }

  @DynamoDBIgnore
  @JsonIgnore
  public boolean isEmpty() {
    return ids.isEmpty();
  }

  public void removeFirst() {
    if (!ids.isEmpty()) {
      ids.removeFirst();
    }
  }
}
