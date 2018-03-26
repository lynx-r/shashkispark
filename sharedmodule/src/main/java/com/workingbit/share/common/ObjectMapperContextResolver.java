//package com.workingbit.share.common;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.workingbit.share.util.Utils;
//
//import javax.ws.rs.ext.ContextResolver;
//import javax.ws.rs.ext.Provider;
//
//@Provider
//public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {
//  private final ObjectMapper mapper;
//
//  public ObjectMapperContextResolver() {
//    mapper = Utils.configureObjectMapper(new ObjectMapper());
//  }
//
//  @Override
//  public ObjectMapper getContext(Class<?> type) {
//    return mapper;
//  }
//}