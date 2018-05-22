package com.workingbit.article.service;

import com.workingbit.share.domain.impl.Article;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import java.util.Map;
import java.util.Optional;

/**
 * Created by Aleksey Popryadukhin on 17/04/2018.
 */
public class ArticleStoreService {

  private Cache<String, Map> store;

  public ArticleStoreService() {
    String article = "article";
    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .withCache(article,
            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Map.class, ResourcePoolsBuilder.heap(10)))
        .build();
    cacheManager.init();

    store = cacheManager.getCache(article, String.class, Map.class);
  }

  public Optional<Article> get(String userSession, String articleHru, String selectedBoardBoxId) {
    Map map = store.get(articleHru);
    if (map != null) {
      return Optional.ofNullable((Article) map.get(getKey(userSession, articleHru, selectedBoardBoxId)));
    }
    return Optional.empty();
  }

  public void put(String userSession, Article article) {
    Map map = Map.of(getKey(userSession, article.getHumanReadableUrl(), article.getSelectedBoardBoxId().getId()), article);
    store.put(article.getHumanReadableUrl(), map);
  }

  public void remove(String articleId) {
    store.remove(articleId);
  }

  private String getKey(String key, String articleHru, String selectedBoardBoxId) {
    return key + ":" + articleHru + ":" + selectedBoardBoxId;
  }
}
