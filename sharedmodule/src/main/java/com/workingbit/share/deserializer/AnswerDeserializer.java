package com.workingbit.share.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.Answer;
import com.workingbit.share.model.CreateArticleResponse;

import java.io.IOException;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * Created by Aleksey Popryaduhin on 20:56 30/09/2017.
 */
public class AnswerDeserializer extends JsonDeserializer<Answer> {

  private ObjectMapper mapper = new ObjectMapper();

  @Override
  public Answer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    JsonNode jsonNode = p.getCodec().readTree(p);
    String classType = jsonNode.get("classType").asText();
    JsonNode body = jsonNode.get("body");
    int code = jsonNode.get("code").asInt();
    Answer.Type type = Answer.Type.valueOf(classType);
    switch (type) {
      case ARTICLE_CREATE: {
        return deserializeObject(code, body.toString(), CreateArticleResponse.class, Answer.Type.ARTICLE_CREATE);
      }
      case ARTICLE: {
        return deserializeObject(code, body.toString(), Article.class, Answer.Type.ARTICLE);
      }
      case ARTICLE_LIST: {
        return deserializeList(code, body.toString(), Answer.Type.ARTICLE_LIST);
      }
      case BOARD_BOX: {
        return deserializeObject(code, body.toString(), BoardBox.class, Answer.Type.BOARD_BOX);
      }
      case BOARD_BOX_LIST: {
        return deserializeList(code, body.toString(), Answer.Type.BOARD_BOX_LIST);
      }
      case ERROR: {
        return new Answer(code, jsonNode.get("error").asText());
      }
      default: {
        Answer answer = new Answer();
        answer.setCode(HTTP_BAD_REQUEST);
        answer.setError("Unable to deserialize " + p);
        answer.setClassType(Answer.Type.ERROR);
        return answer;
      }
    }
  }

  private <T> Answer deserializeObject(int code, String content, Class<T> valueType, Answer.Type type) throws IOException {
    T body = mapper.readValue(content, valueType);
    return new Answer(code, body, type);
  }

  private <T> Answer deserializeList(int code, String content, Answer.Type type) throws IOException {
    TypeReference<List<T>> typeReference = new TypeReference<List<T>>() {};
    List<T> list = mapper.readValue(content, typeReference);
    return new Answer(code, list, type);
  }
}
