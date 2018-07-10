package com.workingbit.share.util;

import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.domain.impl.NotationHistory;
import com.workingbit.share.model.DomainId;
import com.workingbit.share.model.NotationDrives;
import com.workingbit.share.model.NotationFormat;
import com.workingbit.share.model.enumarable.EnumNotationFormat;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.workingbit.share.model.enumarable.EnumNotation.LPAREN;
import static com.workingbit.share.model.enumarable.EnumNotation.RPAREN;

/**
 * Created by Aleksey Popryaduhin on 12:01 12/08/2017.
 */
public class Utils {

  @NotNull
  public static List<String> ALPH = new ArrayList<>() {{
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

  @NotNull
  public static Map<String, String> ALPHANUMERIC_TO_NUMERIC_64 = new HashMap<>() {{
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

  @NotNull
  public static Map<String, String> NUMERIC_TO_ALPHANUMERIC_64 = new HashMap<>() {{
    put("1", "b8");
    put("2", "d8");
    put("3", "f8");
    put("4", "h8");
    put("5", "a7");
    put("6", "c7");
    put("7", "e7");
    put("8", "g7");
    put("9", "b6");
    put("10", "d6");
    put("11", "f6");
    put("12", "h6");
    put("13", "a5");
    put("14", "c5");
    put("15", "e5");
    put("16", "g5");
    put("17", "b4");
    put("18", "d4");
    put("19", "f4");
    put("20", "h4");
    put("21", "a3");
    put("22", "c3");
    put("23", "e3");
    put("24", "g3");
    put("25", "b2");
    put("26", "d2");
    put("27", "f2");
    put("28", "h2");
    put("29", "a1");
    put("30", "c1");
    put("31", "e1");
    put("32", "g1");
  }};

  @NotNull
  public static Map<String, String> ALPHANUMERIC_TO_NUMERIC_100 = new HashMap<>() {{
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
    put("b2", "41");
    put("d2", "42");
    put("f2", "43");
    put("h2", "44");
    put("j2", "45");
    put("a1", "46");
    put("c1", "47");
    put("e1", "48");
    put("g1", "49");
    put("i1", "50");
  }};

  @Nullable
  @NotNull
  public static Map<String, String> NUMERIC_TO_ALPHANUMERIC_100 = new HashMap<>() {{
    put("1", "b10");
    put("2", "d10");
    put("3", "f10");
    put("4", "h10");
    put("5", "j10");
    put("6", "a9");
    put("7", "c9");
    put("8", "e9");
    put("9", "g9");
    put("10", "i9");
    put("11", "b8");
    put("12", "d8");
    put("13", "f8");
    put("14", "h8");
    put("15", "j8");
    put("16", "a7");
    put("17", "c7");
    put("18", "e7");
    put("19", "g7");
    put("20", "i7");
    put("21", "b6");
    put("22", "d6");
    put("23", "f6");
    put("24", "h6");
    put("25", "j6");
    put("26", "a5");
    put("27", "c5");
    put("28", "e5");
    put("29", "g5");
    put("30", "i5");
    put("31", "b4");
    put("32", "d4");
    put("33", "f4");
    put("34", "h4");
    put("35", "j4");
    put("36", "a3");
    put("37", "c3");
    put("38", "e3");
    put("39", "g3");
    put("40", "i3");
    put("41", "b2");
    put("42", "d2");
    put("43", "f2");
    put("44", "h2");
    put("45", "j2");
    put("46", "a1");
    put("47", "c1");
    put("48", "e1");
    put("49", "g1");
    put("50", "i1");
  }};

  @NotNull
  private static String RANDOM_STR_SEP = "-";
  private static final int RANDOM_STRING_LENGTH_32 = 32;
  public static int RANDOM_STRING_LENGTH_7 = 7;
  private static int RANDOM_ID_LENGTH = 20;

  public static String getRandomID() {
    return getRandomString(RANDOM_ID_LENGTH);
  }

  public static void setRandomIdAndCreatedAt(BaseDomain domain) {
    domain.setId(getRandomID());
    domain.setCreatedAt(LocalDateTime.now());
  }

  public static String getRandomString7() {
    return getSecureRandomString(RANDOM_STRING_LENGTH_7);
  }

  public static String getRandomString32() {
    return getSecureRandomString(RANDOM_STRING_LENGTH_32);
  }

  public static String getRandomString(int length) {
    return getSecureRandomString(length);
  }

  private static String getSecureRandomString(int length) {
    try {
      //Initialize SecureRandom
      //This is a lengthy operation, to be done only upon
      //initialization of the application
      SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");

      //generate a random bytes
      byte[] bytes = new byte[length];
      prng.nextBytes(bytes);

      //get its digest
      return hexEncode(bytes).substring(0, length);
    } catch (NoSuchAlgorithmException ex) {
      return null;
    }
  }

  /**
   * The byte[] returned by MessageDigest does not have a nice
   * textual representation, so some form of encoding is usually performed.
   * <p>
   * This implementation follows the example of David Flanagan's book
   * "Java In A Nutshell", and converts a byte array into a String
   * of hex characters.
   * <p>
   * Another popular alternative is to use a "Base64" encoding.
   */
  static private String hexEncode(byte[] aInput) {
    StringBuilder result = new StringBuilder();
    char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    for (int idx = 0; idx < aInput.length; ++idx) {
      byte b = aInput[idx];
      result.append(digits[(b & 0xf0) >> 4]);
      result.append(digits[b & 0x0f]);
    }
    return result.toString();
  }

  public static String encode(@NotNull byte[] key, String data) throws Exception {
    Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
    SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA256");
    sha256_HMAC.init(secret_key);

    return Hex.encodeHexString(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
  }

  public static void setArticleUrlAndIdAndCreatedAt(Article article, boolean present) {
    article.setId(getRandomID());
    article.setSelectedBoardBoxId(DomainId.getRandomID());
    article.setHumanReadableUrl(article.getHumanReadableUrl() + (present ? RANDOM_STR_SEP + getRandomString4() : ""));
    article.setCreatedAt(LocalDateTime.now());
  }

  private static String getRandomString4() {
    return getRandomString(4);
  }

  public static <T extends BaseDomain> List<T> listObjectsToListT(List<Object> objects, Class<T> clazz) {
    return objects.stream()
        .map(clazz::cast)
        .collect(Collectors.toList());
  }

  public static String listToPdn(@Nullable List<NotationFormat> list, EnumNotationFormat alphanumeric) {
    if (list == null || list.isEmpty()) {
      return "";
    }
    return streamToPdn(list.stream(), alphanumeric).collect(Collectors.joining());
  }

  public static Stream<String> streamToPdn(@Nullable Stream<NotationFormat> stream, EnumNotationFormat notationFormat) {
    if (stream == null) {
      return Stream.empty();
    }
    switch (notationFormat) {
      case SHORT:
        return stream.map(NotationFormat::asStringShort);
      case NUMERIC:
        return stream.map(NotationFormat::asStringNumeric);
      case ALPHANUMERIC:
        return stream.map(NotationFormat::asStringAlphaNumeric);
      default:
        throw new RuntimeException("Формат не распознан");
    }
  }

  public static String notationDrivesToPdn(@Nullable NotationDrives drives, EnumNotationFormat notationFormat) {
    if (drives == null || drives.isEmpty()) {
      return "";
    }
    List<String> pdns = drives
        .stream()
        .map(d -> d.getVariants()
            .stream()
            .map(notationDrive -> notationDrive.asString(notationFormat))
            .collect(Collectors.joining(" ", LPAREN.getPdn(), RPAREN.getPdn()))
        )
        .collect(Collectors.toList());
    return StringUtils.join(pdns, "");
  }

  public static String notationDrivesToTreePdn(@Nullable NotationDrives drives, String indent, String tabulation) {
    if (drives == null || drives.isEmpty()) {
      return "";
    }
    List<String> pdns = drives
        .stream()
        .map(d -> d.getVariants()
            .stream()
            .map(notationDrive -> notationDrive.asTree(indent, tabulation))
            .collect(Collectors.joining("", "\n", ""))
        )
        .collect(Collectors.toList());
    return StringUtils.join(pdns, "");
  }

  public static long getTimestamp() {
    return new Date().getTime();
  }

  public static String listToTreePdn(List<NotationFormat> notationDrives, String indent, String tabulation) {
    return notationDrives
        .stream()
        .map(s -> indent + s.asTree(indent, tabulation))
        .collect(Collectors.joining("\n"));
  }

  public static String getRandomColor() {
    var letters = "3456789ABC";
    StringBuilder color = new StringBuilder("#");
    for (var i = 0; i < 6; i++) {
      color.append(letters.charAt((int) Math.floor(Math.random() * 10)));
    }
    return color.toString();
  }

  public static boolean isCorrespondedNotation(NotationHistory toSyncNotationHist, NotationHistory notationHistory) {
    return !notationHistory.getId().equals(toSyncNotationHist.getId()) && toSyncNotationHist.getCurrentIndex() < notationHistory.size();
  }

  public static String getRandomEmail() {
    return getRandomString(5) + "@" + getRandomString(5) + "." + getRandomString(3);
  }
}
