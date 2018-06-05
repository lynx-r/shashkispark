//package com.workingbit.share.converter;
//
//import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
//import com.workingbit.share.domain.impl.NotationHistory;
//
//import static com.workingbit.share.util.JsonUtils.dataToJson;
//import static com.workingbit.share.util.JsonUtils.jsonToData;
//
///**
// * Created by Aleksey Popryadukhin on 25/05/2018.
// */
//public class NotationLineConverter implements DynamoDBTypeConverter<String, NotationHistory.NotationLine> {
//  @Override
//  public String convert(NotationHistory.NotationLine object) {
//    return dataToJson(object);
//  }
//
//  @Override
//  public NotationHistory.NotationLine unconvert(String object) {
//    return jsonToData(object, NotationHistory.NotationLine.class);
//  }
//}
