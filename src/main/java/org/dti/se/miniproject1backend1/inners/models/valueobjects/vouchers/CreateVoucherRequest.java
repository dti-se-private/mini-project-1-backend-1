package org.dti.se.miniproject1backend1.inners.models.valueobjects.vouchers;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.miniproject1backend1.inners.models.Model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CreateVoucherRequest extends Model {
    private String code;
    private String name;
    private String description;
    private Double variableAmount;
    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;
}
