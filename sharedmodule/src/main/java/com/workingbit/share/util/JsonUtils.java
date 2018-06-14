package com.workingbit.share.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.workingbit.share.converter.EnumRulesDeserializer;
import com.workingbit.share.model.enumarable.EnumRules;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.workingbit.share.common.SharedProperties.sharedProperties;

public class JsonUtils {
  private static Logger logger = LoggerFactory.getLogger(JsonUtils.class);

  @NotNull
  public static final ObjectMapper mapper;

  static {
    mapper = configureObjectMapper();
  }

  @NotNull
  private static ObjectMapper configureObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();

    mapper.registerModule(new JavaTimeModule());
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
    mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    SimpleModule module = new SimpleModule();
    module.addDeserializer(EnumRules.class, new EnumRulesDeserializer());
//    module.addDeserializer(NotationDrives.class, new NotationDrivesDeserializer());
//    module.addSerializer(NotationDrives.class, new NotationDrivesSerializer());
    mapper.registerModule(module);

    mapper.findAndRegisterModules();
    InjectableValues inject = new InjectableValues.Std().addValue("validFilterKeys", sharedProperties.validFilters());
    mapper.reader(inject);
    return mapper;
  }

  public static String dataToJson(Object data) {
    try {
      return mapper.writeValueAsString(data);
    } catch (IOException e) {
      throw new RuntimeException("IOEXception while mapping object (" + data + ") to JSON.\n" + e.getMessage());
    }
  }

  public static <T> T jsonToData(String json, @NotNull Class<T> clazz) {
    try {
      return mapper.readValue(json, clazz);
    } catch (IOException e) {
      throw new RuntimeException("IOException while mapping json " + json + ".\n" + e.getMessage());
    }
  }

  public static <T> T jsonToDataTypeRef(String json, @NotNull TypeReference typeRef) {
    try {
      return mapper.readValue(json, typeRef);
    } catch (IOException e) {
      throw new RuntimeException("IOException while mapping json " + json + ".\n" + e.getMessage());
    }
  }
}