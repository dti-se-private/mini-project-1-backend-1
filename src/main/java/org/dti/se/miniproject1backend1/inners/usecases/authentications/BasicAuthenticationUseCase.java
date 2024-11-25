package org.dti.se.miniproject1backend1.inners.usecases.authentications;

import org.dti.se.miniproject1backend1.inners.models.valueobjects.Session;
import org.dti.se.miniproject1backend1.outers.deliveries.filters.ReactiveAuthenticationManagerImpl;
import org.dti.se.miniproject1backend1.outers.repositories.ones.AccountRepository;
import org.dti.se.miniproject1backend1.outers.repositories.twos.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class BasicAuthenticationUseCase {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    JwtAuthenticationUseCase jwtAuthenticationUseCase;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    ReactiveAuthenticationManagerImpl reactiveAuthenticationManagerImpl;

    public Mono<Void> logout(Session session) {
        return reactiveAuthenticationManagerImpl
                .authenticate(new UsernamePasswordAuthenticationToken(null, session))
                .then(sessionRepository.deleteByAccessToken(session.getAccessToken()))
                .then();
    }

    public Mono<Session> refreshSession(Session session) {
        return Mono
                .fromCallable(() -> jwtAuthenticationUseCase.verify(session.getRefreshToken()))
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
