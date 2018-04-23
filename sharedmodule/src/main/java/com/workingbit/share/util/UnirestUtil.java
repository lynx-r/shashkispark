package com.workingbit.share.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;

import java.io.IOException;

/**
 * Created by Aleksey Popryaduhin on 00:02 28/09/2017.
 */
public class UnirestUtil {

  public static void configureSerialization() {
    Unirest.setObjectMapper(new ObjectMapper() {
      private final com.fasterxml.jackson.databind.ObjectMapper mapper;

      {
        mapper = Utils.configureObjectMapper(new com.fasterxml.jackson.databind.ObjectMapper());
      }

      public <T> T readValue(String value, Class<T> valueType) {
        try {
          return mapper.readValue(value, valueType);
        } catch (IOException e) {
          throw new RuntimeException("Unable to read value: " + value + ".\nMessage: " + e.getMessage());
        }
      }

      public String writeValue(Object value) {
        try {
          return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
          throw new RuntimeException("Unable to write value: " + value + ".\nMessage: " + e.getMessage());
        }
      }
    });
  }
}
