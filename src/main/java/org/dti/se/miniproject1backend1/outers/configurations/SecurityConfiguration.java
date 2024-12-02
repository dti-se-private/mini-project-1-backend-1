package org.dti.se.miniproject1backend1.outers.configurations;

import org.dti.se.miniproject1backend1.outers.deliveries.filters.AuthenticationWebFilterImpl;
import org.dti.se.miniproject1backend1.outers.deliveries.filters.ReactiveAuthenticationManagerImpl;
import org.dti.se.miniproject1backend1.outers.deliveries.filters.TransactionWebFilterImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Objects;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration implements PasswordEncoder {

    @Autowired
    AuthenticationWebFilterImpl authenticationWebFilterImpl;

    @Autowired
    ReactiveAuthenticationManagerImpl reactiveAuthenticationManagerImpl;

    @Autowired
    TransactionWebFilterImpl transactionWebFilterImpl;

    @Autowired
    Environment environment;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity serverHttpSecurity) {
        return serverHttpSecurity.
                cors(cors -> cors.configurationSource(serverWebExchange -> {
                    var corsConfiguration = new CorsConfiguration();
                    corsConfiguration.addAllowedOriginPattern("*");
                    corsConfiguration.addAllowedMethod("*");
                    corsConfiguration.addAllowedHeader("*");
                    return corsConfiguration;
                }))
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
                        .pathMatchers("/events/**").permitAll()
                        .pathMatchers("/webjars/**", "/v3/api-docs/**").permitAll()
                                .anyExchange().authenticated()
                        )
                        .build();
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return Sha512DigestUtils.shaHex(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return Objects.equals(encodedPassword, Sha512DigestUtils.shaHex(rawPassword.toString()));
    }
}
