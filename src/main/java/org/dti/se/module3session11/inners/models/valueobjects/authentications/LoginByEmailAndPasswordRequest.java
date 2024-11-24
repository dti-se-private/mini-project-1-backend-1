package org.dti.se.module3session11.inners.models.valueobjects.authentications;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.module3session11.inners.models.Model;


@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class LoginByEmailAndPasswordRequest extends Model {
    private String email;
    private String password;
}
