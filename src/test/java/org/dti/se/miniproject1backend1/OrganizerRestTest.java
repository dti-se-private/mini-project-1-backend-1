package org.dti.se.miniproject1backend1;

import org.dti.se.miniproject1backend1.inners.models.entities.Event;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.CreateEventRequest;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.CreateEventTicketRequest;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.CreateEventVoucherRequest;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.RetrieveEventResponse;
import org.junit.jupiter.api.*;
import org.springframework.core.ParameterizedTypeReference;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrganizerRestTest extends TestConfiguration {
    @BeforeAll
    public void beforeAll() {
        configure();
        populate();
        auth(fakeAccounts.getFirst());
    }

    @AfterAll
    public void afterAll() {
        deauth();
        depopulate();
    }

    @Test
    public void testCreateEvent() {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        List<CreateEventVoucherRequest> eventVouchers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            eventVouchers.add(
                    CreateEventVoucherRequest
                            .builder()
                            .name(String.format("name-%s", UUID.randomUUID()))
                            .description(String.format("description-%s", UUID.randomUUID()))
                            .variableAmount(10.0)
                            .startedAt(now)
                            .endedAt(now.plusDays(1))
                            .build()
            );
        }


        List<CreateEventTicketRequest> eventTickets = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            eventTickets.add(
                    CreateEventTicketRequest
                            .builder()
                            .name(String.format("name-%s", UUID.randomUUID()))
                            .description(String.format("description-%s", UUID.randomUUID()))
                            .price(10.0)
                            .slots(100)
                            .fields(List.of("name", "email", "phone", "dob"))
                            .build()
            );
        }


        CreateEventRequest createEventRequest = CreateEventRequest
                .builder()
                .name(String.format("name-%s", UUID.randomUUID()))
                .description(String.format("description-%s", UUID.randomUUID()))
                .location(String.format("location-%s", UUID.randomUUID()))
                .category(String.format("category-%s", UUID.randomUUID()))
                .time(now.plusDays(1))
                .price(10.0)
                .slots(100)
                .bannerImageUrl(String.format("bannerImageUrl-%s", UUID.randomUUID()))
                .eventVouchers(eventVouchers)
                .eventTickets(eventTickets)
                .build();

        webTestClient
                .post()
                .uri("/organizers/events")
                .bodyValue(createEventRequest)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(new ParameterizedTypeReference<ResponseBody<RetrieveEventResponse>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getData() != null;
                    assert body.getData().getId() != null;
                    assert body.getData().getName().equals(createEventRequest.getName());
                    assert body.getData().getDescription().equals(createEventRequest.getDescription());
                    assert body.getData().getLocation().equals(createEventRequest.getLocation());
                    assert body.getData().getCategory().equals(createEventRequest.getCategory());
                    assert body.getData().getTime().equals(createEventRequest.getTime());
                    assert body.getData().getBannerImageUrl().equals(createEventRequest.getBannerImageUrl());
                    assert body.getData().getEventVouchers().size() == createEventRequest.getEventVouchers().size();
                    body.getData().getEventVouchers().forEach(voucher -> {
                        assert createEventRequest
                                .getEventVouchers()
                                .stream()
                                .anyMatch(voucherRequest -> voucherRequest.getName().equals(voucher.getName()));
                    });
                });
    }

    @Test
    public void testRetrieveEvents() {
        List<Event> accountEvents = fakeEvents
                .stream()
                .filter(e -> e.getAccountId().equals(authenticatedAccount.getId()))
                .toList();

        webTestClient
                .get()
                .uri("/organizers/events?page=0&size={size}", accountEvents.size())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<List<RetrieveEventResponse>>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getData() != null;
                    assert body.getData().size() == accountEvents.size();
                    body.getData().forEach(data -> {
                        assert accountEvents
                                .stream()
                                .anyMatch(event -> Objects.equals(event.getId(), data.getId()));
                    });
                });
    }

    @Test
    public void testRetrieveEvent() {
        Event accountEvent = fakeEvents
                .stream()
                .filter(e -> e.getAccountId().equals(authenticatedAccount.getId()))
                .findFirst()
                .orElseThrow();

        webTestClient
                .get()
                .uri("/organizers/events/{id}", accountEvent.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<RetrieveEventResponse>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getData() != null;
                    assert body.getData().getId().equals(accountEvent.getId());
                    assert body.getData().getName().equals(accountEvent.getName());
                    assert body.getData().getDescription().equals(accountEvent.getDescription());
                    assert body.getData().getLocation().equals(accountEvent.getLocation());
                    assert body.getData().getCategory().equals(accountEvent.getCategory());
                    assert body.getData().getTime().equals(accountEvent.getTime());
                    assert body.getData().getBannerImageUrl().equals(accountEvent.getBannerImageUrl());
                });
    }

    @Test
    public void testPatchEvent() {
        Event accountEvent = fakeEvents
                .stream()
                .filter(e -> e.getAccountId().equals(authenticatedAccount.getId()))
                .findFirst()
                .orElseThrow();

        webTestClient
                .get()
                .uri("/organizers/events/{id}", accountEvent.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ResponseBody<RetrieveEventResponse>>() {
                })
                .value(body -> {
                    assert body != null;
                    assert body.getData() != null;
                    assert body.getData().getId().equals(accountEvent.getId());
                    assert body.getData().getName().equals(accountEvent.getName());
                    assert body.getData().getDescription().equals(accountEvent.getDescription());
                    assert body.getData().getLocation().equals(accountEvent.getLocation());
                    assert body.getData().getCategory().equals(accountEvent.getCategory());
                    assert body.getData().getTime().equals(accountEvent.getTime());
                    assert body.getData().getBannerImageUrl().equals(accountEvent.getBannerImageUrl());
                });
    }
}