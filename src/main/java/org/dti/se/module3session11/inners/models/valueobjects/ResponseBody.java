package org.dti.se.module3session11.inners.models.valueobjects;

import lombok.*;
import lombok.experimental.Accessors;
import org.dti.se.module3session11.inners.models.Model;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ResponseBody<T> extends Model {
    private String message;
    private T data;
    private Throwable error;

    public ResponseEntity<ResponseBody<T>> toEntity(HttpStatus status) {
        return ResponseEntity.status(status).body(this);
    }

}
