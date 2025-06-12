package net.microfalx.heimdall.broker.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("heimdall.broker")
@Getter
@Setter
public class BrokerProperties {

    /**
     * Indicates whether the broker service is enabled.
     * <p>
     * When enabled, all registered consumers and producers will be started.
     */
    private boolean enabled = true;
}
