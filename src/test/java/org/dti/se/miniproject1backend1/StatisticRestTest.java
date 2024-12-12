package org.dti.se.miniproject1backend1;

import org.dti.se.miniproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.statistics.StatisticSeriesResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

public class StatisticRestTest extends TestConfiguration {
    @BeforeEach
    public void beforeEach() {
        configure();
        populate();
        auth();
    }

    @AfterEach
    public void afterEach() {
        deauth();
        depopulate();
    }

    @Test
    public void testEventTransactionAmountStatistic() {
        webTestClient
                .get()
                .uri(
                        "/statistics/events?type={type}&aggregation={aggregation}&period={period}",
                        "transactionAmount", "sum", "day"
                )
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<List<StatisticSeriesResponse>>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getMessage() != null;
                    assert body.getData() != null;
                });
    }

    @Test
    public void testEventParticipantCountStatistic() {
        webTestClient
                .get()
                .uri(
                        "/statistics/events?type={type}&aggregation={aggregation}&period={period}",
                        "participantCount", "sum", "day"
                )
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<List<StatisticSeriesResponse>>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getMessage() != null;
                    assert body.getData() != null;
                });
    }

}