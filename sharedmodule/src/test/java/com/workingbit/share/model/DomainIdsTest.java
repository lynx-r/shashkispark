//package com.workingbit.share.model;
//
//import org.junit.Test;
//
//import java.util.LinkedList;
//
//import static com.workingbit.share.util.JsonUtils.dataToJson;
//import static com.workingbit.share.util.JsonUtils.jsonToData;
//import static java.util.stream.Collectors.collectingAndThen;
//import static java.util.stream.Collectors.toCollection;
//
///**
// * Created by Aleksey Popryadukhin on 06/06/2018.
// */
//public class DomainIdsTest {
//
//  @Test
//  public void name() {
//    var dids = new LinkedList<DomainId>();
//
//    var d = DomainId.getRandomID();
//    dids.add(d);
//
//    d = DomainId.getRandomID();
//    dids.add(d);
//
//    DomainIds collect = dids.stream().collect(collectingAndThen(toCollection(LinkedList::new), DomainIds::new));
//    String json = dataToJson(collect);
//    System.out.println(json);
//    DomainIds domainIds = jsonToData(json, DomainIds.class);
//    System.out.println(domainIds);
//  }
//}