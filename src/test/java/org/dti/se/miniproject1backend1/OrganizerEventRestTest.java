package org.dti.se.miniproject1backend1;

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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrganizerEventRestTest extends TestConfiguration {

    private CreateEventRequest testEvent;
    private RetrieveEventResponse testEventResponse;

    @BeforeEach
    public void setUp() {
        configure();
        populate();
        auth();

        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        CreateVoucherRequest testVoucher = CreateVoucherRequest.builder()
                .name("Test Voucher")
                .description("Description for Test Voucher")
                .code(UUID.randomUUID().toString())
                .variableAmount(7.00)
                .startedAt(now)
                .endedAt(now.plusDays(7))
                .build();

        testEvent = CreateEventRequest.builder()
                .name("Test Event")
                .description("Description for Test Event")
                .category("Test Category")
                .location("Test Location")
                .price(100000.00)
                .slots(100)
                .vouchers(new CreateVoucherRequest[]{testVoucher})
                .build();
    }

    @Test
    public void createNewEvent() {
        ResponseBody<RetrieveEventResponse> responseBody = webTestClient
                .post()
                .uri("/organizer/events")
                .bodyValue(testEvent)
                .exchange()
                .expectStatus()
                .isCreated()
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

        testEventResponse = responseBody.getData();
    }

    @Test
    public void testRetrieveMany() {
        createNewEvent();

        ResponseBody<List<RetrieveEventResponse>> responseBody = webTestClient
                .get()
                .uri("/organizer/events")
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
                        .contains(testEvent.getName()))
                .verifyComplete();
    }

    @Test
    public void testGetEventDetail() {
        createNewEvent();

        ResponseBody<RetrieveEventResponse> responseBody = webTestClient
                .get()
                .uri("/organizer/events/{id}", testEventResponse.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<RetrieveEventResponse>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(responseBody).isNotNull();
        StepVerifier.create(Mono.just(responseBody))
                .assertNext(body -> assertThat(body.getData().getName()).isEqualTo(testEvent.getName()))
                .verifyComplete();
    }

    @Test
    public void testUpdateEvent() {
        createNewEvent();

        testEventResponse.setName("Updated Test Event");

        ResponseBody<RetrieveEventResponse> responseBody = webTestClient
                .put()
                .uri("/organizer/events/{id}", testEventResponse.getId())
                .bodyValue(testEventResponse)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<RetrieveEventResponse>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(responseBody).isNotNull();
        StepVerifier.create(Mono.just(responseBody))
                .assertNext(body -> assertThat(body.getData().getName()).isEqualTo("Updated Test Event"))
                .verifyComplete();
    }

    @Test
    public void testInvalidEventRetrieval() {
        ResponseBody<RetrieveEventResponse> responseBody = webTestClient
                .get()
                .uri("/organizer/events/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(new ParameterizedTypeReference<ResponseBody<RetrieveEventResponse>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(responseBody).isNull();
    }

    @AfterEach
    public void tearDown() {
        deauth();
        depopulate();
    }
}
