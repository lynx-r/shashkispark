package com.workingbit.security.dao;

import com.workingbit.security.config.AppProperties;
import com.workingbit.share.dao.BaseDao;
import com.workingbit.share.model.SecureUser;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
public class SecureUserDao extends BaseDao<SecureUser> {

  public SecureUserDao(AppProperties properties) {
    super(SecureUser.class, properties.regionDynamoDB(), properties.endpointDynamoDB().toString(), properties.test());
  }

  public SecureUser findBySession(String session) {
    return findByAttributeIndex(session, "userSession","userSessionIndex");
  }

  public SecureUser findByUsername(String username) {
    return findByAttributeIndex(username, "username","usernameIndex");
  }
}
