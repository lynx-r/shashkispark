package com.workingbit.share.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.workingbit.share.model.DomainId;

import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;

public class DomainIdConverter implements DynamoDBTypeConverter<String, DomainId> {

  @Override
  public String convert(DomainId object) {
    return dataToJson(object);
  }

  @Override
  public DomainId unconvert(String object) {
    return jsonToData(object, DomainId.class);
  }
}
