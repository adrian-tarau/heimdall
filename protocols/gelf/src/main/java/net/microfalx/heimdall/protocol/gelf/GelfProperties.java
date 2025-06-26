package net.microfalx.heimdall.protocol.gelf;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("heimdall.protocol.gelf")
@Getter
@Setter
public class GelfProperties {

    private int udpPort = 12201;

    private int tcpPort = 12200;
}
