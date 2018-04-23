package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * Created by Aleksey Popryadukhin on 17/04/2018.
 */
@JsonTypeName("Params")
@AllArgsConstructor
@Data
public class ParamPayload implements Payload {
  private Map<String, String> param;
}
