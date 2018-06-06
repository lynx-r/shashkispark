package com.workingbit.share.model;

import com.workingbit.share.domain.impl.BoardBox;
import org.junit.Test;

/**
 * Created by Aleksey Popryadukhin on 12/05/2018.
 */
public class BoardBoxesTest {

  @Test
  public void insertFirst() {
    BoardBoxes boardBoxes = new BoardBoxes();

    BoardBox boardBox = new BoardBox();
    boardBox.setDomainId(DomainId.getRandomID());
    boardBoxes.push(boardBox);
    System.out.println(boardBox);

    boardBox = new BoardBox();
    boardBox.setDomainId(DomainId.getRandomID());
    boardBoxes.push(boardBox);
    System.out.println(boardBox);

    boardBox = new BoardBox();
    boardBox.setDomainId(DomainId.getRandomID());
    boardBoxes.push(boardBox);
    System.out.println(boardBox);

    boardBox = new BoardBox();
    boardBox.setDomainId(DomainId.getRandomID());
    boardBoxes.push(boardBox);
    System.out.println(boardBox);

    System.out.println();
    boardBoxes.values().forEach(System.out::println);
    boardBoxes.keySet().forEach(System.out::println);
  }
}