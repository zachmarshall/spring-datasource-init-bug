package org.example;

import javax.sql.DataSource;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ExampleConfig
{
    @Primary
    public DataSource primaryDataSource(DataSource pooledDataSource)
    {
        return new DummyProxyDataSource(pooledDataSource);
    }
}
