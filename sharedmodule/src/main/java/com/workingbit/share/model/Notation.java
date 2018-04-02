package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
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

  private NotationDrivesContainer notationDrivesContainer;

  public Notation() {
    tags = new ListOrderedMap<>();
    notationDrivesContainer = new NotationDrivesContainer();
  }

  public Notation(ListOrderedMap<String, String> tags, EnumRules rules, NotationDrivesContainer notationDrivesContainer) {
    this();
    this.tags = tags;
    this.rules = rules;
    this.notationDrivesContainer = notationDrivesContainer;
  }

  @JsonAnySetter
  public void add(String key, String value) {
    if (tags == null) {
      tags = new ListOrderedMap<>();
    }
    tags.put(0, key, value);
  }

  @JsonAnyGetter
  public ListOrderedMap<String, String> getTags() {
    return tags;
  }

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
    String moves = notationDrivesContainer.getVariants().toPdn();
    stringBuilder.append("\n")
        .append(moves)
        .append(NotationDrive.EnumNotation.END_GAME_SYMBOL.getPdn());
    return stringBuilder.toString();
  }

  public void print() {
    notationDrivesContainer.getVariants().forEach(notationDrive -> System.out.println(notationDrive.print("\n")));
  }
}
