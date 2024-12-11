package org.dti.se.miniproject1backend1.inners.models.valueobjects.participant;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.miniproject1backend1.inners.models.Model;

import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UsedVoucherResponse extends Model {
    private String code;
    private String name;
    private String description;
    private Double variableAmount;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime endedAt;
}
