package com.workingbit.share.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import java.io.IOException;

public class JsonUtils {
  private static Logger logger = Logger.getLogger(JsonUtils.class);

  private static final ObjectMapper mapper;

  static {
    mapper = Utils.configureObjectMapper(new ObjectMapper());
  }

  public static String dataToJson(Object data) {
    try {
      return mapper.writeValueAsString(data);
    } catch (IOException e) {
      throw new RuntimeException("IOEXception while mapping object (" + data + ") to JSON.\n" + e.getMessage());
    }
  }

  public static <T> T jsonToData(String json, Class<T> clazz) {
    try {
      return mapper.readValue(json, clazz);
    } catch (IOException e) {
      throw new RuntimeException("IOException while mapping json " + json + ".\n" + e.getMessage());
    }
  }
}