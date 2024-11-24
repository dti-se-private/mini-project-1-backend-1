package org.dti.se.miniproject1backend1.inners.models.entities;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.miniproject1backend1.inners.models.Model;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;


@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "account")
public class Account extends Model implements Persistable<UUID> {
    @Id
    private UUID id;
    private String name;
    private String email;
    private String password;
    private String phone;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime dob;
    private String referralCode;

    @Transient
    @Builder.Default
    public Boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }
}
