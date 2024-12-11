package org.dti.se.miniproject1backend1.outers.repositories.ones;

import org.dti.se.miniproject1backend1.inners.models.entities.Transaction;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface TransactionRepository extends R2dbcRepository<Transaction, Long> {
    Mono<Integer> countByEventId(UUID eventId);

    Mono<Void> deleteAllByEventId(UUID eventId);

    Mono<Void> deleteById(UUID id);

    Flux<Transaction> findByAccountId(UUID accountId, Pageable pageable);

    Mono<Transaction> findById(UUID id);
}
