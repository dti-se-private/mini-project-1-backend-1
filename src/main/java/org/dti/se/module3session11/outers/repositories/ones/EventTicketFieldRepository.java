package org.dti.se.module3session11.outers.repositories.ones;

import org.dti.se.module3session11.inners.models.entities.EventTicketField;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventTicketFieldRepository extends R2dbcRepository<EventTicketField, UUID> {
}
