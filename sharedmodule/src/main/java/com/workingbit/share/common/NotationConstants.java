package com.workingbit.share.common;

import org.apache.commons.collections4.map.ListOrderedMap;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Aleksey Popryaduhin on 18:06 03/10/2017.
 */
public class NotationConstants {

  @Nullable
  public static final ListOrderedMap<String, String> NOTATION_DEFAULT_TAGS = new ListOrderedMap<>() {{
    put("Event","");
    put("Site","");
    put("Date","");
    put("Round","");
    put("White","");
    put("Black","");
    put("Result","");
    put("FEN","");
  }};
}
