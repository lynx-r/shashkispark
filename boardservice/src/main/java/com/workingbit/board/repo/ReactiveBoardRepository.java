package com.workingbit.board.repo;

import com.workingbit.share.domain.impl.Board;
import com.workingbit.share.model.DomainId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Created by Aleksey Popryadukhin on 11/07/2018.
 */
public interface ReactiveBoardRepository extends ReactiveCrudRepository<Board, DomainId> {

  Flux<Board> findByBoardBoxIdIn(List<DomainId> domainId);

  Flux<Board> findByIdIn(List<DomainId> boardIdsToRemove);
}
