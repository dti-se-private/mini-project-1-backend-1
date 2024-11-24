package org.dti.se.module3session11.inners.models.valueobjects;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.module3session11.inners.models.Model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CreateVoucherForEventRequest extends Model {
    private String name;
    private String description;
    private BigDecimal variableAmount;
    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;
}
