package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class SimpleFilter {
  private String key;
  private Object value;
  private String operator;
  private String type;

  @JsonCreator
  public SimpleFilter(@JsonProperty("key") String key,
                      @JsonProperty("value") Object value,
                      @JsonProperty("operator") String operator,
                      @JsonProperty("type") String type
  ) {
    this.key = key;
    this.value = value;
    this.operator = StringUtils.isBlank(operator) ? " = " : operator;
    this.type = StringUtils.isBlank(type) ? "S" : type;
  }
}