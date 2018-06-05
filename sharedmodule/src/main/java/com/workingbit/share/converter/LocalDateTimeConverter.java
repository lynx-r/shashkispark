package com.workingbit.share.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

import static com.workingbit.share.common.DBConstants.DATE_TIME_FORMATTER;

public class LocalDateTimeConverter implements DynamoDBTypeConverter<String, LocalDateTime> {
  @NotNull
  @Override
  public String convert(@NotNull final LocalDateTime time) {
    return time.toString();
  }

  @NotNull
  @Override
  public LocalDateTime unconvert(@NotNull final String stringValue) {
    return LocalDateTime.parse(stringValue, DATE_TIME_FORMATTER);
  }
}