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
//  public Optional<BoardBox> get(String userSession, @NotNull DomainId boardBox) {
//    Map map = store.get(boardBox.getId());
//    if (map != null) {
//      return Optional.ofNullable((BoardBox) map.get(getKey(userSession, boardBox.getId())));
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
//  private String getKey(String key, String board) {
//    return key + ":" + board;
//  }
//
//  public Optional<BoardBoxes> getByArticleId(String userSession, @NotNull DomainId article) {
//    Map map = store.get(article.getId());
//    if (map != null) {
//      return Optional.ofNullable((BoardBoxes) map.get(getKey(userSession, article.getId())));
//    }
//    return Optional.empty();
//  }
//
//  public void putByArticleId(String userSession, @NotNull DomainId article, @NotNull BoardBoxes boardBoxes) {
//    Map map = Map.of(getKey(userSession, article.getId()), boardBoxes);
//    store.put(article.getId(), map);
//  }
//
//  public void removeByArticleId(@NotNull DomainId article) {
//    store.remove(article.getId());
//  }
//}
