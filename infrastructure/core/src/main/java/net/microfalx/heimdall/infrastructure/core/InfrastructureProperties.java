package net.microfalx.heimdall.infrastructure.core;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("heimdall.infrustructure")
@Getter
public class InfrastructureProperties {

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

