package com.workingbit.article.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.workingbit.article.config.AppProperties;
import com.workingbit.share.dao.BaseDao;
import com.workingbit.share.dao.DaoFilters;
import com.workingbit.share.dao.Unary;
import com.workingbit.share.dao.ValueFilter;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.exception.RequestException;
import com.workingbit.share.model.Articles;
import com.workingbit.share.model.AuthUser;
import com.workingbit.share.model.enumarable.EnumArticleStatus;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Aleksey Popryaduhin on 18:16 09/08/2017.
 */
public class ArticleDao extends BaseDao<Article> {

  public ArticleDao(AppProperties properties) {
    super(Article.class, properties.regionDynamoDB(), properties.endpointDynamoDB().toString(), properties.local());
  }

  public Article findActiveById(String entityKey) {
    if (StringUtils.isBlank(entityKey)) {
      logger.info("Entity key is null");
      throw RequestException.notFound404();
    }
    logger.info("Find by key: " + entityKey);

    Map<String, AttributeValue> eav = new HashMap<>();
    eav.put(":entityKey", new AttributeValue().withS(entityKey));

    DynamoDBQueryExpression<Article> queryExpression = new DynamoDBQueryExpression<Article>()
        .withKeyConditionExpression("id = :entityKey")
        .withExpressionAttributeValues(eav);

    PaginatedQueryList<Article> queryList = getDynamoDBMapper().query(Article.class, queryExpression);
    if (queryList.isEmpty()) {
      throw RequestException.notFound404();
    }
    return queryList.get(0);
  }

  //    @Override
//  public void save(Article entity) {
//    super.save(entity);
//  }

//  public void publishArticle(Article board) {
//    board.setState(EnumArticleState.published);
//    save(board);
//  }

  public Articles findPublishedBy(int limit, @NotNull AuthUser authUser) {
    logger.info("Find all published with limit " + limit);
    DaoFilters filters = authUser.getFilters();
//    if (authUser.getFilters().isEmpty()) {
//      filters.add(new ValueFilter("articleStatus", EnumArticleStatus.PUBLISHED.name(), "=", "S"));
//    }
    addUserFilter(filters, authUser.getUserId().getId());
    List<Article> articles = findByFilter(filters);
//    articles.sort(Comparator.comparing(Article::getArticleStatus));
    return new Articles(articles);
  }

  public Articles findPublished(int limit) {
    logger.info("Find all published with limit " + limit);
    DaoFilters filters = new DaoFilters();
    filters.add(new ValueFilter("articleStatus", EnumArticleStatus.PUBLISHED.name(), "=", "S"));
    return new Articles(findByFilter(filters));
  }

//  public List<Article> findPublished(int limit) {
//    return findPublished(limit, null);
//  }

  public Article findByHru(String articleHru) {
    return findByAttributeIndex(articleHru, "humanReadableUrl", "humanReadableUrlIndex");
  }

  @NotNull
  private DaoFilters addUserFilter(DaoFilters filters, String userId) {
    if (!filters.containsKey("userId.id")) {
      if (!filters.isEmpty()) {
        filters.add(new Unary("and"));
      }
      filters.add(new ValueFilter("userId.id", userId, "=", "S"));
    }
    return filters;
  }
}
