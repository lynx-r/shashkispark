package com.workingbit.board.dao;

import com.workingbit.board.config.AppProperties;
import com.workingbit.share.dao.BaseDao;
import com.workingbit.share.domain.impl.Notation;

/**
 * Created by Aleksey Popryaduhin on 18:16 09/08/2017.
 */
public class NotationDao extends BaseDao<Notation> {

  public NotationDao(AppProperties properties) {
    super(Notation.class, properties.regionDynamoDB(), properties.endpointDynamoDB().toString(), properties.local());
  }
}
