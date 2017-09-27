package com.workingbit.share.dao;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

import static com.workingbit.share.common.Utils.isBlank;

/**
 * Created by Aleksey Popryaduhin on 18:56 09/08/2017.
 */
public class BaseDao<T> {

  private final Class<T> clazz;
  private final DynamoDBMapper dynamoDBMapper;
  private final ObjectMapper mapper;
  private final boolean test;
  private String dbDir = "~/dbDir";

  protected BaseDao(Class<T> clazz, String region, String endpoint, boolean test) {
    this.clazz = clazz;

    AmazonDynamoDB ddb;
    if (test) {
      ddb = AmazonDynamoDBClientBuilder.standard()
          .withEndpointConfiguration(
              new AwsClientBuilder.EndpointConfiguration(endpoint, region))
          .build();
    } else {
      ddb = AmazonDynamoDBClientBuilder
          .standard()
          .withRegion(region)
          .build();
    }
    this.test = test;
    dynamoDBMapper = new DynamoDBMapper(ddb);
    this.mapper = new ObjectMapper();
  }

  protected DynamoDBMapper getDynamoDBMapper() {
    return dynamoDBMapper;
  }

  public void save(final T entity, DynamoDBSaveExpression saveExpression) {
    dynamoDBMapper.save(entity, saveExpression);
  }

  public void save(final T entity) {
    dynamoDBMapper.save(entity);
  }



  public void batchSave(final Object... entities) {
    dynamoDBMapper.batchSave(entities);
  }

  public void batchSave(final Iterable<T> entities) {
    dynamoDBMapper.batchSave(entities);
  }

  public List<T> findAll(Integer limit) {
//    try {
//      T hashKObject = clazz.newInstance();
//      Method setId = hashKObject.getClass().getMethod("setId", String.class);
//      setId.invoke(hashKObject, "");
//      DynamoDBQueryExpression<T> dynamoDBQueryExpression = new DynamoDBQueryExpression<T>()
//          .withHashKeyValues(hashKObject)
//          .withLimit(limit)
//          .withScanIndexForward(true);
//      return dynamoDBMapper.queryPage(clazz, dynamoDBQueryExpression).getResults();
//    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
//      e.printStackTrace();
//    }
    DynamoDBScanExpression dynamoDBQueryExpression = new DynamoDBScanExpression();
    List<T> result = dynamoDBMapper.scanPage(clazz, dynamoDBQueryExpression)
        .getResults();
    if (limit != null && limit < result.size()) {
      result = result.subList(0, limit);
    }
    Collections.reverse(result);
    return result;
  }

  public Optional<T> findById(String entityId) {
    if (isBlank(entityId)) {
      return Optional.empty();
    }
    T entity = dynamoDBMapper.load(clazz, entityId);
    if (entity != null) {
      return Optional.of(entity);
    }
    return Optional.empty();
  }

  public Optional<T> findByKey(String entityKey) {
    Map<String, AttributeValue> eav = new HashMap<>();
    eav.put(":entityKey", new AttributeValue().withS(entityKey));

    DynamoDBQueryExpression<T> queryExpression = new DynamoDBQueryExpression<T>()
        .withKeyConditionExpression("id = :entityKey")
        .withExpressionAttributeValues(eav);

    PaginatedQueryList<T> queryList = dynamoDBMapper.query(clazz, queryExpression);
    if (!queryList.isEmpty()) {
      return Optional.of(queryList.get(0));
    }
    return Optional.empty();
  }

  public void delete(final String entityId) {
    if (isBlank(entityId)) {
      return;
    }
    findByKey(entityId)
        .ifPresent(dynamoDBMapper::delete);
  }

  public List<T> findByIds(List<String> ids) {
    Map<Class<?>, List<KeyPair>> itemsToGet = new HashMap<>(ids.size());
    itemsToGet.put(clazz, ids.stream()
        .map(id -> new KeyPair().withHashKey(id))
        .collect(Collectors.toList()));
    Map<String, List<Object>> batchLoad = dynamoDBMapper.batchLoad(itemsToGet);
    if (!batchLoad.isEmpty()) {
      return (List<T>) batchLoad.get(clazz.getSimpleName());
    }
    return Collections.emptyList();
  }
}
