package com.workingbit.share.dao;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Aleksey Popryadukhin on 14/05/2018.
 */
@Getter
@Setter
public class DaoFilters {

  private LinkedList<BaseFilter> filters;
  @JsonIgnore
  private Map<String, AttributeValue> eav;

  public DaoFilters() {
    filters = new LinkedList<>();
    eav = new HashMap<>();
  }

  @JsonCreator
  public DaoFilters(@JsonProperty("filters") LinkedList<BaseFilter> filters) {
    this.filters = filters == null ? new LinkedList<>() : filters;
    eav = new HashMap<>();
  }

  public DaoFilters add(BaseFilter filter) {
    filters.add(filter);
    return this;
  }

  public DaoFilters add(int index, BaseFilter filter) {
    filters.add(index, filter);
    return this;
  }

  public Map<String, Object> build() {
    return Map.of(
        "eav", eav,
        "expression", filters
            .stream()
            .filter(BaseFilter::isValid)
            .peek(filter -> filter.updateEav(eav))
            .map(BaseFilter::asString)
            .collect(Collectors.joining(" "))
    );
  }

  @JsonIgnore
  public boolean isEmpty() {
    return filters.isEmpty();
  }

  public DaoFilters addAll(DaoFilters filters) {
    this.filters.addAll(filters.filters);
    return this;
  }

  public int size() {
    return filters.size();
  }

  public boolean containsKey(String key) {
    return filters.stream().anyMatch(filter ->
        filter instanceof ValueFilter
            && ((ValueFilter) filter).getKey().equals(key)
    );
  }

  public void removeLast() {
    filters.removeLast();
  }
}
