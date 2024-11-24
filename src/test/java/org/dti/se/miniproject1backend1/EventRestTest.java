package org.dti.se.miniproject1backend1;

import org.dti.se.miniproject1backend1.inners.models.entities.Account;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.EventResponse;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Flux;

@Disabled
public class EventRestTest extends TestConfiguration {
    Account authenticatedAccount;
    Session authenticatedSession;

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
    public void testGetHero() {
        String expectedMessage = "Hero fetched.";

        webTestClient
                .get()
                .uri("/events/hero")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<Flux<EventResponse>>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getMessage().equals(expectedMessage);
                    assert body.getData() != null;

                    body.getData().collectList().block().forEach(eventResponse -> {
                        assert eventResponse.getId() != null;
                        assert eventResponse.getName() != null;
                    });
                });
    }
}
