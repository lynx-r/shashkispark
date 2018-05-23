package com.workingbit.share.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.workingbit.share.model.enumarable.EnumEditBoardBoxMode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class EnumEditBoardBoxModeConverter extends JsonDeserializer<EnumEditBoardBoxMode> {
  @Override
  public EnumEditBoardBoxMode deserialize(@NotNull JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    String valueAsString = p.getValueAsString();
    return EnumEditBoardBoxMode.valueOf(valueAsString.toUpperCase());
  }
}