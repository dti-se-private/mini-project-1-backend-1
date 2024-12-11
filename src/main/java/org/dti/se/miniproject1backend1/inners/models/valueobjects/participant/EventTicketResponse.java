package org.dti.se.miniproject1backend1.inners.models.valueobjects.participant;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.miniproject1backend1.inners.models.Model;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class EventTicketResponse extends Model {
    private UUID id;
    private String name;
    private String description;
    private Double price;
    private Integer slots;
}
