package org.dti.se.module3session11.inners.models.valueobjects;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.module3session11.inners.models.Model;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Session extends Model {
    private UUID accountId;
    private String accessToken;
    private String refreshToken;
    private OffsetDateTime accessTokenExpiredAt;
    private OffsetDateTime refreshTokenExpiredAt;
}
