package org.dti.se.miniproject1backend1;

import org.dti.se.miniproject1backend1.inners.models.entities.Event;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Objects;

public class EventRestTest extends TestConfiguration {
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
    public void testGetAllEvents() {
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
                    assert body.getMessage() != null;
                    assert body.getData() != null;

                    body.getData().forEach(eventResponse -> {
                        assert eventResponse.getId() != null;
                        assert eventResponse.getName() != null;
                    });
                });
    }

    @Test
    public void testGetEventById() {
        Event realEvent = eventRepository.findAll().blockFirst();

        webTestClient
                .get()
                .uri("/events/{id}", Objects.requireNonNull(realEvent).getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<RetrieveEventResponse>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getMessage() != null;
                    assert body.getData() != null;

                    RetrieveEventResponse eventResponse = body.getData();
                    assert eventResponse.getId() != null;
                    assert eventResponse.getName() != null;
                });
    }
}