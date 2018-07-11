package com.workingbit.board.repo;

import com.workingbit.share.domain.impl.Notation;
import com.workingbit.share.model.DomainId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.List;

/**
 * Created by Aleksey Popryadukhin on 11/07/2018.
 */
public interface ReactiveNotationRepository extends ReactiveCrudRepository<Notation, DomainId> {
  List<Notation> findByIdIn(List<DomainId> domainIds);
}
