package com.workingbit.share.domain.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.workingbit.share.common.DBConstants;
import com.workingbit.share.converter.LocalDateTimeConverter;
import com.workingbit.share.converter.NotationDrivesConverter;
import com.workingbit.share.converter.NotationDrivesDeserializer;
import com.workingbit.share.converter.NotationDrivesSerializer;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.domain.DeepClone;
import com.workingbit.share.model.*;
import com.workingbit.share.model.enumarable.EnumNotationFormat;
import com.workingbit.share.model.enumarable.EnumRules;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by Aleksey Popryaduhin on 10:12 04/10/2017.
 */
@Getter
@Setter
@DynamoDBTable(tableName = DBConstants.NOTATION_HISTORY_TABLE)
public class NotationHistory extends BaseDomain implements DeepClone {

  @DynamoDBHashKey(attributeName = "id")
  private String id;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBRangeKey(attributeName = "createdAt")
  private LocalDateTime createdAt;

  @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
  @DynamoDBAttribute(attributeName = "updatedAt")
  private LocalDateTime updatedAt;

  @DynamoDBTyped(value = DynamoDBMapperFieldModel.DynamoDBAttributeType.M)
  @DynamoDBAttribute(attributeName = "notationId")
  private DomainId notationId;

  @JsonSerialize(using = NotationDrivesSerializer.class)
  @JsonDeserialize(using = NotationDrivesDeserializer.class)
  @DynamoDBTypeConverted(converter = NotationDrivesConverter.class)
  private NotationDrives notation;

  @JsonIgnore
  @DynamoDBTypeConvertedJson(targetType = NotationLine.class)
  @DynamoDBAttribute(attributeName = "notationLine")
  private NotationLine notationLine;

  @DynamoDBAttribute(attributeName = "startMovingFrom")
  private int startMovingFrom;

  public NotationHistory() {
    createWithRoot();
    notationLine = new NotationLine();
  }

  public NotationHistory(NotationDrives notation) {
    this.notation = notation;
    notationLine = new NotationLine();
  }

  @Nullable
  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Nullable
  @Override
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  @Override
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @Nullable
  @Override
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  @Override
  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public boolean isReadonly() {
    return false;
  }

  @SuppressWarnings("unused")
  @Override
  public void setReadonly(boolean readonly) {
  }

  @NotNull
  private static NotationDrives createWithoutRoot(boolean hasRoot) {
    if (hasRoot) {
      NotationDrives notationHistory = NotationDrives.create();
      NotationDrive root = new NotationDrive(true);
      root.setSelected(true);
      notationHistory.add(root);
      return notationHistory;
    } else {
      return NotationDrives.create();
    }
  }

  public NotationDrives getNotation() {
    return notation;
  }

  public void setNotation(NotationDrives notation) {
    this.notation = notation;
  }

  @DynamoDBIgnore
  @JsonIgnore
  public void setNotation(@NotNull List<NotationDrive> notationDrives) {
    this.notation = new NotationDrives(notationDrives);
  }

  public void setRules(@NotNull EnumRules rules) {
    notation.setDimension(rules.getDimension());
  }

  public void setFormat(EnumNotationFormat format) {
    notation.setNotationFormat(format);
  }

  public void add(NotationDrive element) {
    notation.add(element);
  }

  @JsonIgnore
  @DynamoDBIgnore
  public NotationDrive getFirst() {
    return notation.getFirst();
  }

  @JsonIgnore
  @DynamoDBIgnore
  public NotationDrive getLast() {
    return notation.getLast();
  }

  public int size() {
    return notation.size();
  }

  @NotNull
  private NotationDrives subListNotation(int fromIndex, int toIndex) {
    List<NotationDrive> subList = notation.subList(fromIndex, toIndex);
    NotationDrives nd = new NotationDrives();
    nd.addAll(subList);
    return nd;
  }

  @JsonIgnore
  @DynamoDBIgnore
  public boolean isEmpty() {
    return notation.isEmpty();
  }

  @JsonIgnore
  @DynamoDBIgnore
  public NotationDrive get(int index) {
    return notation.get(index);
  }

  public static NotationHistory createWithRoot() {
    NotationHistory notationHistory = new NotationHistory(createWithoutRoot(true));
    notationHistory.setNotationLine(new NotationLine(0, 0));
    return notationHistory;
  }

  public String notationToPdn(EnumNotationFormat notationFormat) {
    switch (notationFormat) {
      case ALPHANUMERIC:
        return notation.asStringAlphaNumeric();
      case SHORT:
        return notation.asStringShort();
      case NUMERIC:
        return notation.asStringNumeric();
      default:
        throw new RuntimeException("Формат нотации не распознан");
    }
  }

  public String notationToTreePdn(String indent, String tabulation) {
    return notation.asTree(indent, tabulation);
  }

  public void printPdn() {
    System.out.println(notationToPdn(EnumNotationFormat.ALPHANUMERIC));
  }

  @JsonIgnore
  @DynamoDBIgnore
  public Optional<DomainId> getLastNotationBoardId() {
    return notation.getLastNotationBoardId();
  }

  @JsonIgnore
  @DynamoDBIgnore
  public Optional<DomainId> getLastNotationBoardIdInVariants() {
    return notation.getLastNotationBoardIdInVariants(getCurrentIndex());
  }

  @JsonIgnore
  @DynamoDBIgnore
  public Optional<NotationMove> getLastMove() {
    return notation.getLastMove();
  }

  @NotNull
  public String debugPdnString() {
    return "\n" +
        "NOTATION: " +
        notation.asStringAlphaNumeric();
  }

  public boolean isEqual(@NotNull NotationHistory that) {
    EnumNotationFormat formatThat = that.getNotation().getFirst().getNotationFormat();
    EnumNotationFormat formatThis = this.getNotation().getFirst().getNotationFormat();
    that.setFormat(EnumNotationFormat.NUMERIC);
    this.setFormat(EnumNotationFormat.NUMERIC);
    boolean equals = that.notationToPdn(EnumNotationFormat.NUMERIC).equals(this.notationToPdn(EnumNotationFormat.NUMERIC));
    that.setFormat(formatThat);
    this.setFormat(formatThis);
    return equals;
  }

  public void syncMoves() {
    notation.getCurrentVariant(getCurrentIndex())
        .ifPresent(notationDrive -> {
          if (getLast().getMoves().size() == 2) {
            if (notationDrive.getNotationNumberInt() == getLast().getNotationNumberInt()) {
              notationDrive.setMoves(getLast().getMoves());
            }
            notationDrive.getLastVariant().setMoves(getLast().getMoves());
          } else {
            notationDrive.addVariant(getLast());
          }
        });
    notation.setLastMoveCursor();
  }

  public void setLastMoveCursor() {
    notation.setLastMoveCursor();
  }

  public void setLastSelected(boolean selected) {
    getLast().setSelected(selected);
  }

  public void setCurrentNotationDrive(Integer currentNotationDrive) {
    notationLine.setCurrentIndex(currentNotationDrive);
  }

  public void setVariantNotationDrive(Integer variantNotationDrive) {
    notationLine.setVariantIndex(variantNotationDrive);
  }

  public Integer getCurrentIndex() {
    return notationLine.getCurrentIndex();
  }

  public void setCurrentIndex(Integer currentIndex) {
    notationLine.setCurrentIndex(currentIndex);
  }

  public Integer getVariantIndex() {
    return notationLine.getVariantIndex();
  }

  public void setVariantIndex(Integer variantIndex) {
    notationLine.setVariantIndex(variantIndex);
  }

  @JsonIgnore
  @DynamoDBIgnore
  public Optional<NotationDrive> getCurrentVariant() {
    return notation.getCurrentVariant(getCurrentIndex() - startMovingFrom);
  }

  @JsonIgnore
  @DynamoDBIgnore
  public Optional<NotationDrive> getPreviousVariant() {
    return notation.getPreviousVariant();
  }

  public void addAll(Collection<NotationDrive> collection) {
    notation.addAll(collection);
  }

  @JsonIgnore
  @DynamoDBIgnore
  public Optional<NotationDrive> getCurrentNotationDrive() {
    if (notation.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(notation.get(getCurrentIndex()));
  }

  public void removeByCurrentIndex() {
    getCurrentNotationDrive()
        .ifPresent(notationDrive ->
            notationDrive.setVariants(notationDrive.filterVariantById(getVariantIndex()))
        );
  }

  public void syncFormatAndDimension() {
    getLast().setNotationFormat(getFirst().getNotationFormat());
    getLast().setBoardDimension(getFirst().getBoardDimension());
  }
}
