package com.workingbit.article;

import com.workingbit.article.config.AppProperties;
import com.workingbit.article.dao.ArticleDao;
import com.workingbit.article.service.ArticleService;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.util.Utils;

import static com.workingbit.share.common.Config4j.configurationProvider;

/**
 * Created by Aleksey Popryadukhin on 06/04/2018.
 */
public class BaseTest {

  private static AppProperties appProperties = configurationProvider("application.yaml").bind("app", AppProperties.class);

  protected ArticleDao articleDao = new ArticleDao(appProperties);

  protected ArticleService articleService = new ArticleService();

  protected Article createArticle() {
    Article article = new Article();
    Utils.setArticleIdAndCreatedAt(article, true);
    article.setAuthor(Utils.getRandomString());
    article.setBoardBoxId(Utils.getRandomString());
    article.setContent(Utils.getRandomString());
    article.setTitle(Utils.getRandomString());
    articleDao.save(article);
    return article;
  }
}
