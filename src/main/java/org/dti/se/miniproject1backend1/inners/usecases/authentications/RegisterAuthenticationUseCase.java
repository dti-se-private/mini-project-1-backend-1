package org.dti.se.miniproject1backend1.inners.usecases.authentications;

import org.dti.se.miniproject1backend1.inners.models.entities.Account;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.authentications.RegisterByEmailAndPasswordRequest;
import org.dti.se.miniproject1backend1.inners.usecases.referrals.BasicReferralUseCase;
import org.dti.se.miniproject1backend1.outers.configurations.SecurityConfiguration;
import org.dti.se.miniproject1backend1.outers.deliveries.holders.WebHolder;
import org.dti.se.miniproject1backend1.outers.exceptions.accounts.AccountExistsException;
import org.dti.se.miniproject1backend1.outers.exceptions.referrals.ReferralCodeNotFoundException;
import org.dti.se.miniproject1backend1.outers.repositories.ones.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransaction;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class RegisterAuthenticationUseCase {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    SecurityConfiguration securityConfiguration;

    @Autowired
    BasicReferralUseCase basicReferralUseCase;

    public Mono<Account> registerByEmailAndPassword(RegisterByEmailAndPasswordRequest request) {
        return accountRepository
                .findFirstByEmail(request.getEmail())
                .flatMap(foundAccount -> Mono.error(new AccountExistsException()))
                .thenReturn(Account
                        .builder()
                        .id(UUID.randomUUID())
                        .name(request.getName())
                        .email(request.getEmail())
                        .password(securityConfiguration.encode(request.getPassword()))
                        .phone(request.getPhone())
                        .dob(request.getDob())
                        .referralCode(UUID.randomUUID().toString())
                        .profileImageUrl(null)
                        .build()
                )
                .flatMap(accountRepository::save)
                .flatMap(registeredAccount -> {
                    if (request.getReferralCode() != null) {
                        return basicReferralUseCase
                                .claimReferralCode(request.getReferralCode(), registeredAccount)
                                .thenReturn(registeredAccount);
                    } else {
                        return Mono.just(registeredAccount);
                    }
                })
                .onErrorResume(ReferralCodeNotFoundException.class, e -> WebHolder
                        .getTransaction()
                        .doOnNext(ReactiveTransaction::setRollbackOnly)
                        .then(accountRepository.deleteByEmail(request.getEmail()))
                        .then(Mono.error(e))
                );
    }

}
