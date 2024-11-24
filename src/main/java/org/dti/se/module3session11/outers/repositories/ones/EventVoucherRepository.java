package org.dti.se.module3session11.outers.repositories.ones;

import org.dti.se.module3session11.inners.models.entities.EventVoucher;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface EventVoucherRepository extends R2dbcRepository<EventVoucher, UUID> {
    Flux<EventVoucher> findByEventId(UUID eventId);
    Flux<EventVoucher> findByVoucherId(UUID voucherId);
}
