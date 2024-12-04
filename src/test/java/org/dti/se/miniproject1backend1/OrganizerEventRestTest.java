package org.dti.se.miniproject1backend1;

import org.dti.se.miniproject1backend1.inners.models.entities.Event;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.CreateEventRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class OrganizerEventRestTest extends TestConfiguration {

    private Event testEvent;

    @BeforeEach
    public void setUp() {
        configure();
        populate();
        auth();

        /*testEvent = CreateEventRequest.builder()
                .name("Test Event")
                .description("Description for Test Event")
                .(authAccount.getId())
                .build();*/
    }

    /*@Test
    public void testCreateEvent() {
        ResponseBody<Event> responseBody = webTestClient
                .post()
                .uri("/events")
                .bodyValue(testEvent)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(new ParameterizedTypeReference<ResponseBody<Event>>() {})
                .returnResult()
                .getResponseBody();

        StepVerifier.create(Mono.just(responseBody))
                .expectNextMatches(body -> body != null && body.getData().getName().equals(testEvent.getName()))
                .verifyComplete();
    }

    @Test
    public void testGetEvent() {
        webTestClient
                .get()
                .uri("/events/{eventId}", testEvent.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.data.name").isEqualTo(testEvent.getName())
                .jsonPath("$.data.description").isEqualTo(testEvent.getDescription());
    }

    @Test
    public void testUpdateEvent() {
        // Update event logic
        testEvent.setName("Updated Event Name");

        webTestClient
                .put()
                .uri("/events/{eventId}", testEvent.getId())
                .bodyValue(testEvent)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.data.name").isEqualTo("Updated Event Name");
    }*/

    @AfterEach
    public void tearDown() {
        deauth();
        depopulate();
    }
}