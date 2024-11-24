package org.dti.se.miniproject1backend1.inners.models.entities;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.miniproject1backend1.inners.models.Model;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "transaction")
public class Transaction extends Model {
    @Id
    private UUID id;
    private UUID accountId;
    private UUID eventId;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime timestamp;
}
