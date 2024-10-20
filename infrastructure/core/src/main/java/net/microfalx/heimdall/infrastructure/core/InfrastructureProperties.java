package net.microfalx.heimdall.infrastructure.core;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.heimdall.infrastructure.api.InfrastructureConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("heimdall.infrastructure")
@Getter
@Setter
public class InfrastructureProperties {

    /**
     * The number of pings used to calculate the health of a service.
     * <p>
     * The number of failed pings as percentage relative to this window will be used against
     * thresholds to calculate the health.
     */
    private int windowSize = InfrastructureConstants.WINDOW_SIZE;

    /**
     * The health stats refresh schedule
     */
    private String schedule = "*/30 * * * * *";

    /**
     * A threshold (percentage) which determines after how many degraded infrastructure elements
     * the overview infrastructure will be in {@link net.microfalx.heimdall.infrastructure.api.Health#DEGRADED}.
     */
    private float degradedThreshold = 20f;

    /**
     * A threshold (percentage) which determines after how many unhealthy infrastructure elements
     * the overview infrastructure will be in {@link net.microfalx.heimdall.infrastructure.api.Health#UNHEALTHY}.
     */
    private float unhealthyThreshold = 33f;

    /**
     * A threshold (percentage) which determines after how many unavailable infrastructure elements
     * the overview infrastructure will be in {@link net.microfalx.heimdall.infrastructure.api.Health#DEGRADED}.
     */
    private float unavailableThreshold = 50f;
}

