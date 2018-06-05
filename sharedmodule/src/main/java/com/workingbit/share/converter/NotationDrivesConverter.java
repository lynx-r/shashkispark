package com.workingbit.share.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.workingbit.share.model.NotationDrives;

import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;

public class NotationDrivesConverter implements DynamoDBTypeConverter<String, NotationDrives> {

  @Override
  public String convert(NotationDrives object) {
    return dataToJson(object);
  }

  @Override
  public NotationDrives unconvert(String object) {
    return jsonToData(object, NotationDrives.class);
  }
}
