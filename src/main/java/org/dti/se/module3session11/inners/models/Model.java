package org.dti.se.module3session11.inners.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Model implements Serializable {

    @SuppressWarnings("unchecked")
    public <T> T patchFrom(T object) {
        BeanUtils.copyProperties(object, this);
        return (T) this;
    }

    public String toJsonString() {
        return Jackson2ObjectMapperBuilder.json().build().valueToTree(this).toString();
    }

}
