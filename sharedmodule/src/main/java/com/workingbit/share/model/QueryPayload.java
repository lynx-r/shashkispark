package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import spark.QueryParamsMap;

/**
 * Created by Aleksey Popryadukhin on 17/04/2018.
 */
@JsonTypeName("query")
@AllArgsConstructor
@Data
public class QueryPayload implements Payload {
  private QueryParamsMap query;
}
