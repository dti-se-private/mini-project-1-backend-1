package org.dti.se.miniproject1backend1.outers.deliveries.filters;

import org.dti.se.miniproject1backend1.outers.deliveries.holders.WebHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class TransactionWebFilterImpl implements WebFilter {

    @Autowired
    @Qualifier("oneTransactionalOperator")
    private TransactionalOperator transactionalOperator;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return transactionalOperator
                .execute(action -> chain
                        .filter(exchange)
                        .contextWrite(context -> context.put(WebHolder.TRANSACTION_CONTEXT_KEY, action))
                )
                .retryWhen(Retry.backoff(10, Duration.ofMillis(100)))
                .then();
    }
}
