package net.microfalx.heimdall.protocol.smtp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("heimdall.protocol.smtp")
@Getter
@Setter
public class SmtpProperties {

    /**
     * The host name of the SMTP server.
     */
    private int port = 2525;

    /**
     * Whether the SMTP server requires TLS.
     */
    private boolean requireTLS;

    /**
     * Whether the SNMP simulator is enabled.
     */
    private boolean simulatorEnabled;

    private int maxMessageSize = 10_000_000;
    private int connectionTimeout = 5000;
    private int maxConnections = 20;
    private int maxRecipients = 20;
}
