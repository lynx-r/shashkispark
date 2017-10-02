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
    // Only one time
    Unirest.setObjectMapper(new ObjectMapper() {
      private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
          = new com.fasterxml.jackson.databind.ObjectMapper();

      public <T> T readValue(String value, Class<T> valueType) {
        try {
          return jacksonObjectMapper.readValue(value, valueType);
        } catch (IOException e) {
          throw new RuntimeException("Unable to read value: " + value + ". Message: " + e.getMessage());
        }
      }

      public String writeValue(Object value) {
        try {
          return jacksonObjectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
          throw new RuntimeException("Unable to write value: " + value + ". Message: " + e.getMessage());
        }
      }
    });
  }
}
