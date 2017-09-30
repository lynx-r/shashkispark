package com.workingbit.share.handler;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workingbit.share.domain.BaseDomain;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.util.Map;

public abstract class AbstractRequestHandler<V extends Validable> implements RequestHandler<V>, Route {

  private Class<V> valueClass;
  protected BaseDomain domain;
  private static ObjectMapper mapper = new ObjectMapper();

  private static final int HTTP_BAD_REQUEST = 400;

  public AbstractRequestHandler(Class<V> valueClass, BaseDomain domain) {
    this.valueClass = valueClass;
    this.domain = domain;
  }

  public static String dataToJson(Object data) {
    try {
      return mapper.writeValueAsString(data);
    } catch (IOException e) {
      throw new RuntimeException("IOException from a StringWriter?");
    }
  }

  public final Answer process(V value, Map<String, String> urlParams) {
    if (value != null && !value.isValid()) {
      return new Answer(HTTP_BAD_REQUEST);
    } else {
      return processImpl(value, urlParams);
    }
  }

  protected abstract Answer processImpl(V value, Map<String, String> urlParams);


  @Override
  public Object handle(Request request, Response response) throws Exception {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      V value = null;
      if (valueClass != EmptyPayload.class) {
        value = objectMapper.readValue(request.body(), valueClass);
      }
      Map<String, String> urlParams = request.params();
      Answer answer = process(value, urlParams);
      response.status(answer.getCode());
      response.type("application/json");
      response.body(answer.getBody());
      return answer.getBody();
    } catch (JsonMappingException e) {
      response.status(HTTP_BAD_REQUEST);
      response.body(e.getMessage());
      return e.getMessage();
    }
  }

}