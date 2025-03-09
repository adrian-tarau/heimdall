package net.microfalx.heimdall.infrastructure.ping;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.heimdall.infrastructure.api.InfrastructureConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties("heimdall.infrustructure.ping")
@Getter
@Setter
public class PingProperties {

    /**
     * The interval between pings
     */
    private Duration interval = Duration.ofSeconds(5);

    /**
     * The number of threads used to ping services.
     */
    private int threads = InfrastructureConstants.WINDOW_SIZE;

    /**
     * The number of pings used to calculate the health of a service.
     * <p>
     * The number of failed pings as percentage relative to this window will be used against
     * thresholds to calculate the health.
     */
    private int windowSize = 20;

    /**
     * A threshold (percentage) which determines after how many failed pings a services will be in {@link net.microfalx.heimdall.infrastructure.api.Health#DEGRADED}.
     */
    private float degradedThreshold = 5f;

    /**
     * A threshold (percentage) which determines after how many failed pings a services will be in {@link net.microfalx.heimdall.infrastructure.api.Health#UNHEALTHY}.
     */
    private float unhealthyThreshold = 20f;
}
