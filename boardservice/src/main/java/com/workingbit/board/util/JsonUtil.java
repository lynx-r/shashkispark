package com.workingbit.board.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.StringWriter;

import static com.workingbit.board.BoardApplication.appProperties;

public class JsonUtil {
  private static final ObjectMapper mapper = new ObjectMapper();

  public static String dataToJson(Object data) {
    try {
      if (appProperties.test()) {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, data);
        return sw.toString();
      } else {
        return mapper.writeValueAsString(data);
      }
    } catch (IOException e) {
      throw new RuntimeException("IOEXception while mapping object (" + data + ") to JSON");
    }
  }

  public static <T> T jsonToData(String json, Class<T> clazz) {
    try {
      return mapper.readValue(json, clazz);
    } catch (IOException e) {
      throw new RuntimeException("IOException while mapping json " + json);
    }
  }
}