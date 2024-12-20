package org.dti.se.miniproject1backend1.outers.repositories.ones;

import org.dti.se.miniproject1backend1.inners.models.entities.Voucher;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoucherRepository extends R2dbcRepository<Voucher, UUID> {

    Flux<Voucher> findAllByCodeIn(List<String> voucherCodes);
}
