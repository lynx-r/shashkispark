package com.workingbit.article.dao;

import com.workingbit.article.config.AppProperties;
import com.workingbit.share.dao.BaseDao;
import com.workingbit.share.domain.impl.Subscriber;

/**
 * Created by Aleksey Popryaduhin on 18:16 09/08/2017.
 */
public class SubscriberDao extends BaseDao<Subscriber> {

  public SubscriberDao(AppProperties properties) {
    super(Subscriber.class, properties.regionDynamoDB(), properties.endpointDynamoDB().toString(), properties.local());
  }

}
