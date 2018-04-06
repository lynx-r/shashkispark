package com.workingbit.article;

import com.workingbit.article.config.AppProperties;
import com.workingbit.article.dao.ArticleDao;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.util.Utils;

import static com.workingbit.share.common.Config4j.configurationProvider;

/**
 * Created by Aleksey Popryadukhin on 06/04/2018.
 */
public class BaseTest {

  private static AppProperties appProperties = configurationProvider().bind("app", AppProperties.class);

  protected ArticleDao articleDao = new ArticleDao(appProperties);

  protected Article createArticle() {
    Article article = new Article();
    Utils.setArticleIdAndCreatedAt(article, true);
    article.setAuthor(Utils.getRandomString());
    article.setBoardBoxId(Utils.getRandomString());
    article.setContent(Utils.getRandomString());
    articleDao.save(article);
    return article;
  }
}
