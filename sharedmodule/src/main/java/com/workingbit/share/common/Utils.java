package com.workingbit.share.common;

import com.workingbit.share.domain.BaseDomain;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.Date;
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
    domain.setCreatedAt(new Date());
  }

  public static String randomString() {
    return String.valueOf(RandomUtils.nextLong());
  }
}
