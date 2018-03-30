package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.workingbit.share.common.NotationConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aleksey Popryaduhin on 21:30 03/10/2017.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Notation {

  /**
   * Some possible tags:
   * whitePlayer,
   * blackPlayer,
   * event,
   * site,
   * round,
   * date,
   * result,
   * gameType,
   */
  private Map<String, String> tags = new HashMap<>();

  private EnumRules rules;

  private NotationDrives notationDrives = new NotationDrives();

  @JsonAnySetter
  public void add(String key, String value) {
    tags.put(key, value);
  }

  @JsonAnyGetter
  public Map<String, String> getTags() {
    return tags;
  }

  public String toPdn() {
    StringBuilder stringBuilder = new StringBuilder();
    tags.forEach((key, value) -> stringBuilder.append("[")
        .append(key)
        .append(" ")
        .append(value)
        .append("]")
        .append("\n")
    );
    String moves = notationDrives.toPdn();
    stringBuilder.append("\n")
        .append(moves)
    .append(NotationConstants.END_GAME_SYMBOL);
    return stringBuilder.toString();
  }

  public void print() {
    notationDrives.forEach(notationStroke -> {
      System.out.println();
      System.out.println(notationStroke.print(""));
    });
  }
}
