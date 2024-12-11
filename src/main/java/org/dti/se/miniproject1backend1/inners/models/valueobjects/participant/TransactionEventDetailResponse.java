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
public class TransactionEventDetailResponse extends Model {
    private UUID id;
    private String name;
    private String description;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime time;
    private String location;
    private String category;
    private List<UsedVoucherResponse> usedVouchers;
}
