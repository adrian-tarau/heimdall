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

    private int batchSize = 100;
    private Duration batchInterval = ofSeconds(5);
}
