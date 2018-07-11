//package com.workingbit.share.model;
//
//import com.workingbit.share.domain.impl.BoardBox;
//import org.apache.commons.collections4.map.ListOrderedMap;
//import org.junit.Test;
//
//import java.util.HashMap;
//import java.util.LinkedList;
//
//import static com.workingbit.share.util.JsonUtils.dataToJson;
//import static java.util.stream.Collectors.collectingAndThen;
//import static java.util.stream.Collectors.toCollection;
//
///**
// * Created by Aleksey Popryadukhin on 12/05/2018.
// */
//public class BoardBoxesTest {
//
//  @Test
//  public void insertFirst() {
//    ListOrderedMap<String, BoardBox> boardBoxes = new ListOrderedMap<>();
//
//    BoardBox boardBox = new BoardBox();
//    boardBox.setDomainId(DomainId.getRandomID());
//    boardBoxes.put("1",boardBox);
//    System.out.println(boardBox);
//
//    boardBox = new BoardBox();
//    boardBox.setDomainId(DomainId.getRandomID());
//    boardBoxes.put("2", boardBox);
//    System.out.println(boardBox);
//
//    String json = dataToJson(boardBoxes);
//    System.out.println(json);
//  }
//}