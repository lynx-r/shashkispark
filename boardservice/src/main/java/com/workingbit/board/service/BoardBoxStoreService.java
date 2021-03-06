//package com.workingbit.board.service;
//
//import com.workingbit.share.domain.impl.BoardBox;
//import com.workingbit.share.model.BoardBoxes;
//import com.workingbit.share.model.DomainId;
//import org.ehcache.Cache;
//import org.ehcache.CacheManager;
//import org.ehcache.config.builders.CacheConfigurationBuilder;
//import org.ehcache.config.builders.CacheManagerBuilder;
//import org.ehcache.config.builders.ResourcePoolsBuilder;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.Map;
//import java.util.Optional;
//
///**
// * Created by Aleksey Popryadukhin on 17/04/2018.
// */
//public class BoardBoxStoreService {
//
//  private Cache<String, Map> store;
//
//  public BoardBoxStoreService() {
//    String board = "board";
//    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
//        .withCache(board,
//            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Map.class, ResourcePoolsBuilder.heap(10)))
//        .build();
//    cacheManager.init();
//
//    store = cacheManager.getCache(board, String.class, Map.class);
//  }
//
//  public Optional<BoardBox> get(String userSession, @NotNull DomainId boardBoxId) {
//    Map map = store.get(boardBoxId.getId());
//    if (map != null) {
//      return Optional.ofNullable((BoardBox) map.get(getKey(userSession, boardBoxId.getId())));
//    }
//    return Optional.empty();
//  }
//
//  public void put(String userSession, @NotNull BoardBox board) {
//    Map map = Map.of(getKey(userSession, board.getId()), board);
//    store.put(board.getId(), map);
//  }
//
//  public void remove(@NotNull BoardBox board) {
//    store.remove(board.getId());
//  }
//
//  private String getKey(String key, String boardId) {
//    return key + ":" + boardId;
//  }
//
//  public Optional<BoardBoxes> getByArticleId(String userSession, @NotNull DomainId articleId) {
//    Map map = store.get(articleId.getId());
//    if (map != null) {
//      return Optional.ofNullable((BoardBoxes) map.get(getKey(userSession, articleId.getId())));
//    }
//    return Optional.empty();
//  }
//
//  public void putByArticleId(String userSession, @NotNull DomainId articleId, @NotNull BoardBoxes boardBoxes) {
//    Map map = Map.of(getKey(userSession, articleId.getId()), boardBoxes);
//    store.put(articleId.getId(), map);
//  }
//
//  public void removeByArticleId(@NotNull DomainId articleId) {
//    store.remove(articleId.getId());
//  }
//}
