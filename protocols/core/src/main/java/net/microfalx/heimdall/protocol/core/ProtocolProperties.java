package net.microfalx.heimdall.protocol.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static java.time.Duration.ofSeconds;

@Configuration
@ConfigurationProperties("heimdall.protocol")
public class ProtocolProperties {

    private int batchSize = 100;
    private Duration batchInterval = ofSeconds(5);

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public Duration getBatchInterval() {
        return batchInterval;
    }

    public void setBatchInterval(Duration batchInterval) {
        this.batchInterval = batchInterval;
    }
}
