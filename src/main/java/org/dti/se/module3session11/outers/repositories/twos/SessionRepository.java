package org.dti.se.module3session11.outers.repositories.twos;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.dti.se.module3session11.inners.models.valueobjects.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;


@Repository
public class SessionRepository {

    @Autowired
    ReactiveRedisTemplate<String, String> stringTemplate;

    public Mono<Boolean> setByAccessToken(Session session) {
        return stringTemplate
                .opsForValue()
                .set(
                        session.getAccessToken(),
                        session.toJsonString(),
                        Duration.between(
                                OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS).toInstant(),
                                session.getAccessTokenExpiredAt().toInstant()
                        )
                );
    }


    public Mono<Session> getByAccessToken(String accessToken) {
        return stringTemplate
                .opsForValue()
                .get(accessToken)
                .handle((jsonString, sink) -> {
                    try {
                        sink.next(Jackson2ObjectMapperBuilder.json().build().readValue(jsonString, Session.class));
                    } catch (JsonProcessingException e) {
                        sink.error(new RuntimeException(e));
                    }
                });
    }


    public Mono<Boolean> deleteByAccessToken(String accessToken) {
        return stringTemplate
                .opsForValue()
                .delete(accessToken);
    }

}
