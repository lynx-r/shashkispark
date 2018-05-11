package com.workingbit.share.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.workingbit.share.model.NotationHistory;

import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;

/**
 * Created by Aleksey Popryadukhin on 24/04/2018.
 */
public class NotationHistoryConverter implements DynamoDBTypeConverter<String, NotationHistory> {
  @Override
  public String convert(NotationHistory object) {
    return dataToJson(object);
  }

  @Override
  public NotationHistory unconvert(String object) {
    return jsonToData(object, NotationHistory.class);
  }
}
