package org.dti.se.miniproject1backend1.inners.models.valueobjects.events;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.miniproject1backend1.inners.models.Model;
import org.springframework.data.annotation.Id;

import java.time.OffsetDateTime;
import java.util.UUID;


@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RetrieveOrganizerAccountResponse extends Model {
    @Id
    private UUID id;
    private String name;
    private String email;
    private String phone;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime dob;
    private String profileImageUrl;
}
