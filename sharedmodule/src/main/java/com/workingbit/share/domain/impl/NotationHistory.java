package com.workingbit.share.domain.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.domain.DeepClone;
import com.workingbit.share.model.NotationDrive;
import com.workingbit.share.model.NotationDrives;
import com.workingbit.share.model.NotationLine;
import com.workingbit.share.model.NotationMove;
import com.workingbit.share.model.enumarable.EnumNotationFormat;
import com.workingbit.share.model.enumarable.EnumRules;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by Aleksey Popryaduhin on 10:12 04/10/2017.
 */
@Getter
@Setter
@Document(collection = NotationHistory.CLASS_NAME)
public class NotationHistory extends BaseDomain implements DeepClone {

  static final String CLASS_NAME = "NotationHistory";

  private String notationId;

  private NotationDrives notationDrives;

  private NotationLine notationLine;

  private int startMovingFrom;

  public NotationHistory() {
    createWithRoot();
    notationLine = new NotationLine();
  }

  public NotationHistory(NotationDrives notationDrives) {
    this.notationDrives = notationDrives;
    notationLine = new NotationLine();
  }

  // todo move to service
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

  public NotationDrives getNotationDrives() {
    return notationDrives;
  }

  public void setNotationDrives(NotationDrives notationDrives) {
    this.notationDrives = notationDrives;
  }

  @DynamoDBIgnore
  @JsonIgnore
  public void setNotationId(@NotNull List<NotationDrive> notationDrives) {
    this.notationDrives = new NotationDrives(notationDrives);
  }

  public void setRules(@NotNull EnumRules rules) {
    notationDrives.setDimension(rules.getDimension());
  }

  public void setFormat(EnumNotationFormat format) {
    notationDrives.setNotationFormat(format);
  }

  public void add(NotationDrive element) {
    notationDrives.add(element);
  }

  @JsonIgnore
  @DynamoDBIgnore
  public NotationDrive getFirst() {
    return notationDrives.getFirst();
  }

  @JsonIgnore
  @DynamoDBIgnore
  public NotationDrive getLast() {
    return notationDrives.getLast();
  }

  public int size() {
    return notationDrives.size();
  }

  @NotNull
  private NotationDrives subListNotation(int fromIndex, int toIndex) {
    List<NotationDrive> subList = notationDrives.subList(fromIndex, toIndex);
    NotationDrives nd = new NotationDrives();
    nd.addAll(subList);
    return nd;
  }

  @JsonIgnore
  @DynamoDBIgnore
  public boolean isEmpty() {
    return notationDrives.isEmpty();
  }

  @JsonIgnore
  @DynamoDBIgnore
  public NotationDrive get(int index) {
    return notationDrives.get(index);
  }

  public static NotationHistory createWithRoot() {
    NotationHistory notationHistory = new NotationHistory(createWithoutRoot(true));
    notationHistory.setNotationLine(new NotationLine(0, 0));
    return notationHistory;
  }

  public String notationToPdn(EnumNotationFormat notationFormat) {
    switch (notationFormat) {
      case ALPHANUMERIC:
        return notationDrives.asStringAlphaNumeric();
      case SHORT:
        return notationDrives.asStringShort();
      case NUMERIC:
        return notationDrives.asStringNumeric();
      default:
        throw new RuntimeException("Формат нотации не распознан");
    }
  }

  public String notationToTreePdn(String indent, String tabulation) {
    return notationDrives.asTree(indent, tabulation);
  }

  public void printPdn() {
    System.out.println(notationToPdn(EnumNotationFormat.ALPHANUMERIC));
  }

  @JsonIgnore
  @DynamoDBIgnore
  public String getLastNotationBoardId() {
    return notationDrives.getLastNotationBoardId();
  }

  @JsonIgnore
  @DynamoDBIgnore
  public Optional<String> getLastNotationBoardIdInVariants() {
    return notationDrives.getLastNotationBoardIdInVariants(getCurrentIndex());
  }

  @JsonIgnore
  @DynamoDBIgnore
  public Optional<NotationMove> getLastMove() {
    return notationDrives.getLastMove();
  }

  @NotNull
  public String debugPdnString() {
    return "\n" +
        "NOTATION: " +
        notationDrives.asStringAlphaNumeric();
  }

  public boolean isEqual(@NotNull NotationHistory that) {
    EnumNotationFormat formatThat = that.getNotationDrives().getFirst().getNotationFormat();
    EnumNotationFormat formatThis = this.getNotationDrives().getFirst().getNotationFormat();
    that.setFormat(EnumNotationFormat.NUMERIC);
    this.setFormat(EnumNotationFormat.NUMERIC);
    boolean equals = that.notationToPdn(EnumNotationFormat.NUMERIC).equals(this.notationToPdn(EnumNotationFormat.NUMERIC));
    that.setFormat(formatThat);
    this.setFormat(formatThis);
    return equals;
  }

  public void syncMoves() {
    notationDrives.getCurrentVariant(getCurrentIndex())
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
    notationDrives.setLastMoveCursor();
  }

  public void setLastMoveCursor() {
    notationDrives.setLastMoveCursor();
  }

  public void setLastSelected(boolean selected) {
    notationDrives.replaceAll(notationDrive -> {
      notationDrive.setSelected(false);
      return notationDrive;
    });
    getLast().setSelected(selected);
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
    return notationDrives.getCurrentVariant(getCurrentIndex());
  }

  @JsonIgnore
  @DynamoDBIgnore
  public Optional<NotationDrive> getPreviousVariant() {
    return notationDrives.getPreviousVariant();
  }

  public void addAll(Collection<NotationDrive> collection) {
    notationDrives.addAll(collection);
  }

  @JsonIgnore
  @DynamoDBIgnore
  public Optional<NotationDrive> getCurrentNotationDrive() {
    if (notationDrives.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(notationDrives.get(getCurrentIndex()));
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
