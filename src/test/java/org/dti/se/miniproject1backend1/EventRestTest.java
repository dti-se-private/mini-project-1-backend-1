package org.dti.se.miniproject1backend1;

import org.dti.se.miniproject1backend1.inners.models.entities.Account;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.Session;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
        .expectBody(new ParameterizedTypeReference<ResponseBody<List<RetrieveEventResponse>>>() {
                    })
                            .value(body -> {
                        assert body != null;
                        assert body.getMessage().equals(expectedMessage);
                        assert body.getData() != null;

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
                .expectBody(new ParameterizedTypeReference<ResponseBody<List<RetrieveEventResponse>>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getMessage().equals(expectedMessage);
                    assert body.getData() != null;

                    body.getData().stream().forEach(eventResponse -> {
                        assert eventResponse.getId() != null;
                        assert eventResponse.getName() != null;
                    });
                });
    }

    @Test
    public void testGetEventById() {
        String expectedMessage = "Event detail fetched.";

        webTestClient
                .get()
                .uri("/events/072675ce-e5aa-42a9-a7fd-da2efff82489")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<RetrieveEventResponse>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getMessage().equals(expectedMessage);
                    assert body.getData() != null;

                    RetrieveEventResponse eventResponse = body.getData();
                    assert eventResponse.getId() != null;
                    assert eventResponse.getName() != null;
                });
    }
}