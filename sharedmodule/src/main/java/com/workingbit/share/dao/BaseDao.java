package com.workingbit.share.dao;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.Select;
import com.workingbit.share.domain.BaseDomain;
import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.DomainIds;
import com.workingbit.share.model.SimpleFilter;
import com.workingbit.share.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.workingbit.share.util.Utils.getRandomString;
import static com.workingbit.share.util.Utils.isBlank;
import static java.lang.String.format;

/**
 * Created by Aleksey Popryaduhin on 18:56 09/08/2017.
 */
public class BaseDao<T extends BaseDomain> {

  protected Logger logger;
  private final Class<T> clazz;
  private final DynamoDBMapper dynamoDBMapper;

  protected BaseDao(Class<T> clazz, String region, String endpoint, boolean test) {
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

  protected DynamoDBMapper getDynamoDBMapper() {
    return dynamoDBMapper;
  }

  public void save(final T entity, DynamoDBSaveExpression saveExpression) {
    if (entity == null) {
      logger.error("Entity is null");
      return;
    }
    if (entity.isReadonly()) {
      throw new RuntimeException("Unmodifiable entity " + entity);
    }
    entity.setUpdatedAt(LocalDateTime.now());
    dynamoDBMapper.save(entity, saveExpression);
  }

  public void save(final T entity) {
    if (entity == null) {
      logger.error("Entity is null");
      return;
    }
    if (entity.isReadonly()) {
      throw new RuntimeException("Unmodifiable entity " + entity);
    }
    logger.info("Saving entity " + entity);
    entity.setUpdatedAt(LocalDateTime.now());
    dynamoDBMapper.save(entity);
  }

  public List<T> findAll(Integer limit) {
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

  public Optional<T> findById(String entityKey) {
    if (StringUtils.isBlank(entityKey)) {
      logger.info("Entity key is null");
      return Optional.empty();
    }
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
    findById(entityId)
        .ifPresent(dynamoDBMapper::delete);
  }

  public List<BoardBox> findByIds(DomainIds ids) {
    logger.info(String.format("Find by ids %s", ids));
    Map<Class<?>, List<KeyPair>> itemsToGet = new HashMap<>(ids.getIds().size());
    itemsToGet.put(BoardBox.class, ids.getIds().stream()
        .map(id -> new KeyPair().withHashKey(id.getId()).withRangeKey(id.getCreatedAt()))
        .collect(Collectors.toList()));
    Map<String, List<Object>> batchLoad = getDynamoDBMapper().batchLoad(itemsToGet);
    if (!batchLoad.isEmpty()) {
      return Utils.listObjectsToListT(batchLoad.get(BoardBox.class.getSimpleName()), BoardBox.class);
    }
    return Collections.emptyList();
  }

  public Optional<T> find(T obj) {
    return findById(obj.getId());
  }

  protected Optional<T> findByAttributeIndex(String attribute, String attributeName, String indexName) {
    if (StringUtils.isBlank(attribute)) {
      logger.info(attribute + " is null");
      return Optional.empty();
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
      return Optional.of(queryList.get(0));
    }
    return Optional.empty();
  }

  protected List<T> findByFilter(int limit, List<SimpleFilter> filters, String defaultFilterExpression,
                                 Map<String, AttributeValue> eav,
                                 String[] validFilterKeys, Pattern[] validFilterValues) {
    String filterExpression = createFilterAndPutValues(filters, eav, validFilterKeys, validFilterValues);
    logger.info("Apply filter: " + filterExpression);

    DynamoDBScanExpression dynamoDBQueryExpression = new DynamoDBScanExpression();
    String filter = chooseFilterExpression(filterExpression, defaultFilterExpression);
    dynamoDBQueryExpression
        .withFilterExpression(filter)
        .withExpressionAttributeValues(eav)
        .withSelect(Select.ALL_ATTRIBUTES);
    try {
      DynamoDBIndexHashKey indexAnnotation = clazz.getDeclaredField("id").getAnnotation(DynamoDBIndexHashKey.class);
      if (indexAnnotation != null) {
        String indexName = indexAnnotation.globalSecondaryIndexName();
        dynamoDBQueryExpression
            .withIndexName(indexName);
      }
    } catch (NoSuchFieldException e) {
      logger.info("No DynamoDBIndexHashKey annotation on class " + getClass().getCanonicalName(), e);
    }

    List<T> result = getDynamoDBMapper().scanPage(clazz, dynamoDBQueryExpression)
        .getResults();
    if (limit < result.size()) {
      result = result.subList(0, limit);
    }
    result.sort((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt()));
    return result;
  }

  private String chooseFilterExpression(String filterExpression, String defaultFilterExpression) {
    String userFilter = StringUtils.isNotBlank(filterExpression) ? filterExpression : "";
    if (StringUtils.isNotBlank(userFilter)) {
      defaultFilterExpression = userFilter;
    }
    return defaultFilterExpression;
  }

  @SuppressWarnings("unchecked")
  private String createFilterAndPutValues(List<SimpleFilter> filters, Map<String, AttributeValue> eav,
                                          String[] validFilterKeys, Pattern[] validFilterValues) {
    List<String> filterExpression = new ArrayList<>();
    filters
        .stream()
        .filter((filter) -> validFilter(filter, validFilterKeys, validFilterValues))
        .forEach(filter -> {
          String key = formatSub(filter.getKey());
          if (eav.containsKey(key)) {
            key += getRandomString(3);
          }
          AttributeValue value = new AttributeValue();
          switch (filter.getType()) {
            case "S":
              value.setS((String) filter.getValue());
              break;
            case "SS":
              value.setSS((Collection<String>) filter.getValue());
              break;
          }
          eav.put(key, value);
          filterExpression.add(filter.getKey() + filter.getOperator() + key);
        });
    Optional<String> userId = filterExpression.stream()
        .filter(f -> f.contains("userId"))
        .findFirst();
    if (userId.isPresent()) {
      String orFilters = filterExpression.stream()
          .filter(f -> !f.contains("userId"))
          .collect(Collectors.joining(" or "));
      if (!orFilters.isEmpty()) {
        return "( " + orFilters + " ) and " + userId.get();
      }
      return userId.get();
    }
    return filterExpression.stream().collect(Collectors.joining(" or "));
  }

  private boolean validFilter(SimpleFilter filter, String[] validFilterKeys, Pattern[] validFilterValues) {
    boolean notBlankAndNotNull = StringUtils.isNotBlank(filter.getKey()) && !filter.getKey().contains("null");
    boolean validKey = true;
    if (validFilterKeys != null) {
      validKey = Arrays.stream(validFilterKeys).anyMatch((p) -> p.equals(filter.getKey()));
    }
    return notBlankAndNotNull && validKey;
  }

  private String formatSub(String sub) {
    return ":" + sub.toLowerCase();
  }
}
