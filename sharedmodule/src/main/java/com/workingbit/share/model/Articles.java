package com.workingbit.share.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.model.enumarable.EnumArticleStatus;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Aleksey Popryaduhin on 19:31 01/10/2017.
 */
@JsonTypeName("Articles")
@Data
public class Articles implements Payload {
  private List<Article> articles;

  public Articles() {
    this.articles = new ArrayList<>();
  }

  public Articles(Collection<Article> articles) {
    this.articles = new ArrayList<>(articles);
  }

  public void addAll(@NotNull Collection<Article> articles) {
    this.articles.addAll(articles);
  }

  public void add(Article article) {
    articles.add(article);
  }

  public void replace(Article article) {
    int i = articles.indexOf(article);
    articles.remove(i);
    articles.add(i, article);
  }

  public void setSecureArticles(boolean secure, String userId) {
    setArticles(
        getArticles().stream().filter(article ->
            !secure && article.getArticleStatus().equals(EnumArticleStatus.PUBLISHED)
                || secure && userId.equals(article.getUserId().getId())
        ).collect(Collectors.toList())
    );
  }

  public boolean isEmpty() {
    return articles.isEmpty();
  }

  public boolean contains(Article article) {
    return articles.contains(article);
  }


}
