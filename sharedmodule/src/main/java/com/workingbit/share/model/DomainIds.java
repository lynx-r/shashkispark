package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aleksey Popryaduhin on 09:40 28/09/2017.
 */
@JsonTypeName("DomainIds")
@Data
public class DomainIds implements Payload {
  private List<DomainId> ids;

  public DomainIds() {
    this.ids = new ArrayList<>();
  }

  public int size() {
    return ids.size();
  }

  public DomainId get(int index) {
    return ids.get(index);
  }

  public void add(DomainId element) {
    ids.add(element);
  }

  public void addAll(List<DomainId> domainIds) {
    ids.addAll(domainIds);
  }
}
