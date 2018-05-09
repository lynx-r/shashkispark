package com.workingbit.share.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.workingbit.share.model.DomainIds;

import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;

public class DomainIdsConverter implements DynamoDBTypeConverter<String, DomainIds> {

  @Override
  public String convert(DomainIds object) {
    return dataToJson(object);
  }

  @Override
  public DomainIds unconvert(String object) {
    return jsonToData(object, DomainIds.class);
  }
}
