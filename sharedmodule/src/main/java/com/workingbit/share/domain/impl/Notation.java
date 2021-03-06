package com.workingbit.share.domain.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.common.DBConstants;
import com.workingbit.share.converter.ListOrderedMapConverter;
import com.workingbit.share.converter.LocalDateTimeConverter;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.model.DomainId;
import com.workingbit.share.model.NotationFen;
import com.workingbit.share.model.NotationLine;
import com.workingbit.share.model.Payload;
import com.workingbit.share.model.enumarable.EnumNotation;
import com.workingbit.share.model.enumarable.EnumNotationFormat;
import com.workingbit.share.model.enumarable.EnumRules;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.*;

import static com.workingbit.share.common.NotationConstants.NOTATION_DEFAULT_TAGS;

/**
 * Created by Aleksey Popryaduhin on 21:30 03/10/2017.
 */
@Getter
@Setter
@ToString
@DynamoDBTable(tableName = DBConstants.NOTATION_TABLE)
public class Notation extends BaseDomain implements Payload {

  @DynamoDBHashKey(attributeName = "id")
  private String id;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBRangeKey(attributeName = "createdAt")
  private LocalDateTime createdAt;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBAttribute(attributeName = "updatedAt")
  private LocalDateTime updatedAt;

  @DynamoDBTyped(value = DynamoDBMapperFieldModel.DynamoDBAttributeType.M)
  @DynamoDBAttribute(attributeName = "boardBoxId")
  private DomainId boardBoxId;

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
  @DynamoDBTypeConverted(converter = ListOrderedMapConverter.class)
  @DynamoDBAttribute(attributeName = "tags")
  private ListOrderedMap<String, String> tags;

  @DynamoDBTyped(value = DynamoDBMapperFieldModel.DynamoDBAttributeType.M)
  @DynamoDBAttribute(attributeName = "notationHistoryId")
  private DomainId notationHistoryId;

  @JsonIgnore
  @DynamoDBAttribute(attributeName = "prevVariantId")
  private Integer prevVariantId;

  @DynamoDBIgnore
  private NotationHistory notationHistory;

  @DynamoDBIgnore
  private Map<String, NotationHistory> forkedNotations;

  @DynamoDBTyped(value = DynamoDBMapperFieldModel.DynamoDBAttributeType.M)
  @DynamoDBAttribute(attributeName = "notationFen")
  private NotationFen notationFen;

//  @DynamoDBTyped(value = DynamoDBMapperFieldModel.DynamoDBAttributeType.M)
//  @DynamoDBAttribute(attributeName = "gameType")
//  private GameType gameType;

  @DynamoDBAttribute(attributeName = "readonly")
  private boolean readonly;

  @DynamoDBTypeConvertedEnum
  @DynamoDBAttribute(attributeName = "rules")
  private EnumRules rules;

  @DynamoDBTypeConvertedEnum
  @DynamoDBAttribute(attributeName = "format")
  private EnumNotationFormat format;

  @DynamoDBIgnore
  private EnumNotationFormat[] formats;

  public Notation() {
    setTags(new ListOrderedMap<>());
    notationFen = new NotationFen();
    notationHistory = NotationHistory.createWithRoot();
    forkedNotations = new HashMap<>();
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
    notationHistory.getNotation().forEach(notationDrive -> System.out.println(notationDrive.print("\n")));
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
      forkedNotations.replaceAll((s, nh) -> {
        nh.setRules(rules);
        return nh;
      });
    }
  }

  public void setFormat(EnumNotationFormat format) {
    this.format = format;
    notationHistory.setFormat(format);
    if (forkedNotations != null) {
      forkedNotations.replaceAll((s, nh) -> {
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
    if (forkedNotations == null) {
      forkedNotations = new HashMap<>();
    }
    forkedNotations.put(notationHistory.getId(), notationHistory);
  }

  public void addForkedNotationHistories(List<NotationHistory> byNotationId) {
    byNotationId.forEach(this::addForkedNotationHistory);
  }

  public Optional<NotationHistory> findNotationHistoryByLine(NotationLine notationLine) {
    return getForkedNotations().values()
        .stream()
        .filter(notationHistory -> notationHistory.getCurrentIndex().equals(notationLine.getCurrentIndex())
            && notationHistory.getVariantIndex().equals(notationLine.getVariantIndex()))
        .findFirst();
  }

  public void removeForkedNotations(NotationHistory notationHistory) {
    forkedNotations.remove(notationHistory.getId());
  }

  public void syncFormatAndRules() {
    setRules(getRules());
    setFormat(getFormat());
  }
}
