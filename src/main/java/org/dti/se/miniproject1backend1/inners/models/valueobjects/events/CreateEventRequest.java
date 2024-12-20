package org.dti.se.miniproject1backend1.inners.models.valueobjects.events;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.miniproject1backend1.inners.models.Model;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CreateEventRequest extends Model {
    private String name;
    private String description;
    private String location;
    private String category;
    private OffsetDateTime time;
    private Double price;
    private Integer slots;
    private String bannerImageUrl;
    private List<CreateEventTicketRequest> eventTickets;
    private List<CreateEventVoucherRequest> eventVouchers;
}
