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
    if (StringUtils.isBlank(session)) {
      logger.info("Session is null");
      return Optional.empty();
    }
    logger.info("Find by userSession: " + session);

    Map<String, AttributeValue> eav = new HashMap<>();
    eav.put(":userSession", new AttributeValue().withS(session));

    DynamoDBQueryExpression<SecureUser> queryExpression = new DynamoDBQueryExpression<SecureUser>()
        .withIndexName("userSessionIndex")
        .withConsistentRead(false)
        .withKeyConditionExpression("userSession = :userSession")
        .withExpressionAttributeValues(eav);

    PaginatedQueryList<SecureUser> queryList = getDynamoDBMapper().query(SecureUser.class, queryExpression);
    if (!queryList.isEmpty()) {
      return Optional.of(queryList.get(0));
    }
    return Optional.empty();
  }
}
