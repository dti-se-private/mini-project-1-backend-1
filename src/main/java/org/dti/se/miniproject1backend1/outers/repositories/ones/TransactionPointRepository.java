package org.dti.se.miniproject1backend1.outers.repositories.ones;

import org.dti.se.miniproject1backend1.inners.models.entities.TransactionPoint;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface TransactionPointRepository extends R2dbcRepository<TransactionPoint, Long> {
    Flux<TransactionPoint> findByTransactionId(UUID transactionId);
}
