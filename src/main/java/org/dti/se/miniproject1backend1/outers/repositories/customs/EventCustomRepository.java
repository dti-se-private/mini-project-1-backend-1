package org.dti.se.miniproject1backend1.outers.repositories.customs;

import org.dti.se.miniproject1backend1.inners.models.valueobjects.events.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class EventCustomRepository {

    @Autowired
    @Qualifier("oneTemplate")
    private R2dbcEntityTemplate oneTemplate;

    public Flux<RetrieveEventResponse> retrieveEvents(Integer page, Integer size, List<String> filters, String search) {
        return Mono
                .fromCallable(() -> filters
                        .stream()
                        .map(filter -> String.format("SIMILARITY(%s::text, '%s')", filter, search))
                        .collect(Collectors.joining("+"))
                )
                .map(similarities -> String.format("""
                        SELECT event.*
                        FROM event
                        ORDER BY %s DESC
                        LIMIT :limit
                        OFFSET :offset
                        """, similarities)
                )
                .flatMapMany(query -> oneTemplate
                        .getDatabaseClient()
                        .sql(query)
                        .bind("limit", size)
                        .bind("offset", page * size)
                        .map((row, rowMetadata) -> RetrieveEventResponse
                                .builder()
                                .id(row.get("id", UUID.class))
                                .name(row.get("name", String.class))
                                .description(row.get("description", String.class))
                                .category(row.get("category", String.class))
                                .time(row.get("time", OffsetDateTime.class))
                                .location(row.get("location", String.class))
                                .bannerImageUrl(row.get("banner_image_url", String.class))
                                .eventTickets(new ArrayList<>())
                                .eventVouchers(new ArrayList<>())
                                .build()
                        )
                        .all()
                )
                .flatMap(event -> retrieveOrganizerAccountsByEventIds(List.of(event.getId()))
                        .map(event::setOrganizerAccount)
                )
                .flatMap(event -> retrieveEventTicketsByEventIds(List.of(event.getId()))
                        .collectList()
                        .map(event::setEventTickets)
                )
                .flatMap(event -> retrieveEventVouchersByEventIds(List.of(event.getId()))
                        .collectList()
                        .map(event::setEventVouchers)
                )
                .flatMap(event -> retrieveEventParticipantCountByEventId(event.getId())
                        .map(event::setParticipantCount)
                );
    }

    public Mono<RetrieveEventResponse> retrieveEventById(UUID id) {
        return oneTemplate
                .getDatabaseClient()
                .sql("""
                        SELECT event.*
                        FROM event
                        WHERE event.id = :id
                        LIMIT 1
                        """)
                .bind("id", id)
                .map((row, rowMetadata) -> RetrieveEventResponse
                        .builder()
                        .id(row.get("id", UUID.class))
                        .name(row.get("name", String.class))
                        .description(row.get("description", String.class))
                        .category(row.get("category", String.class))
                        .time(row.get("time", OffsetDateTime.class))
                        .location(row.get("location", String.class))
                        .bannerImageUrl(row.get("banner_image_url", String.class))
                        .eventTickets(new ArrayList<>())
                        .eventVouchers(new ArrayList<>())
                        .build()
                )
                .all()
                .flatMap(event -> retrieveOrganizerAccountsByEventIds(List.of(event.getId()))
                        .map(event::setOrganizerAccount)
                )
                .flatMap(event -> retrieveEventTicketsByEventIds(List.of(event.getId()))
                        .collectList()
                        .map(event::setEventTickets)
                )
                .flatMap(event -> retrieveEventVouchersByEventIds(List.of(event.getId()))
                        .collectList()
                        .map(event::setEventVouchers)
                )
                .flatMap(event -> retrieveEventParticipantCountByEventId(event.getId())
                        .map(event::setParticipantCount)
                )
                .single();
    }

    public Flux<RetrieveOrganizerAccountResponse> retrieveOrganizerAccountsByEventIds(List<UUID> eventIds) {
        return oneTemplate
                .getDatabaseClient()
                .sql("""
                        SELECT account.*
                        FROM account
                        INNER JOIN event ON event.account_id = account.id
                        WHERE event.id IN (:eventIds)
                        """)
                .bind("eventIds", eventIds)
                .map((row, rowMetadata) -> RetrieveOrganizerAccountResponse
                        .builder()
                        .id(row.get("id", UUID.class))
                        .email(row.get("email", String.class))
                        .name(row.get("name", String.class))
                        .phone(row.get("phone", String.class))
                        .dob(row.get("dob", OffsetDateTime.class))
                        .profileImageUrl(row.get("profile_image_url", String.class))
                        .build()
                )
                .all();
    }

    public Flux<RetrieveEventTicketFieldResponse> retrieveEventTicketFieldsByTicketId(UUID ticketId) {
        return oneTemplate
                .getDatabaseClient()
                .sql("""
                        SELECT event_ticket_field.*
                        FROM event_ticket_field
                        WHERE event_ticket_field.event_ticket_id = :ticketId
                        """)
                .bind("ticketId", ticketId)
                .map((row, rowMetadata) -> RetrieveEventTicketFieldResponse
                        .builder()
                        .id(row.get("id", UUID.class))
                        .key(row.get("key", String.class))
                        .build()
                )
                .all();
    }

    public Flux<RetrieveEventTicketResponse> retrieveEventTicketsByEventIds(List<UUID> eventIds) {
        return oneTemplate
                .getDatabaseClient()
                .sql("""
                        SELECT event_ticket.*
                        FROM event_ticket
                        WHERE event_ticket.event_id IN (:eventIds)
                        """)
                .bind("eventIds", eventIds)
                .map((row, rowMetadata) -> RetrieveEventTicketResponse
                        .builder()
                        .id(row.get("id", UUID.class))
                        .name(row.get("name", String.class))
                        .description(row.get("description", String.class))
                        .price(row.get("price", Double.class))
                        .slots(row.get("slots", Integer.class))
                        .fields(new ArrayList<>())
                        .build()
                )
                .all()
                .flatMap(ticket -> retrieveEventTicketFieldsByTicketId(ticket.getId())
                        .collectList()
                        .map(ticket::setFields)
                        .then(Mono.just(ticket))
                );
    }

    public Flux<RetrieveEventVoucherResponse> retrieveEventVouchersByEventIds(List<UUID> eventIds) {
        return oneTemplate
                .getDatabaseClient()
                .sql("""
                        SELECT voucher.*
                        FROM voucher
                        INNER JOIN event_voucher ON event_voucher.voucher_id = voucher.id
                        WHERE event_voucher.event_id IN (:eventIds)
                        """)
                .bind("eventIds", eventIds)
                .map((row, rowMetadata) -> RetrieveEventVoucherResponse
                        .builder()
                        .id(row.get("id", UUID.class))
                        .code(row.get("code", String.class))
                        .name(row.get("name", String.class))
                        .description(row.get("description", String.class))
                        .variableAmount(row.get("variable_amount", Double.class))
                        .startedAt(row.get("started_at", OffsetDateTime.class))
                        .endedAt(row.get("ended_at", OffsetDateTime.class))
                        .build()
                )
                .all();
    }

    public Mono<Integer> retrieveEventParticipantCountByEventId(UUID eventId) {
        return oneTemplate
                .getDatabaseClient()
                .sql("""
                        SELECT COUNT(transaction.id) AS count
                        FROM transaction
                        WHERE transaction.event_id = :eventId
                        """)
                .bind("eventId", eventId)
                .map((row, rowMetadata) -> row.get("count", Integer.class))
                .one();
    }
}
