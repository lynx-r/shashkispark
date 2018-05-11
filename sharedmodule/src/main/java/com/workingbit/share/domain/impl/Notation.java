package com.workingbit.share.domain.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.workingbit.share.common.DBConstants;
import com.workingbit.share.converter.ListOrderedMapConverter;
import com.workingbit.share.converter.LocalDateTimeConverter;
import com.workingbit.share.converter.NotationHistoryConverter;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.model.DomainId;
import com.workingbit.share.model.NotationHistory;
import com.workingbit.share.model.Payload;
import com.workingbit.share.model.ToPdn;
import com.workingbit.share.model.enumarable.EnumNotation;
import com.workingbit.share.model.enumarable.EnumRules;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.workingbit.share.common.NotationConstants.NOTATION_DEFAULT_TAGS;

/**
 * Created by Aleksey Popryaduhin on 21:30 03/10/2017.
 */
//@JsonTypeName("Notation")
@Getter
@Setter
@ToString
@DynamoDBTable(tableName = DBConstants.NOTATION_TABLE)
public class Notation extends BaseDomain implements Payload, ToPdn {

  @DynamoDBHashKey(attributeName = "id")
  private String id;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBRangeKey(attributeName = "createdAt")
  private LocalDateTime createdAt;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBAttribute(attributeName = "updatedAt")
  private LocalDateTime updatedAt;

  @DynamoDBTyped(value = DynamoDBMapperFieldModel.DynamoDBAttributeType.M)
  @DynamoDBAttribute(attributeName = "selectedBoardBoxId")
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

  @DynamoDBTypeConvertedEnum
  @DynamoDBAttribute(attributeName = "rules")
  private EnumRules rules;

  @DynamoDBTypeConverted(converter = NotationHistoryConverter.class)
  @DynamoDBAttribute(attributeName = "notationHistory")
  private NotationHistory notationHistory;

  @DynamoDBAttribute(attributeName = "readonly")
  private boolean readonly;

  public Notation() {
    setTags(new ListOrderedMap<>());
    notationHistory = NotationHistory.createWithRoot();
  }

//  public Notation(ListOrderedMap<String, String> tags, EnumRules rules, NotationHistory notationHistory) {
//    this();
//    setTags(tags);
//    this.rules = rules;
//    this.notationHistory = notationHistory;
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
}
