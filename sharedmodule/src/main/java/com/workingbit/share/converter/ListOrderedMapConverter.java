package com.workingbit.share.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.map.ListOrderedMap;

import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToDataTypeRef;

public class ListOrderedMapConverter implements DynamoDBTypeConverter<String, ListOrderedMap<String, String>> {

  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public String convert(ListOrderedMap<String, String> object) {
    return dataToJson(object);
  }

  @Override
  public ListOrderedMap<String, String> unconvert(String object) {
    TypeReference<ListOrderedMap<String, String>> typeRef = new TypeReference<>() {
    };
    return jsonToDataTypeRef(object, typeRef);
  }
}