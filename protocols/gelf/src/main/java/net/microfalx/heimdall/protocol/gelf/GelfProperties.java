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

    /**
     * The UDP port on which the GELF agent listens.
     */
    private int udpPort = 12201;

    /**
     * The TCP port on which the GELF agent listens.
     */
    private int tcpPort = 12200;

    /**
     * Whether the SNMP simulator is enabled.
     */
    private boolean simulatorEnabled;
}
