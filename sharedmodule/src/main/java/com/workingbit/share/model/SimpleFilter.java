package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SimpleFilter {
  private String key;
  private String value;

  @JsonCreator
  public SimpleFilter(@JsonProperty("key") String key,
                      @JsonProperty("value") String value
  ) {
    this.key = key;
    this.value = value;
  }
}