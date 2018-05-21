package com.workingbit.share.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.workingbit.share.model.Boards;

import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;

public class BoardsConverter implements DynamoDBTypeConverter<String, Boards> {

  @Override
  public String convert(Boards object) {
    return dataToJson(object);
  }

  @Override
  public Boards unconvert(String object) {
    return jsonToData(object, Boards.class);
  }
}
