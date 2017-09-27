//package com.workingbit.share.common;
//
//import com.fasterxml.jackson.core.JsonParser;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.DeserializationContext;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
//import com.workingbit.share.domain.IDraught;
//import com.workingbit.share.domain.impl.Draught;
//
//import java.io.IOException;
//
///**
// * Created by Aleksey Popryaduhin on 10:43 12/08/2017.
// */
//public class DraughtDeserializer extends StdDeserializer<IDraught> {
//
//  public DraughtDeserializer() {
//    super(IDraught.class);
//  }
//
//  @Override
//  public IDraught deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
//    JsonNode node = p.getCodec().readTree(p);
//
//    int v = (Integer) node.get("v").numberValue();
//    int h = (Integer) node.get("h").numberValue();
//    boolean black = node.get("black").booleanValue();
//    boolean queen = node.get("queen").booleanValue();
//    boolean beaten = node.get("beaten").booleanValue();
//    boolean highlight = node.get("highlight").booleanValue();
//
//    return new Draught(v, h, black, queen, beaten, highlighted);
//  }
//}
