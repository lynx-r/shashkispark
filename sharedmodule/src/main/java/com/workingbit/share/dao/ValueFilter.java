package com.workingbit.share.dao;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.exception.DaoException;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.workingbit.share.common.SharedProperties.sharedProperties;
import static com.workingbit.share.util.Utils.getRandomString;

@JsonTypeName("ValueFilter")
@Data
public class ValueFilter implements BaseFilter {
  private String key;
  private Object value;
  private String operator;
  private String dataType;
  private String stringValue;
  private static final List<String> VALID_FILTER_KEYS = sharedProperties.validFilters();;

  @JsonCreator
  public ValueFilter(@JsonProperty("key") String key,
                     @JsonProperty("value") Object value,
                     @JsonProperty("operator") String operator,
                     @JsonProperty("type") String dataType) {
    this.key = key;
    this.value = value;
    this.operator = StringUtils.isBlank(operator) ? "=" : operator;
    this.dataType = StringUtils.isBlank(dataType) ? "S" : dataType;
  }

  @Override
  public String asString() {
    return stringValue;
  }

  @JsonIgnore
  @Override
  public boolean isValid() {
    boolean notBlankAndNotNull = StringUtils.isNotBlank(key) && !key.contains("null");
    boolean validKey;
    if (VALID_FILTER_KEYS != null) {
      validKey = VALID_FILTER_KEYS.stream().anyMatch((p) -> p.equals(key));
    } else {
      throw new DaoException(0, "PATTERN FILTERS ARE NOT SET");
    }
    if (notBlankAndNotNull && validKey) {
      return true;
    }
    throw new DaoException(1, "InvalidFilter");
  }

  @SuppressWarnings("unchecked")
  @Override
  public void updateEav(@NotNull Map<String, AttributeValue> eav) {
    String sub = formatSub(key);
    if (eav.containsKey(sub)) {
      sub += getRandomString(3);
    }
    AttributeValue attributeValue = new AttributeValue();
    switch (dataType) {
      case "BOOL":
        attributeValue.setBOOL((Boolean) value);
        break;
      case "S":
        attributeValue.setS((String) value);
        break;
      case "SS":
        attributeValue.setSS((Collection<String>) value);
        break;
      case "M":
        attributeValue.setM((Map<String, AttributeValue>) value);
        break;
    }
    stringValue = key + " " + operator + " " + sub;
    eav.put(sub, attributeValue);
  }

  private String formatSub(String sub) {
    return ":" + sub.toLowerCase().replace(".", "");
  }
}