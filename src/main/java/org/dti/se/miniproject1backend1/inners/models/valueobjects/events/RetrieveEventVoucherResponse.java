package org.dti.se.miniproject1backend1.inners.models.valueobjects.events;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
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
public class RetrieveEventVoucherResponse extends Model {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private Double variableAmount;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime startedAt;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime endedAt;
}
