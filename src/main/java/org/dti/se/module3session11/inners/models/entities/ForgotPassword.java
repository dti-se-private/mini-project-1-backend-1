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
@Table(name = "forgot_password")
public class ForgotPassword extends Model {
    @Id
    private UUID id;
    private UUID accountId;
    private String token;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime expiredAt;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    protected OffsetDateTime createdAt;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    protected OffsetDateTime updatedAt;
}
