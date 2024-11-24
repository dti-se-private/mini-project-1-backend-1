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
@Table(name = "event_voucher")
public class EventVoucher extends Model {
    @Id
    private UUID id;
    private UUID voucherId;
    private UUID eventId;
}
