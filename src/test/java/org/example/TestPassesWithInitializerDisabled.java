package org.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * This test passes because the {@link DataSourceInitializer} is disabled. See README.md for more details on why.
 */
@RunWith(SpringRunner.class)
@SpringBootTest("spring.datasource.initialize=false")
public class TestPassesWithInitializerDisabled
{
    @Test
    public void appStarts()
    {
        System.out.println("This test passes.");
    }
}
