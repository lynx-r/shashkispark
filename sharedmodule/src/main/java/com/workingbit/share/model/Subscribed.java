package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by Aleksey Popryadukhin on 18/06/2018.
 */
@JsonTypeName("Subscribed")
@Data
@AllArgsConstructor
public class Subscribed implements Payload {
  private boolean success;
}
