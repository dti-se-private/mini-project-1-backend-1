package org.dti.se.miniproject1backend1.outers.configurations;

import org.dti.se.miniproject1backend1.outers.deliveries.filters.AuthenticationWebFilterImpl;
import org.dti.se.miniproject1backend1.outers.deliveries.filters.ReactiveAuthenticationManagerImpl;
import org.dti.se.miniproject1backend1.outers.deliveries.filters.TransactionWebFilterImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration {

    @Autowired
    AuthenticationWebFilterImpl authenticationWebFilterImpl;

    @Autowired
    ReactiveAuthenticationManagerImpl reactiveAuthenticationManagerImpl;

    @Autowired
    TransactionWebFilterImpl transactionWebFilterImpl;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity serverHttpSecurity) {
        return serverHttpSecurity.
                cors(ServerHttpSecurity.CorsSpec::disable)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .authenticationManager(reactiveAuthenticationManagerImpl)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .addFilterAt(transactionWebFilterImpl, SecurityWebFiltersOrder.FIRST)
                .addFilterAt(authenticationWebFilterImpl, SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(authorizeExchange -> authorizeExchange
                        .pathMatchers("/authentications/**").permitAll()
                        .pathMatchers("/authorizations/**").permitAll()
                        .pathMatchers("/events/**").permitAll()
                        .pathMatchers("/webjars/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .anyExchange().authenticated()
                )
                .build();
    }
}
