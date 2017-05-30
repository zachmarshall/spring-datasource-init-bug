package org.example;

import javax.sql.DataSource;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.EmbeddedDatabaseConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;

/**
 * Simple example configuration that uses a {@link Primary} {@link DataSource} bean that depends on a secondary
 * {@link DataSource} bean. This configuration will fail to start when the {@link DataSourceInitializer} is enabled. See
 * the README.md for why. Note this example uses an {@link EmbeddedDatabaseBuilder} because it is quick and simple, but
 * this general approach fails any time there is a dependency between DataSource beans like this (i.e., if you autowire
 * a DataSource in the primary bean, that also fails).
 */
@SpringBootConfiguration
@EnableAutoConfiguration
public class ExampleConfig
{
    @Primary
    @Bean
    public DataSource primaryDataSource()
    {
        return new DummyProxyDataSource(secondaryDataSource());
    }

    @Bean
    public DataSource secondaryDataSource()
    {
        // just use an EmbeddedDataSourceBuilder for purposes of illustration
        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder().setType(
            EmbeddedDatabaseConnection.get(Thread.currentThread().getContextClassLoader()).getType());
        DataSource dataSource = builder.setName("testdatasource").build();
        return dataSource;
    }
}
