package org.dti.se.miniproject1backend1.outers.repositories.ones;

import org.dti.se.miniproject1backend1.inners.models.entities.AccountVoucher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface AccountVoucherRepository extends R2dbcRepository<AccountVoucher, UUID> {
    Flux<AccountVoucher> findByAccountId(UUID accountId, Pageable pageable);
}
