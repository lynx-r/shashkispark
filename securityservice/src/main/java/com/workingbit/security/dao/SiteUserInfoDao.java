package com.workingbit.security.dao;

import com.workingbit.security.config.AppProperties;
import com.workingbit.share.dao.BaseDao;
import com.workingbit.share.model.SiteUserInfo;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
public class SiteUserInfoDao extends BaseDao<SiteUserInfo> {

  public SiteUserInfoDao(AppProperties properties) {
    super(SiteUserInfo.class, properties.regionDynamoDB(), properties.endpointDynamoDB().toString(), properties.test());
  }

  public SiteUserInfo findBySession(String session) {
    return findByAttributeIndex(session, "userSession","userSessionIndex");
  }

  public SiteUserInfo findByUsername(String username) {
    return findByAttributeIndex(username, "username","usernameIndex");
  }
}
