package com.workingbit.board.service;

import com.workingbit.share.domain.impl.Notation;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import java.util.Optional;

/**
 * Created by Aleksey Popryadukhin on 17/04/2018.
 */
public class NotationStoreService {

  private Cache<String, Notation> store;

  public NotationStoreService() {
    String notation = "notation";
    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .withCache(notation,
            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Notation.class, ResourcePoolsBuilder.heap(10)))
        .build();
    cacheManager.init();

    store = cacheManager.getCache(notation, String.class, Notation.class);
  }

  public Optional<Notation> get(String key, String notationId) {
    return Optional.ofNullable(store.get(getKey(key, notationId)));
  }

  public void put(String key, Notation notation) {
    store.put(getKey(key, notation.getId()), notation);
  }

  private String getKey(String key, String boardId) {
    return key + ":" + boardId;
  }
}
