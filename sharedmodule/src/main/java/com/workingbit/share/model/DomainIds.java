package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
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

  public void add(DomainId id) {
    ids.add(id);
  }

  public void addAll(Collection<DomainId> ids) {
    this.ids.addAll(ids);
  }
}
