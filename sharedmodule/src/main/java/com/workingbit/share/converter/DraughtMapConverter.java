package com.workingbit.share.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.workingbit.share.domain.impl.Draught;

import java.util.HashMap;

import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToDataTypeRef;

/**
 * Created by Aleksey Popryaduhin on 16:45 22/09/2017.
 */
public class DraughtMapConverter implements DynamoDBTypeConverter<String, HashMap<String, Draught>> {

  @Override
  public String convert(HashMap<String, Draught> object) {
    return dataToJson(object);
  }

  @Override
  public HashMap<String, Draught> unconvert(String object) {
      TypeReference<HashMap<String, Draught>> typeRef = new TypeReference<>() {
      };
      return jsonToDataTypeRef(object, typeRef);
  }
}
