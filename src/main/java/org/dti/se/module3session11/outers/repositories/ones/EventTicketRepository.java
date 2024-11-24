package org.dti.se.module3session11.outers.repositories.ones;

import org.dti.se.module3session11.inners.models.entities.EventTicket;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface EventTicketRepository extends R2dbcRepository<EventTicket, UUID> {
    Mono<EventTicket> findByEventId(UUID eventId);
}
