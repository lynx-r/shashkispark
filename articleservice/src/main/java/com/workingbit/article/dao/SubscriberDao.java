package com.workingbit.article.dao;

import com.workingbit.article.config.AppProperties;
import com.workingbit.share.dao.BaseDao;
import com.workingbit.share.dao.DaoFilters;
import com.workingbit.share.dao.ValueFilter;
import com.workingbit.share.domain.impl.Subscriber;

import java.util.List;

/**
 * Created by Aleksey Popryaduhin on 18:16 09/08/2017.
 */
public class SubscriberDao extends BaseDao<Subscriber> {

  public SubscriberDao(AppProperties properties) {
    super(Subscriber.class, properties.regionDynamoDB(), properties.endpointDynamoDB().toString(), properties.local());
  }

  public Subscriber findByEmail(String email) {
    return findByAttributeIndex(email, "email", "emailIndex");
  }

  public List<Subscriber> findActive() {
    logger.info("Find all active subscribers");
    DaoFilters filters = new DaoFilters();
    filters.add(new ValueFilter("subscribed", true, "=", "BOOL"));
    return findByFilter(filters);
  }
}
