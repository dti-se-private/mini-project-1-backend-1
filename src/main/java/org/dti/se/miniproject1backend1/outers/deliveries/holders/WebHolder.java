package org.dti.se.miniproject1backend1.outers.deliveries.holders;

import org.springframework.stereotype.Component;
import org.springframework.transaction.ReactiveTransaction;
import reactor.core.publisher.Mono;

@Component
public class WebHolder {

    public final static String TRANSACTION_CONTEXT_KEY = "transaction";

    public static Mono<ReactiveTransaction> getTransaction() {
        return Mono
                .deferContextual(contextView -> contextView.get(TRANSACTION_CONTEXT_KEY))
                .cast(ReactiveTransaction.class);
    }

}
