package com.workingbit.share.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.workingbit.share.common.AppProperties;
import com.workingbit.share.model.SecureUser;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.workingbit.share.common.Config4j.configurationProvider;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
public class SecureUserDao extends BaseDao<SecureUser> {
  private static AppProperties properties;

  static {
    properties = configurationProvider().bind("app", AppProperties.class);
  }

  private SecureUserDao() {
    super(SecureUser.class, properties.regionDynamoDB(), properties.endpointDynamoDB().toString(), properties.test());
  }

  public static SecureUserDao getInstance() {
    return new SecureUserDao();
  }

  public Optional<SecureUser> findBySession(String session) {
    return findByAttributeIndex(session, "userSession","userSessionIndex");
  }

  public Optional<SecureUser> findByUsername(String username) {
    return findByAttributeIndex(username, "username","usernameIndex");
  }

  private Optional<SecureUser> findByAttributeIndex(String attribute, String attributeName, String indexName) {
    if (StringUtils.isBlank(attribute)) {
      logger.info(attribute + " is null");
      return Optional.empty();
    }
    logger.info("Find by " + attributeName + ": " + attribute);

    Map<String, AttributeValue> eav = new HashMap<>();
    eav.put(":attribute", new AttributeValue().withS(attribute));

    DynamoDBQueryExpression<SecureUser> queryExpression = new DynamoDBQueryExpression<SecureUser>()
        .withIndexName(indexName)
        .withConsistentRead(false)
        .withKeyConditionExpression(attributeName + " = :attribute")
        .withExpressionAttributeValues(eav);

    PaginatedQueryList<SecureUser> queryList = getDynamoDBMapper().query(SecureUser.class, queryExpression);
    if (!queryList.isEmpty()) {
      return Optional.of(queryList.get(0));
    }
    return Optional.empty();
  }
}
