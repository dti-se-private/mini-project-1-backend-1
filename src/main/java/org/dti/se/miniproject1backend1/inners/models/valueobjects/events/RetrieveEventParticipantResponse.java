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
public class RetrieveEventParticipantResponse extends Model {
    private UUID accountId;
    private UUID transactionId;
    private String eventTicketId;
    private List<RetrieveEventParticipantFieldResponse> fields;
}
