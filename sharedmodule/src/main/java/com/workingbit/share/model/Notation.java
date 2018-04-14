package com.workingbit.share.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedEnum;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.common.DBConstants;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.map.ListOrderedMap;

/**
 * Created by Aleksey Popryaduhin on 21:30 03/10/2017.
 */
@JsonTypeName("notation")
@Getter
@Setter
@ToString
@DynamoDBTable(tableName = DBConstants.NOTATION_TABLE)
public class Notation implements ToPdn {

  @DynamoDBHashKey(attributeName = "id")
  private String id;

  @DynamoDBAttribute(attributeName = "boardBoxId")
  private String boardBoxId;

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
  @DynamoDBAttribute(attributeName = "tags")
  private ListOrderedMap<String, String> tags;

  @DynamoDBTypeConvertedEnum
  @DynamoDBAttribute(attributeName = "rules")
  private EnumRules rules;

  private NotationHistory notationHistory;

  public Notation() {
    tags = new ListOrderedMap<>();
    notationHistory = NotationHistory.createWithRoot();
  }

  public Notation(ListOrderedMap<String, String> tags, EnumRules rules, NotationHistory notationHistory) {
    this();
    this.tags = tags;
    this.rules = rules;
    this.notationHistory = notationHistory;
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
