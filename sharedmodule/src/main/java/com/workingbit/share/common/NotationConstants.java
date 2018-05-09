package com.workingbit.share.common;

import org.apache.commons.collections4.map.ListOrderedMap;

/**
 * Created by Aleksey Popryaduhin on 18:06 03/10/2017.
 */
public class NotationConstants {

  public static final String NEW_LINE = "#";
  public static final String STROKE = "-";
  public static final String CAPTURE = ":";
  public static final String NOTATION_DOT_NUMBER = ". ";
  public static final String SPACE = " ";
  public static final String END_GAME_SYMBOL = "*";

  public static final ListOrderedMap<String, String> NOTATION_DEFAULT_TAGS = new ListOrderedMap<>() {{
    put("Игрок белыми","");
    put("Игрок черными","");
    put("Событие","");
    put("Место","");
    put("Раунд","");
    put("Дата","");
    put("Результат","");
    put("Тип игры","");
    put("#tag","");
  }};
}
