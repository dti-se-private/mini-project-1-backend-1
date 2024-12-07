package org.dti.se.miniproject1backend1;

import org.dti.se.miniproject1backend1.inners.models.entities.Event;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.CreateEventRequest;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventResponse;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.vouchers.CreateVoucherRequest;
import org.junit.jupiter.api.*;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrganizerEventRestTest extends TestConfiguration {
    @BeforeAll
    public void beforeAll() {
        configure();
        populate();
        auth();
    }

    @AfterAll
    public void afterAll() {
        deauth();
        depopulate();
    }

    @Test
    @Order(1)
    public void testCreateNewEvent() {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        List<CreateVoucherRequest> vouchers = new ArrayList<>();
        CreateVoucherRequest testVoucher = CreateVoucherRequest.builder()
                .name("Test Voucher")
                .description("Description for Test Voucher")
                .variableAmount(7.00)
                .startedAt(now)
                .endedAt(now.plusDays(7))
                .build();
        vouchers.add(testVoucher);

        CreateEventRequest testEvent = CreateEventRequest.builder()
                .name("Test Event")
                .description("Description for Test Event")
                .category("Test Category")
                .location("Test Location")
                .price(100000.00)
                .slots(100)
                .time(now.plusDays(15))
                .vouchers(vouchers)
                .build();

        ResponseBody<RetrieveEventResponse> responseBody = webTestClient
                .post()
                .uri("/organizer/events/create")
                .bodyValue(testEvent)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(new ParameterizedTypeReference<ResponseBody<RetrieveEventResponse>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(responseBody).isNotNull();
        StepVerifier.create(Mono.just(responseBody))
                .assertNext(body -> {
                    assertThat(body.getData().getName()).isEqualTo(testEvent.getName());
                    assertThat(body.getData().getLocation()).isEqualTo(testEvent.getLocation());
                })
                .verifyComplete();

        Event fakeEvent = Event.builder()
                .id(responseBody.getData().getId())
                .accountId(responseBody.getData().getOrganizerAccount().getId())
                .name(responseBody.getData().getName())
                .build();
        fakeEvents.add(fakeEvent);
    }

    @Test
    @Order(2)
    public void testRetrieveMany() {
        Event event = fakeEvents.stream()
                .filter(e -> e.getAccountId().equals(authenticatedAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No event found for the authenticated account"));

        ResponseBody<List<RetrieveEventResponse>> responseBody = webTestClient
                .get()
                .uri("/organizer/events?page=0&size=10")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<List<RetrieveEventResponse>>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(responseBody).isNotNull();
        StepVerifier.create(Mono.just(responseBody))
                .assertNext(body -> assertThat(body.getData())
                        .extracting(RetrieveEventResponse::getName)
                        .contains(event.getName()))
                .verifyComplete();
    }

    @Test
    @Order(3)
    public void testGetEventDetail() {
        Event event = fakeEvents.stream()
                .filter(e -> e.getAccountId().equals(authenticatedAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No event found for the authenticated account"));

        ResponseBody<RetrieveEventResponse> responseBody = webTestClient
                .get()
                .uri("/organizer/events/{id}", event.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<RetrieveEventResponse>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(responseBody).isNotNull();
        StepVerifier.create(Mono.just(responseBody))
                .assertNext(body -> assertThat(body.getData().getName()).isEqualTo(event.getName()))
                .verifyComplete();
    }

    @Test
    @Order(4)
    public void testUpdateEvent() {
        Event event = fakeEvents.stream()
                .filter(e -> e.getAccountId().equals(authenticatedAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No event found for the authenticated account"));

        ResponseBody<RetrieveEventResponse> responseBodyPrepare = webTestClient
                .get()
                .uri("/organizer/events/{id}", event.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<RetrieveEventResponse>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(responseBodyPrepare).isNotNull();
        RetrieveEventResponse eventResponse = responseBodyPrepare.getData();
        eventResponse.setName("Updated Test Event");

        ResponseBody<RetrieveEventResponse> responseBody = webTestClient
                .patch()
                .uri("/organizer/events/{id}", event.getId())
                .bodyValue(eventResponse)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<RetrieveEventResponse>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(responseBody).isNotNull();
        StepVerifier.create(Mono.just(responseBody))
                .assertNext(body -> assertThat(body.getData().getName()).isEqualTo("Updated Test Event"))
                .verifyComplete();
    }
}