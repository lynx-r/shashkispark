package com.workingbit.article.dao;

import com.workingbit.article.BaseTest;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.model.SimpleFilter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertTrue;

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

    List<SimpleFilter> filters = new ArrayList<>();
    filters.add(new SimpleFilter("articleStatus", "DRAFT", " = ", "S"));
    List<Article> published = articleDao.findPublished(100, null, filters);
    assertTrue(published.contains(article));
  }
}