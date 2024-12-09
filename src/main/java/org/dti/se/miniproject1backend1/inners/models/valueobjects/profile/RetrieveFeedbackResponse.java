package org.dti.se.miniproject1backend1.inners.models.valueobjects.profile;

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
public class RetrieveFeedbackResponse extends Model {
    private UUID transactionId;
    private UUID eventId;
    private String eventName;
    private OffsetDateTime time;
}
