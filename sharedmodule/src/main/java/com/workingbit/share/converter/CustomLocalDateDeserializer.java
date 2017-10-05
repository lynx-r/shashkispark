package com.workingbit.share.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.workingbit.share.common.DBConstants;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by Aleksey Popryaduhin on 10:24 05/10/2017.
 */
public class CustomLocalDateDeserializer extends JsonDeserializer<LocalDate> {
  @Override
  public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    String string = p.getText();
    if(string.length() > 20) {
      ZonedDateTime zonedDateTime = ZonedDateTime.parse(string);
      return zonedDateTime.toLocalDate();
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DBConstants.DEFAULT_DATE_FORMAT);
    return LocalDate.parse(string, formatter);
  }
}
