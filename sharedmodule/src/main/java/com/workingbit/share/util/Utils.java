package com.workingbit.share.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.CreateBoardPayload;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomStringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Aleksey Popryaduhin on 12:01 12/08/2017.
 */
public class Utils {

  public static List<String> alph = new ArrayList<String>() {{
    add("a");
    add("b");
    add("c");
    add("d");
    add("e");
    add("f");
    add("g");
    add("h");
    add("i");
    add("j");
  }};
  private static String RANDOM_STR_SEP = "-";
  private static int COUNT_RANDOM_STR = 10;

  public static boolean isBlank(String s) {
    if (s == null) {
      return true;
    }
    for (int i = 0; i < s.length(); i++) {
      if (!Character.isWhitespace(s.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  public static String getRandomUUID() {
    return UUID.randomUUID().toString();
  }

  public static void setRandomIdAndCreatedAt(BaseDomain domain) {
    domain.setId(getRandomUUID());
    domain.setCreatedAt(LocalDateTime.now());
  }

  public static String getRandomString() {
    return RandomStringUtils.randomAlphanumeric(COUNT_RANDOM_STR);
  }

  public static String encode(String key, String data) throws Exception {
    Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
    SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
    sha256_HMAC.init(secret_key);

    return Hex.encodeHexString(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
  }

  public static void setArticleIdAndCreatedAt(Article article, boolean present) {
    article.setId(article.getTitle() + (present ? RANDOM_STR_SEP + getRandomString() : ""));
    article.setCreatedAt(LocalDateTime.now());
  }

  public static void setBoardIdAndCreatedAt(Board board, String articleId, String boardBoxId) {
    board.setId(articleId + RANDOM_STR_SEP + boardBoxId + RANDOM_STR_SEP + getRandomString());
    board.setCreatedAt(LocalDateTime.now());
  }

  public static void setBoardBoxIdAndCreatedAt(BoardBox boardBox, CreateBoardPayload createBoardPayload) {
    boardBox.setId(createBoardPayload.getArticleId() +
        RANDOM_STR_SEP + createBoardPayload.getBoardBoxId() +
        RANDOM_STR_SEP + getRandomString());
    boardBox.setCreatedAt(LocalDateTime.now());
  }

  public static ObjectMapper configureObjectMapper(ObjectMapper mapper) {
    mapper.registerModule(new JavaTimeModule());
    mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
    return mapper.configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, false);
  }
}
