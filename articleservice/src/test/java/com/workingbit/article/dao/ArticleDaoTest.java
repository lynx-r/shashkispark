package com.workingbit.article.dao;

import com.workingbit.article.BaseTest;
import com.workingbit.share.dao.DaoFilters;
import com.workingbit.share.dao.ValueFilter;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.model.Articles;
import com.workingbit.share.model.AuthUser;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * Created by Aleksey Popryadukhin on 06/04/2018.
 */
public class ArticleDaoTest extends BaseTest {

  @Test
  public void test_find_all_ordered_by_date() throws InterruptedException {
    Article article = createArticle();
    articleDao.save(article);
    TimeUnit.SECONDS.sleep(1);
    article = createArticle();
    articleDao.save(article);
    TimeUnit.SECONDS.sleep(1);
    article = createArticle();
    articleDao.save(article);

    List<Article> articles = articleDao.findAll(2);
    articles.forEach(System.out::println);
  }

  @Test
  public void findPublished() {
    Article article = createArticle();
    articleDao.save(article);

    AuthUser authUser = new AuthUser();
    DaoFilters filters = new DaoFilters();
    filters.add(new ValueFilter("articleStatus", "DRAFT", "=", "S"));
    authUser.setFilters(filters);
    Articles published = articleDao.findPublishedBy(100, authUser);
    assertTrue(published.contains(article));
  }
}