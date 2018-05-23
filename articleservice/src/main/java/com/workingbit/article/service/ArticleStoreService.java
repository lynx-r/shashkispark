package com.workingbit.article.service;

import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.model.Articles;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

/**
 * Created by Aleksey Popryadukhin on 17/04/2018.
 */
public class ArticleStoreService {

  private static final String ALL_ARTICLES_KEY = "allArticles";

  private Cache<String, Map> storeArticle;
  private Cache<String, Articles> storeArticles;

  public ArticleStoreService() {
    String article = "article";
    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .withCache(article,
            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Map.class, ResourcePoolsBuilder.heap(10)))
        .build();
    cacheManager.init();

    storeArticle = cacheManager.getCache(article, String.class, Map.class);

    String articles = "articles";
    cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .withCache(articles,
            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Articles.class, ResourcePoolsBuilder.heap(10)))
        .build();
    cacheManager.init();

    storeArticles = cacheManager.getCache(articles, String.class, Articles.class);
  }

  public Optional<Article> get(String userSession, String articleHru, String selectedBoardBoxId) {
    Map map = storeArticle.get(articleHru);
    if (map != null) {
      return Optional.ofNullable((Article) map.get(getKey(userSession, articleHru, selectedBoardBoxId)));
    }
    return Optional.empty();
  }

  public void put(String userSession, @NotNull Article article) {
    Map map = Map.of(getKey(userSession, article.getHumanReadableUrl(), article.getSelectedBoardBoxId().getId()), article);
    storeArticle.put(article.getHumanReadableUrl(), map);
  }

  public void remove(@NotNull Article article) {
    storeArticle.remove(article.getHumanReadableUrl());
  }

  private String getKey(String key, String articleHru, String selectedBoardBoxId) {
    return key + ":" + articleHru + ":" + selectedBoardBoxId;
  }

  public void putAllArticles(Articles articles) {
    storeArticles.put(ALL_ARTICLES_KEY, articles);
  }

  public Optional<Articles> getAllArticles() {
    return Optional.ofNullable(storeArticles.get(ALL_ARTICLES_KEY));
  }
}
