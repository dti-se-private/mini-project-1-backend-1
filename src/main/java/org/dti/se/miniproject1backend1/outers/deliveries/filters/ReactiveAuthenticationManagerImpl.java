package org.dti.se.miniproject1backend1.outers.deliveries.filters;

import org.dti.se.miniproject1backend1.inners.models.valueobjects.Session;
import org.dti.se.miniproject1backend1.inners.usecases.JwtUseCase;
import org.dti.se.miniproject1backend1.outers.exceptions.jwt.AccessTokenExpiredException;
import org.dti.se.miniproject1backend1.outers.exceptions.jwt.VerifyFailedException;
import org.dti.se.miniproject1backend1.outers.repositories.ones.AccountRepository;
import org.dti.se.miniproject1backend1.outers.repositories.twos.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
public class ReactiveAuthenticationManagerImpl implements ReactiveAuthenticationManager {
    @Autowired
    private JwtUseCase jwtUseCase;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private AccountRepository accountRepository;

    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono
                .fromCallable(() -> (Session) authentication.getCredentials())
                .map(session -> jwtUseCase.verify(session.getAccessToken()))
                .onErrorResume(e -> Mono.error(new VerifyFailedException(e)))
                .filter(decodedJwt -> OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS).isBefore(
                                OffsetDateTime.ofInstant(
                                        decodedJwt.getExpiresAt().toInstant(),
                                        ZoneId.systemDefault()
                                )
                        )
                )
                .switchIfEmpty(Mono.error(new AccessTokenExpiredException()))
                .map(decodedJwt -> decodedJwt.getClaim("account_id").asString())
                .map(UUID::fromString)
                .flatMap(accountId -> accountRepository.findFirstById(accountId))
                .map(account -> new UsernamePasswordAuthenticationToken(
                        account,
                        authentication.getCredentials(),
                        null
                ));
    }
}
