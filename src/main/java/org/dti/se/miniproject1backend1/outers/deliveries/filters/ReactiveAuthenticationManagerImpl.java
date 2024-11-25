package org.dti.se.miniproject1backend1.outers.deliveries.filters;

import org.dti.se.miniproject1backend1.inners.models.valueobjects.Session;
import org.dti.se.miniproject1backend1.inners.usecases.authentications.JwtAuthenticationUseCase;
import org.dti.se.miniproject1backend1.outers.repositories.ones.AccountRepository;
import org.dti.se.miniproject1backend1.outers.repositories.twos.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class ReactiveAuthenticationManagerImpl implements ReactiveAuthenticationManager {
    @Autowired
    private JwtAuthenticationUseCase jwtAuthenticationUseCase;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private AccountRepository accountRepository;

    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono
                .fromCallable(() -> (Session) authentication.getCredentials())
                .map(session -> jwtAuthenticationUseCase.verify(session.getAccessToken()))
                .map(decodedJwt -> decodedJwt.getClaim("account_id").as(UUID.class))
                .flatMap(accountId -> accountRepository.findFirstById(accountId))
                .map(account -> new UsernamePasswordAuthenticationToken(
                        account,
                        authentication.getCredentials(),
                        null
                ));
    }
}
