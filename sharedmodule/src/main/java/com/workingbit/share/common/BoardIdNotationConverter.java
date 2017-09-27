package com.workingbit.share.common;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workingbit.share.model.BoardIdNotation;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by Aleksey Popryaduhin on 13:59 24/09/2017.
 */
public class BoardIdNotationConverter implements DynamoDBTypeConverter<String, LinkedList<BoardIdNotation>> {

  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public String convert(LinkedList<BoardIdNotation> object) {
    try {
      return mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      Log.error(e.getMessage());
      return "";
    }
  }

  @Override
  public LinkedList<BoardIdNotation> unconvert(String object) {
    try {
      TypeReference<LinkedList<BoardIdNotation>> typeRef
          = new TypeReference<LinkedList<BoardIdNotation>>() {};
      return mapper.readValue(object, typeRef);
    } catch (IOException e) {
      Log.error(e.getMessage());
      return new LinkedList<>();
    }
  }
}
