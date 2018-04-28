package com.workingbit.share.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workingbit.share.model.enumarable.EnumAuthority;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Aleksey Popryadukhin on 24/04/2018.
 */
public class UserRolesConverter implements DynamoDBTypeConverter<String, Set<EnumAuthority>> {
  private ObjectMapper mapper = new ObjectMapper();

  @Override
  public String convert(Set<EnumAuthority> object) {
    try {
      return mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return "";
    }
  }

  @Override
  public Set<EnumAuthority> unconvert(String object) {
    try {
      TypeReference<Set<EnumAuthority>> typeRef
          = new TypeReference<Set<EnumAuthority>>() {};
      return mapper.readValue(object, typeRef);
    } catch (IOException e) {
      e.printStackTrace();
      return new HashSet<>();
    }
  }
}
