package com.workingbit.share.domain.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.model.NotationFen;
import com.workingbit.share.model.NotationLine;
import com.workingbit.share.model.Payload;
import com.workingbit.share.model.enumarable.EnumNotation;
import com.workingbit.share.model.enumarable.EnumNotationFormat;
import com.workingbit.share.model.enumarable.EnumRules;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

import static com.workingbit.share.common.NotationConstants.NOTATION_DEFAULT_TAGS;

/**
 * Created by Aleksey Popryaduhin on 21:30 03/10/2017.
 */
@Getter
@Setter
@Document(collection = Notation.CLASS_NAME)
public class Notation extends BaseDomain implements Payload {

  static final String CLASS_NAME = "Notation";

  private BoardBox boardBox;

  /**
   * Some possible tags:
   * "Игрок белыми"
   * "Игрок черными"
   * "Событие"
   * "Место"
   * "Раунд"
   * "Дата"
   * "Результат"
   * "Тип игры"
   * "#tag"
   */
  private ListOrderedMap<String, String> tags;

  @JsonIgnore
  private Integer prevVariantId;

  private NotationHistory notationHistory;

  @DBRef
  private List<NotationHistory> forkedNotations;

  private NotationFen notationFen;

//  @DynamoDBTyped(value = DynamoDBMapperFieldModel.DynamoDBAttributeType.M)
//  @DynamoDBAttribute(attributeName = "gameType")
//  private GameType gameType;

  private boolean readonly;

  private EnumRules rules;

  private EnumNotationFormat format;

  @Transient
  private EnumNotationFormat[] formats;

  public Notation() {
    setTags(new ListOrderedMap<>());
    notationFen = new NotationFen();
    notationHistory = NotationHistory.createWithRoot();
    forkedNotations = new ArrayList<>();
    format = EnumNotationFormat.ALPHANUMERIC;
    formats = EnumNotationFormat.values();
  }

  @NotNull
  @JsonIgnore
  @DynamoDBIgnore
  private String getAsString(EnumNotationFormat notationFormat) {
    EnumNotationFormat oldNotationFormat = null;
    if (!this.format.equals(notationFormat)) {
      oldNotationFormat = this.format;
      setFormat(notationFormat);
    }
    StringBuilder stringBuilder = new StringBuilder();
    tagsAsString(stringBuilder);
    stringBuilder.append(getGameType());
    stringBuilder.append("\n");
    stringBuilder.append(notationFen.getAsString());
    stringBuilder.append("\n");
    if (!notationHistory.isEmpty()) {
      String moves = notationHistory.notationToPdn(notationFormat);
      stringBuilder.append("\n");
      stringBuilder
          .append(moves)
          .append(EnumNotation.END_GAME_SYMBOL.getPdn());
    }
    String notation = stringBuilder.toString();
    if (!this.format.equals(notationFormat)) {
      setFormat(oldNotationFormat);
    }
    return notation;
  }

  private String getGameType() {
    return "[GameType \"" + rules.getTypeNumber() +
        (notationFen != null ? "," + notationFen.getTurn() : "") +
        ",0,0" +
        ("," + getFormat().getType())
        + ",0\"]";
  }

  @DynamoDBIgnore
  public String getAsStringAlphaNumeric() {
    return getAsString(EnumNotationFormat.ALPHANUMERIC);
  }

  @DynamoDBIgnore
  public String getAsStringNumeric() {
    return getAsString(EnumNotationFormat.NUMERIC);
  }

  @DynamoDBIgnore
  public String getAsStringShort() {
    return getAsString(EnumNotationFormat.SHORT);
  }

  @SuppressWarnings("unused")
  public void setAsString(String ignore) {
  }

  @NotNull
  @DynamoDBIgnore
  public String getAsTreeString() {
    StringBuilder stringBuilder = new StringBuilder();
    tagsAsString(stringBuilder);
    String moves = notationHistory.notationToTreePdn("", "");
    stringBuilder.append("\n")
        .append(moves)
        .append(EnumNotation.END_GAME_SYMBOL.getPdn());
    return stringBuilder.toString();
  }

  @SuppressWarnings("unused")
  public void setAsTreeString(String ignore) {
  }

  public void print() {
    notationHistory.getNotationDrives().forEach(notationDrive -> System.out.println(notationDrive.print("\n")));
  }

  public void setTags(ListOrderedMap<String, String> tags) {
    Map<String, String> map = new LinkedHashMap<>(tags);
    NOTATION_DEFAULT_TAGS.forEach((key, value) -> {
      if (StringUtils.isBlank(map.get(key))) {
        map.put(key, "");
      }
    });
    this.tags = new ListOrderedMap<>();
    this.tags.putAll(map);
  }

  public void setRules(@NotNull EnumRules rules) {
    this.rules = rules;
    notationHistory.setRules(rules);
    if (forkedNotations != null) {
      forkedNotations.replaceAll((nh) -> {
        nh.setRules(rules);
        return nh;
      });
    }
  }

  public void setFormat(EnumNotationFormat format) {
    this.format = format;
    notationHistory.setFormat(format);
    if (forkedNotations != null) {
      forkedNotations.replaceAll((nh) -> {
        nh.setFormat(format);
        return nh;
      });
    }
  }

  private void tagsAsString(@NotNull StringBuilder stringBuilder) {
    if (tags != null && !tags.isEmpty()) {
      tags.forEach((key, value) -> {
            if (!key.equals("FEN")) {
              if (StringUtils.isNotBlank(value)) {
                if (!value.startsWith("\"")) {
                  value = "\"" + value;
                }
                if (!value.endsWith("\"")) {
                  value = value + "\"";
                }
              } else {
                value = "\"\"";
              }
              stringBuilder.append("[")
                  .append(key)
                  .append(" ")
                  .append(value)
                  .append("]")
                  .append("\n");
            }
          }
      );
    }
  }

  public void addForkedNotationHistory(NotationHistory notationHistory) {
    forkedNotations.add(notationHistory);
  }

  public void addForkedNotationHistories(List<NotationHistory> byNotationId) {
    byNotationId.forEach(this::addForkedNotationHistory);
  }

  public Optional<NotationHistory> findNotationHistoryByLine(NotationLine notationLine) {
    return getForkedNotations()
        .stream()
        .filter(notationHistory -> notationHistory.getCurrentIndex().equals(notationLine.getCurrentIndex())
            && notationHistory.getVariantIndex().equals(notationLine.getVariantIndex()))
        .findFirst();
  }

  public void removeForkedNotations(NotationHistory notationHistory) {
    forkedNotations.remove(notationHistory);
  }

  public void syncFormatAndRules() {
    setRules(getRules());
    setFormat(getFormat());
  }
}
