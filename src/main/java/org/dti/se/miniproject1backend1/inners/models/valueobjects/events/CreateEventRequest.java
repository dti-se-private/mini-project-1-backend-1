package org.dti.se.miniproject1backend1.inners.models.valueobjects.events;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.miniproject1backend1.inners.models.Model;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.vouchers.CreateVoucherRequest;

import java.time.OffsetDateTime;

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
    private CreateVoucherRequest[] vouchers;
}
