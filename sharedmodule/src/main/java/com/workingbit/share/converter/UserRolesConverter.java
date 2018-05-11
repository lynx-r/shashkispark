package com.workingbit.share.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workingbit.share.model.enumarable.EnumAuthority;

import java.util.Set;

import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToDataTypeRef;

/**
 * Created by Aleksey Popryadukhin on 24/04/2018.
 */
public class UserRolesConverter implements DynamoDBTypeConverter<String, Set<EnumAuthority>> {
  @Override
  public String convert(Set<EnumAuthority> object) {
    return dataToJson(object);
  }

  @Override
  public Set<EnumAuthority> unconvert(String object) {
    TypeReference<Set<EnumAuthority>> typeRef
        = new TypeReference<>() {
    };
    return jsonToDataTypeRef(object, typeRef);
  }
}
