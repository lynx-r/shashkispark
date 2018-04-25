package com.workingbit.share.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workingbit.share.model.EnumSecureRole;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Aleksey Popryadukhin on 24/04/2018.
 */
public class UserRolesConverter implements DynamoDBTypeConverter<String, Set<EnumSecureRole>> {
  private ObjectMapper mapper = new ObjectMapper();

  @Override
  public String convert(Set<EnumSecureRole> object) {
    try {
      return mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return "";
    }
  }

  @Override
  public Set<EnumSecureRole> unconvert(String object) {
    try {
      TypeReference<Set<EnumSecureRole>> typeRef
          = new TypeReference<Set<EnumSecureRole>>() {};
      return mapper.readValue(object, typeRef);
    } catch (IOException e) {
      e.printStackTrace();
      return new HashSet<>();
    }
  }
}
