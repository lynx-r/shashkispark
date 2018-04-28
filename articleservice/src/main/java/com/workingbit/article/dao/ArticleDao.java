package com.workingbit.article.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.Select;
import com.workingbit.article.config.AppProperties;
import com.workingbit.share.dao.BaseDao;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.model.SimpleFilter;
import com.workingbit.share.model.enumarable.EnumArticleStatus;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.workingbit.share.util.Utils.getRandomString;

/**
 * Created by Aleksey Popryaduhin on 18:16 09/08/2017.
 */
public class ArticleDao extends BaseDao<Article> {

  public ArticleDao(AppProperties properties) {
    super(Article.class, properties.regionDynamoDB(), properties.endpointDynamoDB().toString(), properties.test());
  }

  public Optional<Article> findActiveById(String entityKey) {
    if (StringUtils.isBlank(entityKey)) {
      logger.info("Entity key is null");
      return Optional.empty();
    }
    logger.info("Find by key: " + entityKey);

    Map<String, AttributeValue> eav = new HashMap<>();
    eav.put(":entityKey", new AttributeValue().withS(entityKey));
    eav.put(":new_added", new AttributeValue().withS(EnumArticleStatus.NEW_ADDED.name()));
    eav.put(":published", new AttributeValue().withS(EnumArticleStatus.PUBLISHED.name()));

    DynamoDBQueryExpression<Article> queryExpression = new DynamoDBQueryExpression<Article>()
        .withKeyConditionExpression("id = :entityKey")
        .withFilterExpression("articleStatus = :new_added or articleStatus = :published")
        .withExpressionAttributeValues(eav);

    PaginatedQueryList<Article> queryList = getDynamoDBMapper().query(Article.class, queryExpression);
    if (!queryList.isEmpty()) {
      return Optional.of(queryList.get(0));
    }
    return Optional.empty();
  }

  //    @Override
//  public void save(Article entity) {
//    super.save(entity);
//  }

//  public void publishArticle(Article board) {
//    board.setState(EnumArticleState.published);
//    save(board);
//  }

  public List<Article> findPublished(int limit, List<SimpleFilter> filters) {
    logger.info("Find all published with limit " + limit);

    String filterExpression = null;
    Map<String, AttributeValue> eav = new HashMap<>();
    if (filters.isEmpty()) {
      eav.put(":published", new AttributeValue().withS(EnumArticleStatus.PUBLISHED.name()));
    } else {
      filterExpression = createFilterAndPutValues(filters, eav);
      logger.info("Apply filter: " + filterExpression);
    }

    DynamoDBScanExpression dynamoDBQueryExpression = new DynamoDBScanExpression();
    String filter = chooseFilterExpression(filterExpression);
    dynamoDBQueryExpression
        .withFilterExpression(filter)
        .withExpressionAttributeValues(eav)
        .withSelect(Select.ALL_ATTRIBUTES);
    try {
      DynamoDBIndexHashKey indexAnnotation = Article.class.getDeclaredField("id").getAnnotation(DynamoDBIndexHashKey.class);
      if (indexAnnotation != null) {
        String indexName = indexAnnotation.globalSecondaryIndexName();
        dynamoDBQueryExpression
            .withIndexName(indexName);
      }
    } catch (NoSuchFieldException e) {
      logger.info("No DynamoDBIndexHashKey annotation on class " + getClass().getCanonicalName(), e);
    }

    List<Article> result = getDynamoDBMapper().scanPage(Article.class, dynamoDBQueryExpression)
        .getResults();
    if (limit < result.size()) {
      result = result.subList(0, limit);
    }
    result.sort((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt()));
    return result;
  }

  public List<Article> findPublished(int limit) {
    return findPublished(limit, new ArrayList<>());
  }

  private String chooseFilterExpression(String filterExpression) {
    String userFilter = StringUtils.isNotBlank(filterExpression) ? filterExpression : "";
    String filter = "articleStatus = :published";
    if (StringUtils.isNotBlank(userFilter)) {
      filter = userFilter;
    }
    return filter;
  }

  private String createFilterAndPutValues(List<SimpleFilter> filters, Map<String, AttributeValue> eav) {
    List<String> filterExpression = new ArrayList<>();
    filters
        .stream()
        .filter(this::validFilter)
        .forEach(filter -> {
          String key = formatSub(filter.getKey());
          if (eav.containsKey(key)) {
            key += getRandomString(3);
          }
          eav.put(key, new AttributeValue(filter.getValue()));
          filterExpression.add(filter.getKey() + " = " + key);
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

  private boolean validFilter(SimpleFilter filter) {
    return StringUtils.isNotBlank(filter.getKey()) && !filter.getKey().contains("null");
  }

  private String formatSub(String sub) {
    return ":" + sub.toLowerCase();
  }
}
