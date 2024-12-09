package org.dti.se.miniproject1backend1.outers.repositories.ones;

import org.dti.se.miniproject1backend1.inners.models.entities.Event;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface EventRepository extends R2dbcRepository<Event, UUID> {

    Mono<Event> findFirstById(UUID id);
}