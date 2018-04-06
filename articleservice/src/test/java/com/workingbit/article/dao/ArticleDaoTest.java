package com.workingbit.article.dao;

import com.workingbit.article.BaseTest;
import com.workingbit.share.domain.impl.Article;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aleksey Popryadukhin on 06/04/2018.
 */
public class ArticleDaoTest extends BaseTest {

  @Test
  public void test_find_all_ordered_by_date() throws InterruptedException {
    createArticle();
    TimeUnit.SECONDS.sleep(1);
    createArticle();
    TimeUnit.SECONDS.sleep(1);
    createArticle();

    List<Article> articles = articleDao.findAll(2);
    articles.forEach(System.out::println);
  }
}