package com.workingbit.article.dao;

import com.workingbit.article.config.AppProperties;
import com.workingbit.share.dao.BaseDao;
import com.workingbit.share.domain.impl.Article;

/**
 * Created by Aleksey Popryaduhin on 18:16 09/08/2017.
 */
public class ArticleDao extends BaseDao<Article> {

  public ArticleDao(AppProperties properties) {
    super(Article.class, properties.regionDynamoDB(), properties.endpointDynamoDB().toString(), properties.test());
  }

//  @Override
//  public void save(Article entity) {
//    super.save(entity);
//  }

//  public void publishArticle(Article board) {
//    board.setState(EnumArticleState.published);
//    save(board);
//  }

//  public List<Article> findPublished() {
//    DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
//    Map<String, AttributeValue> eav = new HashMap<>();
//    eav.put(":trueVal", new AttributeValue().withN("1"));
//    eav.put(":falseVal", new AttributeValue().withN("0"));
//    String filterExpression = Published.name() + " = :trueVal and "
//        + NewAdded.name() + " = :falseVal and "
//        + Banned.name() + " = :falseVal";
//    scanExpression.withFilterExpression(filterExpression).withExpressionAttributeValues(eav);
//    PaginatedScanList<Article> scanArticle = getDynamoDBMapper().scan(Article.class, scanExpression);
//    scanArticle.loadAllResults();
//    List<Article> articles = new ArrayList<>(scanArticle.size());
//    articles.addAll(scanArticle);
//    return articles;
//  }
}
