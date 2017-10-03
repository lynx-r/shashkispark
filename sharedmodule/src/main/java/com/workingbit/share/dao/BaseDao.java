package com.workingbit.share.dao;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static com.workingbit.share.util.Utils.isBlank;
import static java.lang.String.format;

/**
 * Created by Aleksey Popryaduhin on 18:56 09/08/2017.
 */
public class BaseDao<T> {

  private final Class<T> clazz;
  private Logger logger;
  private final DynamoDBMapper dynamoDBMapper;

  protected BaseDao(Class<T> clazz, String region, String endpoint, boolean test) {
    this.clazz = clazz;

    logger = Logger.getLogger(clazz);

    AmazonDynamoDB ddb;
    if (test) {
      logger.info(format("Use test db with region %s and endpoint %s", region, endpoint));
      ddb = AmazonDynamoDBClientBuilder.standard()
          .withEndpointConfiguration(
              new AwsClientBuilder.EndpointConfiguration(endpoint, region))
          .build();
    } else {
      logger.info("Use production db");
      ddb = AmazonDynamoDBClientBuilder
          .standard()
          .withRegion(region)
          .build();
    }
    dynamoDBMapper = new DynamoDBMapper(ddb);
  }

  protected DynamoDBMapper getDynamoDBMapper() {
    return dynamoDBMapper;
  }

  public void save(final T entity, DynamoDBSaveExpression saveExpression) {
    dynamoDBMapper.save(entity, saveExpression);
  }

  public void save(final T entity) {
    logger.info("Saving entity " + entity);
    dynamoDBMapper.save(entity);
  }

  public void batchSave(final Object... entities) {
    dynamoDBMapper.batchSave(entities);
  }

  public void batchSave(final Iterable<T> entities) {
    dynamoDBMapper.batchSave(entities);
  }

  public List<T> findAll(Integer limit) {
    logger.info(String.format("Find all with limit %s", limit));

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
    logger.info("Find by id: " + entityId);
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
    logger.info("Find by key: " + entityKey);

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
    logger.info(String.format("Find by ids %s", ids));
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
