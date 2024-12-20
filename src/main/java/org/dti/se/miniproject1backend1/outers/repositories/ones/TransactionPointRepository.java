package org.dti.se.miniproject1backend1.outers.repositories.ones;

import org.dti.se.miniproject1backend1.inners.models.entities.TransactionPoint;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface TransactionPointRepository extends R2dbcRepository<TransactionPoint, UUID> {
    Flux<TransactionPoint> findByTransactionId(UUID transactionId);
}
