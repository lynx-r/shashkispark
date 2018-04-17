package com.workingbit.board.service;


import com.workingbit.share.domain.impl.BoardBox;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Aleksey Popryadukhin on 17/04/2018.
 */
public class StoreServiceTest {

  private StoreService store = new StoreService();

  @Test
  public void test_cache() {
    BoardBox value = new BoardBox();
    store.put("1", value);
    BoardBox boardBox = store.get("1");
    assertEquals(value, boardBox);
  }
}
