package net.microfalx.heimdall.infrastructure.ping;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("heimdall.infrustructure.ping")
@Getter
@Setter
public class PingProperties {

    private int threads = 20;
}
