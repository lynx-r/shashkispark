package com.workingbit.board.service;

import com.workingbit.share.domain.impl.Notation;
import com.workingbit.share.domain.impl.NotationHistory;
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

  private Cache<String, Map> notationStore;
  private Cache<String, Map> notationHistoryStore;

  public NotationStoreService() {
    String notation = "notation";
    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .withCache(notation,
            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Map.class, ResourcePoolsBuilder.heap(10)))
        .build();
    cacheManager.init();

    notationStore = cacheManager.getCache(notation, String.class, Map.class);

    String notationHistory = "notationHistory";
    cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .withCache(notationHistory,
            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Map.class, ResourcePoolsBuilder.heap(10)))
        .build();
    cacheManager.init();

    notationHistoryStore = cacheManager.getCache(notationHistory, String.class, Map.class);
  }

  public Optional<Notation> getNotation(String userSession, @NotNull DomainId notationId) {
    Map map = notationStore.get(notationId.getId());
    if (map != null) {
      return Optional.ofNullable((Notation) map.get(getKey(userSession, notationId.getId())));
    }
    return Optional.empty();
  }

  public void putNotation(String userSession, @NotNull Notation notation) {
    String key = getKey(userSession, notation.getId());
    Map map = Map.of(key, notation);
    notationStore.put(notation.getId(), map);
  }

  public void removeNotation(@NotNull Notation notation) {
    notationStore.remove(notation.getId());
  }

  public void removeNotationById(@NotNull DomainId notationId) {
    notationStore.remove(notationId.getId());
  }

  public Optional<NotationHistory> getNotationHistory(String userSession, @NotNull DomainId notationHistoryId) {
    Map map = notationHistoryStore.get(notationHistoryId.getId());
    if (map != null) {
      return Optional.ofNullable((NotationHistory) map.get(getKey(userSession, notationHistoryId.getId())));
    }
    return Optional.empty();
  }

  public void putNotationHistory(String userSession, @NotNull NotationHistory notationHistory) {
    String key = getKey(userSession, notationHistory.getId());
    Map map = Map.of(key, notationHistory);
    notationHistoryStore.put(notationHistory.getId(), map);
  }

  public void removeNotationHistory(@NotNull NotationHistory notationHistory) {
    notationHistoryStore.remove(notationHistory.getId());
  }

  private String getKey(String key, String boardId) {
    return key + ":" + boardId;
  }
}
