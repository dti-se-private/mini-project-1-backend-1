package org.dti.se.miniproject1backend1.outers.configurations.datastores;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.transaction.support.DefaultTransactionDefinition;


@Configuration
@EnableR2dbcRepositories(
        basePackages = "org.dti.se.miniproject1backend1.outers.repositories.ones",
        entityOperationsRef = "oneTemplate"
)
@EnableTransactionManagement
public class OneDatastoreConfiguration {

    @Autowired
    private Environment environment;

    @Bean
    public ConnectionFactory oneConnectionFactory() {
        String url = String.format(
                "r2dbc:postgresql://%s:%s@%s:%s/%s?preparedStatementCacheQueries=0",
                environment.getProperty("datastore.one.user"),
                environment.getProperty("datastore.one.password"),
                environment.getProperty("datastore.one.host"),
                environment.getProperty("datastore.one.port"),
                environment.getProperty("datastore.one.database")
        );
        return ConnectionFactories
                .get(url);
    }

    @Bean
    public R2dbcEntityTemplate oneTemplate(@Qualifier("oneConnectionFactory") ConnectionFactory connectionFactory) {
        return new R2dbcEntityTemplate(connectionFactory);
    }

    @Bean
    public R2dbcTransactionManager oneTransactionManager(@Qualifier("oneConnectionFactory") ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean
    public TransactionalOperator oneTransactionalOperator(@Qualifier("oneTransactionManager") R2dbcTransactionManager transactionManager) {
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        transactionDefinition.setIsolationLevel(Isolation.DEFAULT.value());
        return TransactionalOperator.create(transactionManager, transactionDefinition);
    }

}
