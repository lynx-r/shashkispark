package com.workingbit.share.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.Answer;

import java.io.IOException;

/**
 * Created by Aleksey Popryaduhin on 20:56 30/09/2017.
 */
public class AnswerDeserializer extends JsonDeserializer<Answer> {

  private ObjectMapper mapper = new ObjectMapper();

  @Override
  public Answer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    Answer answer = new Answer();
    JsonNode jsonNode = p.getCodec().readTree(p);
    String classType = jsonNode.get("classType").asText();
    JsonNode body = jsonNode.get("body");
    Answer.Type type = Answer.Type.valueOf(classType);
    switch (type) {
      case BOARD_BOX: {
        String content = body.toString();
        BoardBox boardBox = mapper.readValue(content, BoardBox.class);
        answer.setBody(boardBox);
        return answer;
      }
      case ARTICLE: {
        String content = body.toString();
        Article article = mapper.readValue(content, Article.class);
        answer.setBody(article);
        return answer;
      }
      case ERROR: {
        answer.setError(jsonNode.get("error").asText());
        return answer;
      }
      default: {
        answer.setError("Unable to deserialize " + p);
        return answer;
      }
    }
  }
}
