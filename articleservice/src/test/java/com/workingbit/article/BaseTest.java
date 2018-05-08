package com.workingbit.article;

import com.workingbit.article.config.AppProperties;
import com.workingbit.article.dao.ArticleDao;
import com.workingbit.article.service.ArticleService;
import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.model.enumarable.EnumArticleStatus;
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
    Utils.setArticleUrlAndIdAndCreatedAt(article, true);
    article.setArticleStatus(EnumArticleStatus.DRAFT);
    article.setAuthor(Utils.getRandomString20());
    article.setBoardBoxId(Utils.getRandomString20());
    article.setContent(Utils.getRandomString20());
    article.setTitle(Utils.getRandomString20());
    return article;
  }
}
