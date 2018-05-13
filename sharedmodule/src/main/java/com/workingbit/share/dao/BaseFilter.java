package com.workingbit.share.dao;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Map;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ValueFilter.class, name = "ValueFilter"),
    @JsonSubTypes.Type(value = Unary.class, name = "Unary"),
})
public interface BaseFilter {
  String asString();

  boolean isValid();

  void updateEav(Map<String, AttributeValue> eav);
}
