package org.dti.se.miniproject1backend1.inners.models.entities;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.miniproject1backend1.inners.models.Model;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "event_ticket_field")
public class EventTicketField extends Model {
    @Id
    private UUID id;
    private UUID eventTicketId;
    private String key;
}
