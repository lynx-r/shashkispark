package com.workingbit.share.domain.impl;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.model.Payload;
import com.workingbit.share.model.enumarable.EnumEditBoardBoxMode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Aleksey Popryaduhin on 19:54 12/08/2017.
 */
@JsonTypeName(BoardBox.CLASS_NAME)
@Getter
@Setter
@Document(collection = BoardBox.CLASS_NAME)
public class BoardBox extends BaseDomain implements Payload {

  public static final String CLASS_NAME = "BoardBox";

  private Article article;

  /**
   * Id of boardBox in article. Used to find board box by this id in preview article
   */
  private int idInArticle;

  private int taskIdInArticle;

  private Board board;

  /**
   * Используется в публичном API для хранения ВСЕХ досок boardbox'a
   */
  @DBRef
  private List<Board> publicBoards;

  private Notation notation;

  private EnumEditBoardBoxMode editMode;

  /**
   * Показывать ли эту доску в просмотре статьи когда пользователь не залогинен
   */
  private boolean removed;

  /**
   * Whether this BoardBox contains task
   */
  private boolean task;

  public BoardBox() {
    editMode = EnumEditBoardBoxMode.EDIT;
    publicBoards = new ArrayList<>();
  }

  public BoardBox(@NotNull Board board) {
    this();
    this.board = board;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    BoardBox boardBox = (BoardBox) o;
    return Objects.equals(getId(), boardBox.getId());
  }

  @Override
  public int hashCode() {

    return Objects.hash(super.hashCode(), getId());
  }
}
