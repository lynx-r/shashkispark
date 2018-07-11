package com.workingbit.board.repo;

import com.workingbit.share.domain.impl.BoardBox;
import com.workingbit.share.model.DomainId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

/**
 * Created by Aleksey Popryadukhin on 11/07/2018.
 */
public interface ReactiveBoardBoxRepository extends ReactiveCrudRepository<BoardBox, DomainId> {
  Flux<BoardBox> findByArticleId_Id(String articleId);
}
