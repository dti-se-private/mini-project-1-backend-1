package org.dti.se.module3session11.outers.repositories.ones;

import org.dti.se.module3session11.inners.models.entities.Account;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface AccountRepository extends R2dbcRepository<Account, UUID> {
    Mono<Account> findFirstById(UUID id);

    Mono<Account> findFirstByEmail(String email);

    Mono<Account> findFirstByEmailAndPassword(String email, String password);
}