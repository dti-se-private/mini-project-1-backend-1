package org.dti.se.miniproject1backend1.inners.models.valueobjects.participant;

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
public class TransactionDetailResponse extends Model {
    private UUID transactionId;
    private UUID eventId;
    private List<UsedPointResponse> usedPoints;
    private List<UsedVoucherResponse> usedVouchers;
}
