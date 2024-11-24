package org.dti.se.module3session11.inners.models.valueobjects.authentications;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.module3session11.inners.models.Model;

import java.time.OffsetDateTime;


@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RegisterByEmailAndPasswordRequest extends Model {
    private String name;
    private String email;
    private String password;
    private String phone;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime dob;
    private String referralCode;
}
