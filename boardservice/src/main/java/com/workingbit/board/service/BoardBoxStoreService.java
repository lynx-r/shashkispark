package com.workingbit.board.service;

import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.DomainId;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import java.util.Optional;

/**
 * Created by Aleksey Popryadukhin on 17/04/2018.
 */
public class BoardBoxStoreService {

  private Cache<String, BoardBox> store;

  public BoardBoxStoreService() {
    String board = "board";
    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .withCache(board,
            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, BoardBox.class, ResourcePoolsBuilder.heap(10)))
        .build();
    cacheManager.init();

    store = cacheManager.getCache(board, String.class, BoardBox.class);
  }

  public Optional<BoardBox> get(String key, DomainId boardBoxId) {
    return Optional.ofNullable(store.get(getKey(key, boardBoxId.getId())));
  }

  public void put(String key, BoardBox board) {
    store.put(getKey(key, board.getId()), board);
  }

  private String getKey(String key, String boardId) {
    return key + ":" + boardId;
  }
}
