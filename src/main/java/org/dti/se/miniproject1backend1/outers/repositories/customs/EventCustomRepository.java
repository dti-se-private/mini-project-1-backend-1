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
                        OFFSET :offset;
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
                .flatMap(event -> retrieveEventParticipantsByEventIds(List.of(event.getId()))
                        .collectList()
                        .map(participants -> event
                                .setEventParticipants(participants)
                                .setParticipantCount(participants.size())
                        )
                );
    }

    public Mono<RetrieveEventResponse> retrieveEventById(UUID id) {
        return oneTemplate
                .getDatabaseClient()
                .sql("""
                        SELECT event.*
                        FROM event
                        WHERE event.id = :id::uuid
                        LIMIT 1;
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
                .flatMap(event -> retrieveEventParticipantsByEventIds(List.of(event.getId()))
                        .collectList()
                        .map(participants -> event
                                .setEventParticipants(participants)
                                .setParticipantCount(participants.size())
                        )
                )
                .single();
    }


    public Flux<RetrieveEventResponse> retrieveEventsByAccountId(UUID accountId, Integer page, Integer size) {
        return oneTemplate
                .getDatabaseClient()
                .sql("""
                        SELECT event.*
                        FROM event
                        WHERE event.account_id = :accountId::uuid
                        LIMIT :limit
                        OFFSET :offset;
                        """)
                .bind("accountId", accountId)
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
                .flatMap(event -> retrieveEventParticipantsByEventIds(List.of(event.getId()))
                        .collectList()
                        .map(participants -> event
                                .setEventParticipants(participants)
                                .setParticipantCount(participants.size())
                        )
                );
    }

    public Flux<RetrieveOrganizerAccountResponse> retrieveOrganizerAccountsByEventIds(List<UUID> eventIds) {
        return oneTemplate
                .getDatabaseClient()
                .sql("""
                        SELECT account.*
                        FROM account
                        INNER JOIN event ON event.account_id = account.id
                        WHERE event.id IN (:eventIds);
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
                        WHERE event_ticket_field.event_ticket_id = :ticketId::uuid;
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
                        WHERE event_ticket.event_id IN (:eventIds);
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
                        WHERE event_voucher.event_id IN (:eventIds);
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

    public Flux<RetrieveEventParticipantResponse> retrieveEventParticipantsByEventIds(List<UUID> eventIds) {
        return oneTemplate
                .getDatabaseClient()
                .sql("""
                        SELECT
                            a.id AS account_id,
                            t.id AS transaction_id,
                            et.id AS event_ticket_id
                        FROM account a
                        INNER JOIN transaction t ON t.account_id = a.id
                        INNER JOIN event_ticket et ON et.event_id = t.event_id
                        INNER JOIN event_ticket_field etf ON etf.event_ticket_id = et.id
                        INNER JOIN transaction_ticket_field ttf ON ttf.transaction_id = t.id
                        WHERE t.event_id IN (:eventIds)
                        GROUP BY a.id, t.id, et.id;
                        """)
                .bind("eventIds", eventIds)
                .map((row, rowMetadata) -> RetrieveEventParticipantResponse
                        .builder()
                        .accountId(row.get("account_id", UUID.class))
                        .transactionId(row.get("transaction_id", UUID.class))
                        .eventTicketId(row.get("event_ticket_id", String.class))
                        .fields(new ArrayList<>())
                        .build()
                )
                .all()
                .flatMap(participant -> oneTemplate
                        .getDatabaseClient()
                        .sql("""
                                SELECT
                                    etf.key as key,
                                    ttf.value as value
                                FROM event_ticket_field etf
                                INNER JOIN transaction_ticket_field ttf ON ttf.event_ticket_field_id = etf.id
                                WHERE ttf.transaction_id = :transactionId::uuid AND etf.event_ticket_id = :eventTicketId::uuid;
                                """)
                        .bind("transactionId", participant.getTransactionId())
                        .bind("eventTicketId", participant.getEventTicketId())
                        .map((row, rowMetadata) -> RetrieveEventParticipantFieldResponse
                                .builder()
                                .key(row.get("key", String.class))
                                .value(row.get("value", String.class))
                                .build()
                        )
                        .all()
                        .collectList()
                        .map(participant::setFields)
                );
    }
}
