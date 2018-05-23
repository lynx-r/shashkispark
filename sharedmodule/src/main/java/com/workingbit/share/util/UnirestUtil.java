package com.workingbit.share.util;

import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import org.jetbrains.annotations.NotNull;

import static com.workingbit.share.util.JsonUtils.dataToJson;
import static com.workingbit.share.util.JsonUtils.jsonToData;

/**
 * Created by Aleksey Popryaduhin on 00:02 28/09/2017.
 */
public class UnirestUtil {

  public static void configureSerialization() {
    Unirest.setTimeouts(3 * 60 * 1000, 3 * 60 * 1000);
    Unirest.setObjectMapper(new ObjectMapper() {

      public <T> T readValue(String value, @NotNull Class<T> valueType) {
        return jsonToData(value, valueType);
      }

      public String writeValue(Object value) {
        return dataToJson(value);
      }
    });
  }
}
