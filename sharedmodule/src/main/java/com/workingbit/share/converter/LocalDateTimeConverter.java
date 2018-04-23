package com.workingbit.share.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeConverter implements DynamoDBTypeConverter<String, LocalDateTime> {
  @Override
  public String convert(final LocalDateTime time) {
    return time.toString();
  }

  @Override
  public LocalDateTime unconvert(final String stringValue) {
    return LocalDateTime.parse(stringValue, DateTimeFormatter.ISO_DATE_TIME);
  }
}