package org.dti.se.module3session11.outers.configurations.datastores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.Objects;


@Configuration
public class TwoDatastoreConfiguration {

    @Autowired
    private Environment environment;

    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(Objects.requireNonNull(environment.getProperty("datastore.two.host")));
        config.setPort(Integer.parseInt(Objects.requireNonNull(environment.getProperty("datastore.two.port"))));
        config.setPassword(environment.getProperty("datastore.two.password"));

        return new LettuceConnectionFactory(config);
    }

}
