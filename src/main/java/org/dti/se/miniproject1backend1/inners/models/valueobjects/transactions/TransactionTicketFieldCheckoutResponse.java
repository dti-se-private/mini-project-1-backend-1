package org.dti.se.miniproject1backend1.inners.models.valueobjects.transactions;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.miniproject1backend1.inners.models.Model;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class TransactionTicketFieldCheckoutResponse extends Model {
    private String key;
    private String value;
}
