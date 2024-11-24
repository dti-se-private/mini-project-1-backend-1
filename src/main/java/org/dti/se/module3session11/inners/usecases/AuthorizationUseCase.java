package org.dti.se.module3session11.inners.usecases;

import org.dti.se.module3session11.inners.models.valueobjects.Session;
import org.dti.se.module3session11.outers.exceptions.jwt.VerifyFailedException;
import org.dti.se.module3session11.outers.repositories.ones.AccountRepository;
import org.dti.se.module3session11.outers.repositories.twos.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthorizationUseCase {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    JwtUseCase jwtUseCase;

    @Autowired
    SessionRepository sessionRepository;

    public Mono<Session> refreshSession(Session session) {
        return Mono
                .fromCallable(() -> jwtUseCase.verify(session.getRefreshToken()))
                .onErrorResume(e -> Mono.error(new VerifyFailedException(e)))
                .map(decodedJwt -> decodedJwt.getClaim("account_id").as(UUID.class))
                .flatMap(accountId -> Mono
                        .zip(
                                accountRepository.findFirstById(accountId),
                                sessionRepository.getByAccessToken(session.getAccessToken())
                        )
                )
                .map(tuple -> Session
                        .builder()
                        .accountId(tuple.getT1().getId())
                        .accessToken(tuple.getT2().getAccessToken())
                        .refreshToken(tuple.getT2().getRefreshToken())
                        .accessTokenExpiredAt(OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS).plusMinutes(5))
                        .refreshTokenExpiredAt(OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS).plusDays(3))
                        .build()
                )
                .flatMap(newSession -> sessionRepository
                        .setByAccessToken(newSession)
                        .thenReturn(newSession)
                );
    }
}
