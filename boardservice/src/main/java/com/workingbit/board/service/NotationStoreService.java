package com.workingbit.board.service;

import com.workingbit.share.domain.impl.Notation;
import com.workingbit.share.model.DomainId;
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
public class NotationStoreService {

  private Cache<String, Map> store;

  public NotationStoreService() {
    String notation = "notation";
    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .withCache(notation,
            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Map.class, ResourcePoolsBuilder.heap(10)))
        .build();
    cacheManager.init();

    store = cacheManager.getCache(notation, String.class, Map.class);
  }

  public Optional<Notation> get(String userSession, @NotNull DomainId notationId) {
    Map map = store.get(notationId.getId());
    if (map != null) {
      return Optional.ofNullable((Notation) map.get(getKey(userSession, notationId.getId())));
    }
    return Optional.empty();
  }

  public void put(String userSession, @NotNull Notation notation) {
    String key = getKey(userSession, notation.getId());
    Map map = Map.of(key, notation);
    store.put(notation.getId(), map);
  }

  public void remove(@NotNull Notation notation) {
    store.remove(notation.getId());
  }

  private String getKey(String key, String boardId) {
    return key + ":" + boardId;
  }
}
