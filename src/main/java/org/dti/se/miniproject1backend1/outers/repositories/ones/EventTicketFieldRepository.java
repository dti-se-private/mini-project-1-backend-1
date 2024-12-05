package org.dti.se.miniproject1backend1.outers.repositories.ones;

import org.dti.se.miniproject1backend1.inners.models.entities.EventTicketField;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventTicketFieldRepository extends R2dbcRepository<EventTicketField, UUID> {
    Flux<EventTicketField> findAllByEventTicketIdIn(List<UUID> eventTicketIds);
}
