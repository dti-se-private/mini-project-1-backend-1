package org.dti.se.module3session11.inners.models.entities;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.module3session11.inners.models.Model;
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
@Table(name = "event")
public class Event extends Model {
    @Id
    private UUID id;
    private UUID accountId;
    private String name;
    private String description;
    private String location;
    private String category;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime time;
}
