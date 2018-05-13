package com.workingbit.share.common;

import java.time.format.DateTimeFormatter;

/**
 * Created by Aleksey Popryaduhin on 10:15 13/08/2017.
 */
public class DBConstants {
  public static final String BOARD_BOX_TABLE = "SHASHKIWIKI_BoardBox";
  public static final String ARTICLE_TABLE = "SHASHKIWIKI_Article";
  public static final String BOARD_TABLE = "SHASHKIWIKI_Board";
  public static final String NOTATION_TABLE = "SHASHKIWIKI_Notation";
  public static final String SECURE_USER_TABLE = "SHASHKIWIKI_SecureUser";
  public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
}
