package org.dti.se.miniproject1backend1.inners.models.valueobjects.transactions;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.miniproject1backend1.inners.models.Model;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class TransactionCountRequest extends Model {
    private UUID eventId;
    private Long transactionCount;
    private OffsetDateTime latestEventTime;
}
