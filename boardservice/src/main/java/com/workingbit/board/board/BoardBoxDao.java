package com.workingbit.board.board;

import com.workingbit.board.config.AppProperties;
import com.workingbit.share.dao.BaseDao;
import com.workingbit.share.domain.impl.BoardBox;

/**
 * Created by Aleksey Popryaduhin on 06:55 22/09/2017.
 */
public class BoardBoxDao extends BaseDao<BoardBox> {

  public BoardBoxDao(AppProperties appProperties) {
    super(BoardBox.class, appProperties.regionDynamoDB(), appProperties.endpointDynamoDB().toString(), appProperties.test());
  }
}
