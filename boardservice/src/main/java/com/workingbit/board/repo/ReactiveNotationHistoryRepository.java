package com.workingbit.board.repo;

import com.workingbit.share.domain.impl.NotationHistory;
import com.workingbit.share.model.DomainId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Created by Aleksey Popryadukhin on 11/07/2018.
 */
public interface ReactiveNotationHistoryRepository extends ReactiveCrudRepository<NotationHistory, DomainId> {
  Flux<NotationHistory> findByNotationId(DomainId domainId);

  Flux<NotationHistory> findByNotationIdIn(List<DomainId> notationIds);
}
