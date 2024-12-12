package org.dti.se.miniproject1backend1.outers.repositories.ones;

import org.dti.se.miniproject1backend1.inners.models.entities.TransactionVoucher;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface TransactionVoucherRepository extends R2dbcRepository<TransactionVoucher, UUID> {
    Flux<TransactionVoucher> findByTransactionId(UUID transactionId);
}
