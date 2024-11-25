package org.dti.se.miniproject1backend1;

import org.dti.se.miniproject1backend1.inners.models.entities.Account;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.Session;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;


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
<<<<<<< Updated upstream
                .expectBody(new ParameterizedTypeReference<ResponseBody<Flux<RetrieveEventResponse>>>() {
=======
                .expectBody(new ParameterizedTypeReference<ResponseBody<List<EventResponse>>>() {
>>>>>>> Stashed changes
                })
                .value(body -> {
                    assert body != null;
                    assert body.getMessage().equals(expectedMessage);
                    assert body.getData() != null;

<<<<<<< Updated upstream
                    body.getData().collectList().block().forEach(retrieveEventResponse -> {
                        assert retrieveEventResponse.getId() != null;
                        assert retrieveEventResponse.getName() != null;
=======
                    body.getData().stream().forEach(eventResponse -> {
                        assert eventResponse.getId() != null;
                        assert eventResponse.getName() != null;
                    });
                });
    }

    @Test
    public void testGetAllEvents() {
        String expectedMessage = "Events by category fetched.";

        webTestClient
                .get()
                .uri("/events")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<List<EventResponse>>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getMessage().equals(expectedMessage);
                    assert body.getData() != null;

                    body.getData().stream().forEach(eventResponse -> {
                        assert eventResponse.getId() != null;
                        assert eventResponse.getName() != null;
>>>>>>> Stashed changes
                    });
                });
    }
}
