package com.workingbit.board.service;

import com.workingbit.share.domain.impl.BoardBox;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import java.util.Optional;

/**
 * Created by Aleksey Popryadukhin on 17/04/2018.
 */
public class BoardStoreService {

  private Cache<String, BoardBox> store;

  public BoardStoreService() {
    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .withCache("preConfigured",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, BoardBox.class, ResourcePoolsBuilder.heap(10)))
        .build();
    cacheManager.init();

    store = cacheManager.getCache("preConfigured", String.class, BoardBox.class);
  }

  public Optional<BoardBox> get(String key) {
    return Optional.of(store.get(key));
  }

  public void put(String key, BoardBox board) {
    store.put(key, board);
  }
}
