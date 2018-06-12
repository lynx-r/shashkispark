package com.workingbit.board.dao;

import com.workingbit.board.config.AppProperties;
import com.workingbit.share.dao.BaseDao;
import com.workingbit.share.dao.DaoFilters;
import com.workingbit.share.dao.Unary;
import com.workingbit.share.dao.ValueFilter;
import com.workingbit.share.domain.impl.NotationHistory;
import com.workingbit.share.model.DomainId;
import com.workingbit.share.model.DomainIds;

import java.util.List;

/**
 * Created by Aleksey Popryaduhin on 18:16 09/08/2017.
 */
public class NotationHistoryDao extends BaseDao<NotationHistory> {

  public NotationHistoryDao(AppProperties properties) {
    super(NotationHistory.class, properties.regionDynamoDB(), properties.endpointDynamoDB().toString(), properties.local());
  }

  public List<NotationHistory> findByNotationId(DomainId notationId) {
    DaoFilters filterPublic = new DaoFilters();
    filterPublic.add(new ValueFilter("notationId.id", notationId.getId(), "=", "S"));
    return findByFilter(filterPublic);
  }

  public List<NotationHistory> findByNotationIds(DomainIds notationIds) {
    DaoFilters filterPublic = new DaoFilters();
    for (DomainId notationId : notationIds.getIds()) {
      filterPublic.add(new ValueFilter("notationId.id", notationId.getId(), "=", "S"));
      filterPublic.add(new Unary("or"));
    }
    filterPublic.removeLast();
    return findByFilter(filterPublic);
  }

//  @Override
//  public void saveAndFillBoard(Article entity) {
//    super.saveAndFillBoard(entity);
//  }

//  public void publishArticle(Article board) {
//    board.setState(EnumArticleState.published);
//    saveAndFillBoard(board);
//  }

//  public List<Article> findPublished() {
//    DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
//    Map<String, AttributeValue> eav = new HashMap<>();
//    eav.push(":trueVal", new AttributeValue().withN("1"));
//    eav.push(":falseVal", new AttributeValue().withN("0"));
//    String filters = Published.name() + " = :trueVal and "
//        + NewAdded.name() + " = :falseVal and "
//        + Banned.name() + " = :falseVal";
//    scanExpression.withFilterExpression(filters).withExpressionAttributeValues(eav);
//    PaginatedScanList<Article> scanArticle = getDynamoDBMapper().scan(Article.class, scanExpression);
//    scanArticle.loadAllResults();
//    List<Article> articles = new ArrayList<>(scanArticle.size());
//    articles.addAllEverywhere(scanArticle);
//    return articles;
//  }
}
