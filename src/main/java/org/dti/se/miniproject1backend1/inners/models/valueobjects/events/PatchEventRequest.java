package org.dti.se.miniproject1backend1.inners.models.valueobjects.events;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.miniproject1backend1.inners.models.Model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PatchEventRequest extends Model {
    private UUID id;
    private String name;
    private String description;
    private String location;
    private String category;
    private OffsetDateTime time;
    private String bannerImageUrl;
    private List<PatchEventTicketRequest> eventTickets;
    private List<PatchEventVoucherRequest> eventVouchers;
}
