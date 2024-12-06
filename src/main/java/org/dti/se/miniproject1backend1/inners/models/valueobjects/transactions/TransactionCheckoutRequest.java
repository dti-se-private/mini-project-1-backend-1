package org.dti.se.miniproject1backend1.inners.models.valueobjects.transactions;

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
public class TransactionCheckoutRequest extends Model {
    private UUID eventId;
    private List<TransactionTicketCheckoutRequest> transactionTickets;
    private List<String> voucherCodes;
    private Double points;
}
