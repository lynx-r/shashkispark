package com.workingbit.share.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.workingbit.share.model.enumarable.EnumRules;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class EnumRulesDeserializer extends JsonDeserializer<EnumRules> {
  @Override
  public EnumRules deserialize(@NotNull JsonParser p, DeserializationContext ctxt) throws IOException {
    String valueAsString = p.getValueAsString();
    return EnumRules.valueOf(valueAsString.toUpperCase());
  }
}