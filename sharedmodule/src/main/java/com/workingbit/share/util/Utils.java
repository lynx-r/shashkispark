package com.workingbit.share.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.CreateBoardPayload;
import com.workingbit.share.model.ToPdn;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomStringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by Aleksey Popryaduhin on 12:01 12/08/2017.
 */
public class Utils {

  public static List<String> ALPH = new ArrayList<String>() {{
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

  public static Map<String, String> ALPHANUMERIC64_TO_NUMERIC64 = new HashMap<String, String>() {{
    put("b8", "1");
    put("d8", "2");
    put("f8", "3");
    put("h8", "4");
    put("a7", "5");
    put("c7", "6");
    put("e7", "7");
    put("g7", "8");
    put("b6", "9");
    put("d6", "10");
    put("f6", "11");
    put("h6", "12");
    put("a5", "13");
    put("c5", "14");
    put("e5", "15");
    put("g5", "16");
    put("b4", "17");
    put("d4", "18");
    put("f4", "19");
    put("h4", "20");
    put("a3", "21");
    put("c3", "22");
    put("e3", "23");
    put("g3", "24");
    put("b2", "25");
    put("d2", "26");
    put("f2", "27");
    put("h2", "28");
    put("a1", "29");
    put("c1", "30");
    put("e1", "31");
    put("g1", "32");
  }};

  public static Map<String, String> ALPHANUMERIC64_TO_NUMERIC100 = new HashMap<String, String>() {{
    put("b10", "1");
    put("d10", "2");
    put("f10", "3");
    put("h10", "4");
    put("j10", "5");
    put("a9", "6");
    put("c9", "7");
    put("e9", "8");
    put("g9", "9");
    put("i9", "10");
    put("b8", "11");
    put("d8", "12");
    put("f8", "13");
    put("h8", "14");
    put("j8", "15");
    put("a7", "16");
    put("c7", "17");
    put("e7", "18");
    put("g7", "19");
    put("i7", "20");
    put("b6", "21");
    put("d6", "22");
    put("f6", "23");
    put("h6", "24");
    put("j6", "25");
    put("a5", "26");
    put("c5", "27");
    put("e5", "28");
    put("g5", "29");
    put("i5", "30");
    put("b4", "31");
    put("d4", "32");
    put("f4", "33");
    put("h4", "34");
    put("j4", "35");
    put("a3", "36");
    put("c3", "37");
    put("e3", "38");
    put("g3", "39");
    put("i3", "40");
    put("a2", "41");
    put("c2", "42");
    put("e2", "43");
    put("g2", "44");
    put("i2", "45");
    put("a1", "46");
    put("c1", "47");
    put("e1", "48");
    put("g1", "49");
    put("i1", "50");
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
    board.setBoardBoxId(boardBoxId);
    board.setId(articleId + RANDOM_STR_SEP + boardBoxId + RANDOM_STR_SEP + getRandomString());
    board.setCreatedAt(LocalDateTime.now());
  }

  public static void setBoardBoxIdAndCreatedAt(BoardBox boardBox, CreateBoardPayload createBoardPayload) {
    boardBox.setId(createBoardPayload.getArticleId() +
        RANDOM_STR_SEP + createBoardPayload.getBoardBoxId() +
        RANDOM_STR_SEP + getRandomString());
    boardBox.setCreatedAt(LocalDateTime.now());
  }

  public static void setBoardBoxIdAndCreatedAt(BoardBox boardBox, String articleId, String boardBoxId) {
    boardBox.setId(articleId +
        RANDOM_STR_SEP + boardBoxId +
        RANDOM_STR_SEP + getRandomString());
    boardBox.setCreatedAt(LocalDateTime.now());
  }

  public static ObjectMapper configureObjectMapper(ObjectMapper mapper) {
    mapper.registerModule(new JavaTimeModule());
//    mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
    mapper = mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.findAndRegisterModules();
    return mapper;
  }

  public static <T extends BaseDomain> List<T> listObjectsToListT(List<Object> objects, Class<T> clazz) {
    return objects.stream()
        .map(clazz::cast)
        .collect(Collectors.toList());
  }

  public static String listToPdn(List<ToPdn> list) {
    if (list == null || list.isEmpty()) {
      return "";
    }
    AtomicInteger i = new AtomicInteger();
    return list.stream()
        .map(s -> {
          String pdn = s.toPdn();
          i.getAndIncrement();
          if (i.get() > 3) {
            pdn = pdn.trim();
            pdn += "\n";
            i.set(0);
          }
          return pdn;
        }).collect(Collectors.joining());
  }
}
