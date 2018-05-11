package com.workingbit.article.service;

import com.workingbit.share.domain.impl.Article;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import java.util.Optional;

/**
 * Created by Aleksey Popryadukhin on 17/04/2018.
 */
public class ArticleStoreService {

  private Cache<String, Article> store;

  public ArticleStoreService() {
    String article = "article";
    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .withCache(article,
            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Article.class, ResourcePoolsBuilder.heap(10)))
        .build();
    cacheManager.init();

    store = cacheManager.getCache(article, String.class, Article.class);
  }

  public Optional<Article> get(String key, String articleHru, String selectedBoardBoxId) {
    return Optional.ofNullable(store.get(getKey(key, articleHru, selectedBoardBoxId)));
  }

  public void put(String key, Article article) {
    store.put(getKey(key, article.getHumanReadableUrl(), article.getSelectedBoardBoxId().getId()), article);
  }

  private String getKey(String key, String articleHru, String selectedBoardBoxId) {
    return key + ":" + articleHru + ":" + selectedBoardBoxId;
  }
}
