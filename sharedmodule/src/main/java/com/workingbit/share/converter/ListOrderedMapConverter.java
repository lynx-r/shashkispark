package com.workingbit.share.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.map.ListOrderedMap;

import java.io.IOException;

public class ListOrderedMapConverter implements DynamoDBTypeConverter<String, ListOrderedMap<String, String>> {

  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public String convert(ListOrderedMap<String, String> object) {
    try {
      return mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return "";
    }
  }

  @Override
  public ListOrderedMap<String, String> unconvert(String object) {
    try {
      TypeReference<ListOrderedMap<String, String>> typeRef
          = new TypeReference<ListOrderedMap<String, String>>() {};
      return mapper.readValue(object, typeRef);
    } catch (IOException e) {
      e.printStackTrace();
      return new ListOrderedMap<>();
    }
  }
}