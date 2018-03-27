package com.workingbit.share.converter;

///**
// * Created by Aleksey Popryaduhin on 20:56 30/09/2017.
// */
//public class AnswerDeserializer extends JsonDeserializer<Answer> {
//
//  private ObjectMapper mapper = new ObjectMapper();
//
//  @Override
//  public Answer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
//    JsonNode jsonNode = p.getCodec().readTree(p);
//    String classType = jsonNode.get("type").asText();
//    JsonNode body = jsonNode.get("body");
//    int code = jsonNode.get("code").asInt();
//    Answer.Type type = Answer.Type.valueOf(classType);
//    switch (type) {
//      case ARTICLE_CREATE: {
//        return deserializeObject(code, body.toString(), CreateArticleResponse.class, Answer.Type.ARTICLE_CREATE);
//      }
//      case ARTICLE: {
//        return deserializeObject(code, body.toString(), Article.class, Answer.Type.ARTICLE);
//      }
//      case ARTICLE_LIST: {
//        return deserializeObject(code, body.toString(), Articles.class, Answer.Type.ARTICLE_LIST);
//      }
//      case BOARD_BOX: {
//        return deserializeObject(code, body.toString(), BoardBox.class, Answer.Type.BOARD_BOX);
//      }
//      case BOARD_BOX_LIST: {
//        return deserializeObject(code, body.toString(), BoardBoxes.class, Answer.Type.BOARD_BOX_LIST);
//      }
//      case ERROR: {
//        return new Answer(code, jsonNode.get("message").asText());
//      }
//      default: {
//        Answer answer = new Answer();
//        answer.setCode(HTTP_BAD_REQUEST);
//        answer.setMessage("Unable to deserialize " + p);
//        answer.setType(Answer.Type.ERROR);
//        return answer;
//      }
//    }
//  }
//
//  private <T> Answer deserializeObject(int code, String content, Class<T> valueType, Answer.Type type) throws IOException {
//    T body = mapper.readValue(content, valueType);
//    return new Answer(code, body, type);
//  }
//}
