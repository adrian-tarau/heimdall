package net.microfalx.heimdall.database.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties("heimdall.database")
@Getter
@Setter
public class DatabaseProperties {

    /**
     * Indicates whether the database service is enabled.
     * <p>
     * When enabled, all registered consumers and producers will be started.
     */
    private boolean enabled = true;

    /**
     * The interval used to collect database statistics.
     */
    private Duration interval = Duration.ofSeconds(20);
}
