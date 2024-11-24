package org.dti.se.module3session11.inners.models.valueobjects;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.module3session11.inners.models.Model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CreateEventRequest extends Model {
    private UUID accountId;
    private String name;
    private String description;
    private String location;
    private String category;
    private OffsetDateTime time;
    private BigDecimal price;
    private Integer slots;
    private CreateVoucherForEventRequest[] vouchers;
}
