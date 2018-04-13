package com.workingbit.share.model;

import lombok.Data;
import org.apache.commons.collections4.map.ListOrderedMap;

/**
 * Created by Aleksey Popryaduhin on 21:30 03/10/2017.
 */
@Data
public class Notation implements ToPdn {

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
  private ListOrderedMap<String, String> tags;

  private EnumRules rules;

  private NotationHistory notationHistory;

  public Notation() {
    tags = new ListOrderedMap<String, String>() {{
      put("Игрок белыми", "");
      put("Игрок черными", "");
      put("Событие", "");
      put("Место", "");
      put("Раунд", "");
      put("Дата", "");
      put("Результат", "");
      put("Тип игры", "");
      put("#tag", "");
    }};
    notationHistory = NotationHistory.createWithRoot();
  }

  public Notation(ListOrderedMap<String, String> tags, EnumRules rules, NotationHistory notationHistory) {
    this();
    this.tags = tags;
    this.rules = rules;
    this.notationHistory = notationHistory;
  }

//  @JsonAnySetter
//  public void add(String key, String value) {
//    if (tags == null) {
//      tags = new ListOrderedMap<>();
//    }
//    tags.put(0, key, value);
//  }
//
//  @JsonAnyGetter
//  public ListOrderedMap<String, String> getTags() {
//    return tags;
//  }

  public String toPdn() {
    StringBuilder stringBuilder = new StringBuilder();
    if (tags != null && !tags.isEmpty()) {
      tags.forEach((key, value) -> stringBuilder.append("[")
          .append(key)
          .append(" ")
          .append(value)
          .append("]")
          .append("\n")
      );
    }
    String moves = notationHistory.variantsToPdn();
    stringBuilder.append("\n")
        .append(moves)
        .append(EnumNotation.END_GAME_SYMBOL.getPdn());
    return stringBuilder.toString();
  }

  public void print() {
    notationHistory.getNotation().forEach(notationDrive -> System.out.println(notationDrive.print("\n")));
  }
}
