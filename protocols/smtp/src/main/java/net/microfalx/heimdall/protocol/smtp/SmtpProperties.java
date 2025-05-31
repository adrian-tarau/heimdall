package net.microfalx.heimdall.protocol.smtp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("heimdall.smtp")
@Getter
@Setter
public class SmtpProperties {

    private int port = 2525;
    private boolean requireTLS;
    private int maxMessageSize = 10_000_000;
    private int connectionTimeout = 5000;
    private int maxConnections = 20;
    private int maxRecipients = 20;
}
