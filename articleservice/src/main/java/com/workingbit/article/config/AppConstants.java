package com.workingbit.article.config;

import com.workingbit.share.model.enumarable.EnumArticleStatus;

import java.util.regex.Pattern;

import static com.workingbit.share.util.Utils.RANDOM_ID_LENGTH;

/**
 * Created by Aleksey Popryadukhin on 08/05/2018.
 */
public class AppConstants {
  public static String[] VALID_FILTER_KEYS = new String[] {
      "articleStatus",
      "userId"
  };
  public static Pattern[] VALID_FILTER_VALUES = new Pattern[] {
      Pattern.compile(EnumArticleStatus.DRAFT.name()),
      Pattern.compile(EnumArticleStatus.PUBLISHED.name()),
      Pattern.compile(EnumArticleStatus.REMOVED.name()),
      Pattern.compile(String.format("^\\p{ASCII}{%s}$", RANDOM_ID_LENGTH))
  };
}
