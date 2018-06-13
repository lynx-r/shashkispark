package com.workingbit.share.model;

import com.workingbit.share.model.enumarable.EnumNotationFormat;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Aleksey Popryadukhin on 13/06/2018.
 */
public class NotationMoveTest {

  @Test
  public void moves() {
    NotationMove gh4 = NotationMove.fromPdn("gh4", EnumNotationFormat.SHORT);
    System.out.println(gh4);
    gh4 = NotationMove.fromPdn("g:h4", EnumNotationFormat.SHORT);
    System.out.println(gh4);
  }
}