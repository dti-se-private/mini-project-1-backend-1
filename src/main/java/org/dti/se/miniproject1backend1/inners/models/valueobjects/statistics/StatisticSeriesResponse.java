package org.dti.se.miniproject1backend1.inners.models.valueobjects.statistics;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.miniproject1backend1.inners.models.Model;

import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class StatisticSeriesResponse extends Model {
    private OffsetDateTime x;
    private Double y;
}
