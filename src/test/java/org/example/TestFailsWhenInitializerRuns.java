package org.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * This test fails because the {@link DataSourceInitializer} is allowed to run. See README.md for more details on why.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestFailsWhenInitializerRuns
{
    @Test
    public void appStarts()
    {
        System.out.println("This line never prints since the application context fails to start.");
    }
}
