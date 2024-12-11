package org.dti.se.miniproject1backend1.inners.usecases.accounts;

import org.dti.se.miniproject1backend1.inners.models.entities.Account;
import org.dti.se.miniproject1backend1.outers.configurations.SecurityConfiguration;
import org.dti.se.miniproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.miniproject1backend1.outers.repositories.ones.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class BasicAccountUseCase {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    SecurityConfiguration securityConfiguration;

    public Mono<Account> saveOne(Account account) {
        return Mono
                .fromCallable(() -> account.setPassword(securityConfiguration.encode(account.getPassword())))
                .flatMap(accountToSave -> accountRepository.save(accountToSave));
    }

    public Mono<Account> findOneById(UUID id) {
        return accountRepository
                .findFirstById(id)
                .switchIfEmpty(Mono.error(new AccountNotFoundException()));
    }

    public Mono<Account> findOneByEmail(String email) {
        return accountRepository
                .findFirstByEmail(email)
                .switchIfEmpty(Mono.error(new AccountNotFoundException()));
    }

    public Mono<Account> findOneByEmailAndPassword(String email, String password) {
        return accountRepository
                .findFirstByEmailAndPassword(email, password)
                .switchIfEmpty(Mono.error(new AccountNotFoundException()));
    }

    public Mono<Account> patchOneById(UUID id, Account account) {
        return accountRepository
                .findFirstById(id)
                .switchIfEmpty(Mono.error(new AccountNotFoundException()))
                .map(accountToPatch -> accountToPatch.patchFrom(account).setIsNew(false))
                .map(accountToPatch -> accountToPatch.setPassword(securityConfiguration.encode(accountToPatch.getPassword())))
                .flatMap(accountToPatch -> accountRepository.save(accountToPatch));
    }

    public Mono<Void> deleteOneById(UUID id) {
        return accountRepository
                .findFirstById(id)
                .switchIfEmpty(Mono.error(new AccountNotFoundException()))
                .flatMap(account -> accountRepository.delete(account))
                .then();
    }
}
