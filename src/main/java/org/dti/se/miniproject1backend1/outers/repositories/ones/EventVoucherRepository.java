package org.dti.se.miniproject1backend1.outers.repositories.ones;

import org.dti.se.miniproject1backend1.inners.models.entities.EventVoucher;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface EventVoucherRepository extends R2dbcRepository<EventVoucher, UUID> {
    Flux<EventVoucher> findByEventId(UUID eventId);

    Flux<EventVoucher> findAllByEventId(UUID id);

    Mono<Void> deleteAllByEventId(UUID eventId);
}
