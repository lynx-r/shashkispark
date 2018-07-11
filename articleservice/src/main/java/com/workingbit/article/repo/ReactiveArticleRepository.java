package com.workingbit.article.repo;

import com.workingbit.share.domain.impl.Article;
import com.workingbit.share.model.DomainId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 * Created by Aleksey Popryadukhin on 11/07/2018.
 */
public interface ReactiveArticleRepository extends ReactiveCrudRepository<Article, DomainId> {
}
