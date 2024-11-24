package org.dti.se.module3session11.outers.deliveries.filters;

import org.dti.se.module3session11.outers.repositories.twos.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationWebFilterImpl extends AuthenticationWebFilter {

    @Autowired
    private SessionRepository sessionRepository;

    public AuthenticationWebFilterImpl(ReactiveAuthenticationManager authenticationManager) {
        super(authenticationManager);
        setServerAuthenticationConverter(new ServerAuthenticationConverter() {
            @Override
            public Mono<Authentication> convert(ServerWebExchange exchange) {
                return getAccessToken(exchange)
                        .flatMap(accessToken -> sessionRepository.getByAccessToken(accessToken))
                        .map(session -> new UsernamePasswordAuthenticationToken(null, session))
                        .flatMap(authenticationManager::authenticate);
            }
        });
        setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.anyExchange());
    }

    public Mono<String> getAccessToken(ServerWebExchange exchange) {
        return Mono
                .fromCallable(exchange::getRequest)
                .mapNotNull(serverHttpRequest -> serverHttpRequest.getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(authorizationHeader -> !authorizationHeader.isBlank())
                .filter(authorizationHeader -> authorizationHeader.startsWith("Bearer "))
                .map(authorizationHeader -> authorizationHeader.split(" ")[1]);
    }
}
