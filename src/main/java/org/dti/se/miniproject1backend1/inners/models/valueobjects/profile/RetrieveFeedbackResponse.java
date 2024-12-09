package org.dti.se.miniproject1backend1.inners.models.valueobjects.profile;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.miniproject1backend1.inners.models.Model;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RetrieveFeedbackResponse extends Model {
    private UUID id;
    private Integer rating;
    private String review;
}
