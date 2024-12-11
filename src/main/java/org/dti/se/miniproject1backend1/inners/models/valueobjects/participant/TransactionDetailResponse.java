package org.dti.se.miniproject1backend1.inners.models.valueobjects.participant;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.miniproject1backend1.inners.models.Model;

import java.time.OffsetDateTime;
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
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime time;
    private List<UsedPointResponse> usedPoints;
    private List<UsedVoucherResponse> usedVouchers;
}
