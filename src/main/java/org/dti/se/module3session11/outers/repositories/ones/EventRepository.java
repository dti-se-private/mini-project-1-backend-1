package org.dti.se.module3session11.outers.repositories.ones;

import org.dti.se.module3session11.inners.models.entities.Event;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface EventRepository extends R2dbcRepository<Event, UUID> {
    Flux<Event> findByCategoryIgnoreCase(String category);
}