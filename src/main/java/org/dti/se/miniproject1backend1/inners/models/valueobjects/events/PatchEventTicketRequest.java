package org.dti.se.miniproject1backend1.inners.models.valueobjects.events;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.miniproject1backend1.inners.models.Model;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PatchEventTicketRequest extends Model {
    private UUID id;
    private String name;
    private String description;
    private Double price;
    private Integer slots;
    private List<PatchEventTicketFieldRequest> fields;
}
