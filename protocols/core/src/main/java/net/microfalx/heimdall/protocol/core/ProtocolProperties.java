package net.microfalx.heimdall.protocol.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static java.time.Duration.ofSeconds;

@Configuration
@ConfigurationProperties("heimdall.protocol")
@Getter
@Setter
public class ProtocolProperties {

    /**
     * The size of a batch of events to be processed at once.
     */
    private int batchSize = 100;

    /**
     * The interval at which batches of events are processed.
     */
    private Duration batchInterval = ofSeconds(5);
}
