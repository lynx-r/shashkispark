package com.workingbit.share.dao;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

import java.util.Map;

@JsonTypeName("Unary")
@Data
public class Unary implements BaseFilter {

  private String condition;

  @JsonCreator
  public Unary(@JsonProperty("condition") String condition) {
    this.condition = condition;
  }

  @Override
  public String asString() {
    return condition;
  }

  @JsonIgnore
  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public void updateEav(Map<String, AttributeValue> eav) {
  }
}
