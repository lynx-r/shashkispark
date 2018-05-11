package com.workingbit.article.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.workingbit.article.config.AppProperties;
import com.workingbit.share.dao.BaseDao;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.SimpleFilter;
import com.workingbit.share.model.enumarable.EnumArticleStatus;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.workingbit.article.config.AppConstants.VALID_FILTER_KEYS;
import static com.workingbit.article.config.AppConstants.VALID_FILTER_VALUES;

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

    DynamoDBQueryExpression<Article> queryExpression = new DynamoDBQueryExpression<Article>()
        .withKeyConditionExpression("id = :entityKey")
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

  public List<Article> findPublished(int limit, AuthUser authUser, List<SimpleFilter> filters) {
    logger.info("Find all published with limit " + limit);
    Map<String, AttributeValue> eav = new HashMap<>();
    String filter = "";
    if (filters.isEmpty()) {
      eav.put(":published", new AttributeValue().withS(EnumArticleStatus.PUBLISHED.name()));
      filter = "articleStatus = :published and ";
    }
    return findByFilter(limit, filters, filter, eav, VALID_FILTER_KEYS, VALID_FILTER_VALUES);
  }

//  public List<Article> findPublished(int limit) {
//    return findPublished(limit, null);
//  }

  public Optional<Article> findByHru(String articleHru) {
    return findByAttributeIndex(articleHru, "humanReadableUrl", "humanReadableUrlIndex");
  }
}
