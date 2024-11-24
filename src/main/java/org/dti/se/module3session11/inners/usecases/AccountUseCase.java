package org.dti.se.module3session11.inners.usecases;

import org.dti.se.module3session11.inners.models.entities.Account;
import org.dti.se.module3session11.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.module3session11.outers.repositories.ones.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class AccountUseCase {
    @Autowired
    private AccountRepository accountRepository;

    public Mono<Account> saveOne(Account account) {
        return accountRepository
                .save(account);
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
                .map(accountToPatch -> accountToPatch.patchFrom(account))
                .map(accountToPatch -> accountToPatch.setIsNew(false))
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
