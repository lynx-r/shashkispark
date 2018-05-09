package com.workingbit.share.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workingbit.share.domain.impl.Draught;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Aleksey Popryaduhin on 16:45 22/09/2017.
 */
public class DraughtMapConverter implements DynamoDBTypeConverter<String, HashMap<String, Draught>> {

  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public String convert(HashMap<String, Draught> object) {
    try {
      return mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return "";
    }
  }

  @Override
  public HashMap<String, Draught> unconvert(String object) {
    try {
      TypeReference<HashMap<String, Draught>> typeRef = new TypeReference<>() {
      };
      return mapper.readValue(object, typeRef);
    } catch (IOException e) {
      e.printStackTrace();
      return new HashMap<>();
    }
  }
}
