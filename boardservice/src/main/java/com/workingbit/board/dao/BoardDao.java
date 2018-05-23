package com.workingbit.board.dao;

import com.workingbit.board.config.AppProperties;
import com.workingbit.share.dao.BaseDao;
import com.workingbit.share.dao.DaoFilters;
import com.workingbit.share.dao.Unary;
import com.workingbit.share.dao.ValueFilter;
import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.model.DomainId;
import com.workingbit.share.model.DomainIds;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.workingbit.board.BoardEmbedded.boardDao;

/**
 * Created by Aleksey Popryaduhin on 18:16 09/08/2017.
 */
public class BoardDao extends BaseDao<Board> {

  public BoardDao(AppProperties properties) {
    super(Board.class, properties.regionDynamoDB(), properties.endpointDynamoDB().toString(), properties.test());
  }

  public List<Board> findByBoardBoxId(@NotNull DomainId boardBoxDomainId) {
    DaoFilters filterPublic = new DaoFilters();
    filterPublic.add(new ValueFilter("boardBoxId.id", boardBoxDomainId.getId(), "=", "S"));
    return findByFilter(filterPublic);
  }

  public List<Board> findByBoardBoxIds(@NotNull DomainIds boardBoxIds) {
    DaoFilters filterPublic = new DaoFilters();
    for (DomainId boardBoxId : boardBoxIds.getIds()) {
      filterPublic.add(new ValueFilter("boardBoxId.id", boardBoxId.getId(), "=", "S"));
      filterPublic.add(new Unary("or"));
    }
    filterPublic.removeLast();
    return findByFilter(filterPublic);
  }

  public void deleteByBoardBoxId(@NotNull DomainId boardBoxDomainId) {
    List<Board> byBoardBoxId = boardDao.findByBoardBoxId(boardBoxDomainId);
    boardDao.batchDelete(byBoardBoxId);
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
