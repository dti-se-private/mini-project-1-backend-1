package org.dti.se.miniproject1backend1.inners.models.valueobjects.events;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.miniproject1backend1.inners.models.Model;
import org.dti.se.miniproject1backend1.inners.models.valueobjects.vouchers.RetrieveVoucherResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RetrieveEventResponse extends Model {
    private UUID id;
    private UUID accountId;
    private String name;
    private String description;
    private String location;
    private String category;
    private OffsetDateTime time;
    private BigDecimal price;
    private Integer slots;
    private List<RetrieveVoucherResponse> vouchers;
}
