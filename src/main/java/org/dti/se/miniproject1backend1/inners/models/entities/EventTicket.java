package org.dti.se.miniproject1backend1.inners.models.entities;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.miniproject1backend1.inners.models.Model;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "event_ticket")
public class EventTicket extends Model {
    @Id
    private UUID id;
    private UUID eventId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer slots;
}
