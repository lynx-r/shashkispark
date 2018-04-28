package com.workingbit.share.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workingbit.share.model.SimpleFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class JsonUtils {
  private static Logger logger = LoggerFactory.getLogger(JsonUtils.class);

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

  public static List<SimpleFilter> jsonToFilters(String filterValues) {
    try {
      TypeReference<List<SimpleFilter>> typeRef = new TypeReference<>() {
      };
      return mapper.readValue(filterValues, typeRef);
    } catch (IOException e) {
      throw new RuntimeException("IOException while mapping json " + filterValues + ".\n" + e.getMessage());
    }
  }
}