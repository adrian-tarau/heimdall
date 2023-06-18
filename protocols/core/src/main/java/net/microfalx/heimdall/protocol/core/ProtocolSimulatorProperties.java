package net.microfalx.heimdall.protocol.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties("heimdall.protocol.simulator")
public class ProtocolSimulatorProperties {

    private boolean enabled;
    private Duration interval = Duration.ofSeconds(10);
    private int minimumEventCount = 1;
    private int maximumEventCount = 5;
    private int minimumAddressCount = 10;
    private int maximumAddressCount = 20;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getInterval() {
        return interval;
    }

    public void setInterval(Duration interval) {
        this.interval = interval;
    }

    public int getMinimumEventCount() {
        return minimumEventCount;
    }

    public void setMinimumEventCount(int minimumEventCount) {
        this.minimumEventCount = Math.max(1, minimumEventCount);
    }

    public int getMaximumEventCount() {
        return maximumEventCount;
    }

    public void setMaximumEventCount(int maximumEventCount) {
        this.maximumEventCount = maximumEventCount;
    }

    public int getMinimumAddressCount() {
        return minimumAddressCount;
    }

    public void setMinimumAddressCount(int minimumAddressCount) {
        this.minimumAddressCount = minimumAddressCount;
    }

    public int getMaximumAddressCount() {
        return maximumAddressCount;
    }

    public void setMaximumAddressCount(int maximumAddressCount) {
        this.maximumAddressCount = maximumAddressCount;
    }
}
