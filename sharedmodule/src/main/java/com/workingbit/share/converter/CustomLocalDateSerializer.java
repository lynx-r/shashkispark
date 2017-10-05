package com.workingbit.share.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.workingbit.share.common.DBConstants;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by Aleksey Popryaduhin on 10:07 05/10/2017.
 */
public class CustomLocalDateSerializer extends JsonSerializer<LocalDate> {

  @Override
  public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    if (value == null) {
      gen.writeNull();
      return;
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DBConstants.DEFAULT_DATE_FORMAT);
    String date = value.format(formatter);
    gen.writeString(date);
  }
}
