package com.workingbit.share.domain.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.model.Payload;
import com.workingbit.share.model.enumarable.EnumArticleStatus;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * Created by Aleksey Popryaduhin on 18:31 09/08/2017.
 */
@JsonTypeName(Article.CLASS_NAME)
@Getter
@Setter
@Document(collection = Article.CLASS_NAME)
public class Article extends BaseDomain implements Payload {

  public static final String CLASS_NAME = "Article";

  @Size(max = 500)
  private String humanReadableUrl;

  @Size(max = 200)
  private String author;

  private String userId;

  @Size(max = 1000)
  private String title;

  @Size(max = 50000)
  private String content;

  @Transient
  private String html;

  @Size(max = 1000)
  private String intro;

  private BoardBox selectedBoardBox;

  private int boardBoxCount;

  private EnumArticleStatus articleStatus;

  public Article() {
  }

  public Article(String author, String title, String intro, String content) {
    this();
    this.author = author;
    this.title = title;
    this.intro = intro;
    this.content = content;
  }

  @JsonCreator
  public Article(@JsonProperty("author") String author,
                 @JsonProperty("title") String title,
                 @JsonProperty("intro") String intro,
                 @JsonProperty("content") String content,
                 @JsonProperty("selectedBoardBox") BoardBox selectedBoardBox,
                 @JsonProperty("articleStatus") EnumArticleStatus articleStatus,
                 @JsonProperty("humanReadableUrl") String humanReadableUrl
  ) {
    this.author = author;
    this.title = title;
    this.intro = intro;
    this.content = content;
    this.selectedBoardBox = selectedBoardBox;
    this.articleStatus = articleStatus;
    this.humanReadableUrl = humanReadableUrl;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Article article = (Article) o;
    return Objects.equals(getId(), article.getId());
  }

  @Override
  public int hashCode() {

    return Objects.hash(getId());
  }
}
