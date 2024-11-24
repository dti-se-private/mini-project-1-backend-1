package org.dti.se.module3session11.inners.usecases;

import org.dti.se.module3session11.inners.models.entities.Account;
import org.dti.se.module3session11.inners.models.valueobjects.authentications.RegisterByEmailAndPasswordRequest;
import org.dti.se.module3session11.inners.models.valueobjects.Session;
import org.dti.se.module3session11.outers.deliveries.filters.ReactiveAuthenticationManagerImpl;
import org.dti.se.module3session11.outers.exceptions.accounts.AccountCredentialsInvalidException;
import org.dti.se.module3session11.outers.exceptions.accounts.AccountExistsException;
import org.dti.se.module3session11.outers.repositories.ones.AccountRepository;
import org.dti.se.module3session11.outers.repositories.twos.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class AuthenticationUseCase {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    JwtUseCase jwtUseCase;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    ReactiveAuthenticationManagerImpl reactiveAuthenticationManagerImpl;

    public Mono<Session> loginByEmailAndPassword(String email, String password) {
        return accountRepository
                .findFirstByEmailAndPassword(email, password)
                .switchIfEmpty(Mono.error(new AccountCredentialsInvalidException()))
                .map(account -> {
                    OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
                    OffsetDateTime accessTokenExpiredAt = now.plusMinutes(5);
                    OffsetDateTime refreshTokenExpiredAt = now.plusDays(3);
                    return Session
                            .builder()
                            .accountId(account.getId())
                            .accessToken(jwtUseCase.generate(account, accessTokenExpiredAt))
                            .refreshToken(jwtUseCase.generate(account, refreshTokenExpiredAt))
                            .accessTokenExpiredAt(accessTokenExpiredAt)
                            .refreshTokenExpiredAt(refreshTokenExpiredAt)
                            .build();
                })
                .flatMap(session -> sessionRepository
                        .setByAccessToken(session)
                        .thenReturn(session)
                );
    }

    public Mono<Account> registerByEmailAndPassword(RegisterByEmailAndPasswordRequest request) {
        return accountRepository
                .findFirstByEmail(request.getEmail())
                .flatMap(account -> Mono.error(new AccountExistsException()))
                .thenReturn(Account
                        .builder()
                        .name(request.getName())
                        .email(request.getEmail())
                        .password(request.getPassword())
                        .phone(request.getPhone())
                        .dob(request.getDob())
                        .referralCode(request.getReferralCode())
                        .build()
                )
                .flatMap(accountRepository::save);
    }

    public Mono<Void> logout(Session session) {
        return reactiveAuthenticationManagerImpl
                .authenticate(new UsernamePasswordAuthenticationToken(null, session))
                .then(sessionRepository.deleteByAccessToken(session.getAccessToken()))
                .then();
    }

}
