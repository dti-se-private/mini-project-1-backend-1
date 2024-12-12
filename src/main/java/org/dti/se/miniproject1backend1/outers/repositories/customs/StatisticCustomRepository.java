package org.dti.se.miniproject1backend1.outers.repositories.customs;

import org.dti.se.miniproject1backend1.inners.models.entities.Account;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.statistics.StatisticSeriesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;
import java.util.Objects;

@Repository
public class StatisticCustomRepository {

    @Autowired
    @Qualifier("oneTemplate")
    private R2dbcEntityTemplate oneTemplate;

    public Flux<StatisticSeriesResponse> retrieveTransactionAmountAverage(Account account, String period) {
        return oneTemplate
                .getDatabaseClient()
                .sql("""
                        SELECT
                        DATE_TRUNC(:period, t.time) as x,
                        AVG(et.price) as y
                        FROM transaction t
                        INNER JOIN event e ON e.id = t.event_id
                        INNER JOIN event_ticket et ON et.event_id = e.id
                        WHERE e.account_id = :accountId::uuid
                        GROUP BY x
                        ORDER BY x
                        """)
                .bind("period", period)
                .bind("accountId", Objects.requireNonNull(account.getId()))
                .map((row, rowMetadata) -> StatisticSeriesResponse
                        .builder()
                        .x(row.get("x", OffsetDateTime.class))
                        .y(row.get("y", Double.class))
                        .build()
                )
                .all();
    }

    public Flux<StatisticSeriesResponse> retrieveTransactionAmountSum(Account account, String period) {
        return oneTemplate
                .getDatabaseClient()
                .sql("""
                        SELECT
                        DATE_TRUNC(:period, t.time) as x,
                        SUM(et.price) as y
                        FROM transaction t
                        INNER JOIN event e ON e.id = t.event_id
                        INNER JOIN event_ticket et ON et.event_id = e.id
                        WHERE e.account_id = :accountId::uuid
                        GROUP BY x
                        ORDER BY x
                        """)
                .bind("period", period)
                .bind("accountId", Objects.requireNonNull(account.getId()))
                .map((row, rowMetadata) -> StatisticSeriesResponse
                        .builder()
                        .x(row.get("x", OffsetDateTime.class))
                        .y(row.get("y", Double.class))
                        .build()
                )
                .all();
    }

    public Flux<StatisticSeriesResponse> retrieveParticipantCountAverage(Account account, String period) {
        return oneTemplate
                .getDatabaseClient()
                .sql("""
                        SELECT
                        DATE_TRUNC(:period, t.time) as x,
                        AVG((select count(*) from transaction t2 where t2.id = t.id)) as y
                        FROM transaction t
                        INNER JOIN event e ON e.id = t.event_id
                        WHERE e.account_id = :accountId::uuid
                        GROUP BY x
                        ORDER BY x;
                        """)
                .bind("period", period)
                .bind("accountId", Objects.requireNonNull(account.getId()))
                .map((row, rowMetadata) -> StatisticSeriesResponse
                        .builder()
                        .x(row.get("x", OffsetDateTime.class))
                        .y(row.get("y", Double.class))
                        .build()
                )
                .all();
    }

    public Flux<StatisticSeriesResponse> retrieveParticipantCountSum(Account account, String period) {
        return oneTemplate
                .getDatabaseClient()
                .sql("""
                        SELECT
                        DATE_TRUNC(:period, t.time) as x,
                        SUM((select count(*) from transaction t2 where t2.id = t.id)) as y
                        FROM transaction t
                        INNER JOIN event e ON e.id = t.event_id
                        WHERE e.account_id = :accountId::uuid
                        GROUP BY x
                        ORDER BY x;
                        """)
                .bind("period", period)
                .bind("accountId", Objects.requireNonNull(account.getId()))
                .map((row, rowMetadata) -> StatisticSeriesResponse
                        .builder()
                        .x(row.get("x", OffsetDateTime.class))
                        .y(row.get("y", Double.class))
                        .build()
                )
                .all();
    }
}
