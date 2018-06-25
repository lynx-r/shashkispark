package com.workingbit.share.dao;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.Select;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.exception.DaoException;
import com.workingbit.share.model.DomainId;
import com.workingbit.share.model.DomainIds;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

/**
 * Created by Aleksey Popryaduhin on 18:56 09/08/2017.
 */
public class BaseDao<T extends BaseDomain> {

  protected Logger logger;
  @NotNull
  private final Class<T> clazz;
  @NotNull
  private final DynamoDBMapper dynamoDBMapper;

  protected BaseDao(@NotNull Class<T> clazz, String region, String endpoint, boolean test) {
    this.clazz = clazz;

    logger = LoggerFactory.getLogger(clazz);

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

  @NotNull
  protected DynamoDBMapper getDynamoDBMapper() {
    return dynamoDBMapper;
  }

  public void save(@Nullable final T entity, DynamoDBSaveExpression saveExpression) {
    if (entity == null) {
      logger.error("Entity is null");
      return;
    }
    if (entity.isReadonly()) {
      throw new RuntimeException("Unmodifiable entity " + entity.getId());
    }
    entity.setUpdatedAt(LocalDateTime.now());
    dynamoDBMapper.save(entity, saveExpression);
  }

  public void save(@Nullable final T entity) {
    if (entity == null) {
      logger.error("Entity is null");
      throw DaoException.notFound();
    }
    if (entity.isReadonly()) {
      throw new RuntimeException("Unmodifiable entity " + entity.getId());
    }
    logger.info("Saving entity " + entity.getId());
    entity.setUpdatedAt(LocalDateTime.now());
    dynamoDBMapper.save(entity);
  }

  public List<T> findAll(@Nullable Integer limit) {
    logger.info(String.format("Find all with limit %s", limit));

    DynamoDBScanExpression dynamoDBQueryExpression = new DynamoDBScanExpression();
    dynamoDBQueryExpression
        .withSelect(Select.ALL_ATTRIBUTES)
        .withLimit(limit);
    try {
      DynamoDBIndexHashKey indexAnnotation = clazz.getDeclaredField("id").getAnnotation(DynamoDBIndexHashKey.class);
      if (indexAnnotation != null) {
        String indexName = indexAnnotation.globalSecondaryIndexName();
        dynamoDBQueryExpression
            .withIndexName(indexName);
      }
    } catch (NoSuchFieldException e) {
      logger.info("No DynamoDBIndexHashKey annotation on class " + clazz.getCanonicalName(), e);
    }

    List<T> result = dynamoDBMapper.scanPage(clazz, dynamoDBQueryExpression)
        .getResults();
    if (result.isEmpty()) {
      throw DaoException.notFound();
    }
    if (limit != null && limit < result.size()) {
      result = result.subList(0, limit);
    }
    result.sort((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt()));
    return result;
  }

//  public Optional<T> findById(String entityId) {
//    logger.info("Find by id: " + entityId);
//    if (isBlank(entityId)) {
//      return Optional.empty();
//    }
//    T entity = dynamoDBMapper.load(clazz, entityId);
//    if (entity != null) {
//      return Optional.of(entity);
//    }
//    return Optional.empty();
//  }

  public T findById(@Nullable DomainId entityKey) {
    if (entityKey == null) {
      logger.info("Entity key is null");
      throw DaoException.notFound();
    }
    logger.info("Find by key: " + entityKey);
    T load = dynamoDBMapper.load(clazz, entityKey.getId(), entityKey.getCreatedAt());
    if (load == null) {
      throw DaoException.notFound();
    }
    return load;
  }

  public void delete(@Nullable final DomainId entityId) {
    if (entityId == null) {
      throw DaoException.notFound();
    }
    T byId = findById(entityId);
    dynamoDBMapper.delete(byId);
  }

  public void batchDelete(@Nullable final Collection<T> entityIds) {
    if (entityIds == null) {
      throw DaoException.notFound();
    }
    dynamoDBMapper.batchDelete(entityIds);
  }

  public void batchDelete(DomainIds boardIdsToRemove) {
    try {
      var byIds = findByIds(boardIdsToRemove);
      dynamoDBMapper.batchDelete(byIds);
    } catch (DaoException e) {
      if (e.getCode() != HTTP_NOT_FOUND) {
        throw e;
      }
    }
  }


  public List<T> findByIds(@NotNull DomainIds ids) {
    logger.info(String.format("Find by ids %s", ids));
    Map<Class<?>, List<KeyPair>> itemsToGet = new HashMap<>(ids.getIds().size());
    List<KeyPair> findByKeys = ids.getIds()
        .stream()
        .map(id -> new KeyPair().withHashKey(id.getId()).withRangeKey(id.getCreatedAt()))
        .collect(Collectors.toList());
    itemsToGet.put(clazz, findByKeys);
    Map<String, List<Object>> batchLoad = getDynamoDBMapper().batchLoad(itemsToGet);
    if (!batchLoad.isEmpty()) {
      String tableName = clazz.getAnnotation(DynamoDBTable.class).tableName();
      return Utils.listObjectsToListT(batchLoad.get(tableName), clazz);
    }
    throw DaoException.notFound();
  }

  public T find(@NotNull T obj) {
    return findById(obj.getDomainId());
  }

  protected T findByAttributeIndex(String attribute, String attributeName, String indexName) {
    if (StringUtils.isBlank(attribute)) {
      logger.info(attribute + " is null");
      throw DaoException.notFound();
    }
    logger.info("Find by " + attributeName + ": " + attribute);

    Map<String, AttributeValue> eav = new HashMap<>();
    eav.put(":attribute", new AttributeValue().withS(attribute));

    DynamoDBQueryExpression<T> queryExpression = new DynamoDBQueryExpression<T>()
        .withIndexName(indexName)
        .withConsistentRead(false)
        .withKeyConditionExpression(attributeName + " = :attribute")
        .withExpressionAttributeValues(eav);

    PaginatedQueryList<T> queryList = getDynamoDBMapper().query(clazz, queryExpression);
    if (!queryList.isEmpty()) {
      return queryList.get(0);
    }
    throw DaoException.notFound();
  }

  protected List<T> findByFilter(@NotNull DaoFilters filters) {
    return findByFilter(0, filters);
  }

  @SuppressWarnings("unchecked")
  protected List<T> findByFilter(int limit, @NotNull DaoFilters filters) {
    Map<String, Object> parsedFilter = filters.build();
    Map<String, AttributeValue> eav = (Map<String, AttributeValue>) parsedFilter.get("eav");
    String filter = (String) parsedFilter.get("expression");

    if (StringUtils.isBlank(filter)) {
      throw DaoException.notFound();
    }

    DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
    scanExpression.withFilterExpression(filter);
    scanExpression.withExpressionAttributeValues(eav);

    logger.info("Apply filter: " + filter);
    List<T> list = new ArrayList<>(dynamoDBMapper.scan(clazz, scanExpression));
    if (list.isEmpty()) {
      throw DaoException.notFound();
    }
    if (limit != 0 && limit < list.size()) {
      list = list.subList(0, limit);
    }
    list.sort((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt()));
    return list;
  }

  public void batchSave(@NotNull Collection<T> entities) {
    if (entities.isEmpty()) {
      logger.info("Nothing to save");
      return;
    }
    entities.forEach(t -> t.setUpdatedAt(LocalDateTime.now()));
    List<DynamoDBMapper.FailedBatch> failedBatches = dynamoDBMapper.batchSave(entities);
    if (!failedBatches.isEmpty()) {
      String messages = failedBatches.stream()
          .map(DynamoDBMapper.FailedBatch::getException)
          .map(Throwable::getMessage)
          .collect(Collectors.joining(","));
      logger.error(messages);
      throw DaoException.unableToSave();
    }
  }
}
