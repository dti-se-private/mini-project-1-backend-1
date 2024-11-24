package org.dti.se.module3session11.outers.repositories.ones;

import org.dti.se.module3session11.inners.models.entities.Transaction;
import org.dti.se.module3session11.inners.models.valueobjects.TransactionCountRequest;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TransactionRepository extends ReactiveCrudRepository<Transaction, Long> {

    @Query("SELECT transaction.event_id, " +
            "COUNT(*) AS transaction_count, " +
            "MAX(event.time) AS latest_event_time " +
            "FROM transaction " +
            "JOIN event ON transaction.event_id = event.id " +
            "WHERE event.time > CURRENT_DATE " +
            "GROUP BY transaction.event_id " +
            "ORDER BY transaction_count DESC, latest_event_time DESC " +
            "LIMIT 3")
    Flux<TransactionCountRequest> findTop3EventByTransactions();
}
