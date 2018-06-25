package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by Aleksey Popryadukhin on 17/04/2018.
 */
@JsonTypeName("Result")
@AllArgsConstructor
@Data
public class ResultPayload implements Payload {
  private boolean success;
}
