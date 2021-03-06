package com.workingbit.security.dao;

import com.workingbit.security.config.AppProperties;
import com.workingbit.share.dao.BaseDao;
import com.workingbit.share.model.SiteUserInfo;

/**
 * Created by Aleksey Popryadukhin on 16/04/2018.
 */
public class SiteUserInfoDao extends BaseDao<SiteUserInfo> {

  public SiteUserInfoDao(AppProperties properties) {
    super(SiteUserInfo.class, properties.regionDynamoDB(), properties.endpointDynamoDB().toString(), properties.local());
  }

  public SiteUserInfo findByEmail(String username) {
    return findByAttributeIndex(username, "email", "emailIndex");
  }
}
