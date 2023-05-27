package net.microfalx.heimdall.protocol.smtp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("bootstrap.resource")
public class SmtpConfiguration {

    @Value("2525")
    private int port;

    @Value("true")
    private boolean requireTLS;

    @Value("10000000")
    private int maxMessageSize;

    @Value("5000")
    private int connectionTimeout;

    @Value("20")
    private int maxConnections;

    @Value("20")
    private int maxRecipients;

    public int getPort() {
        return port;
    }

    public boolean isRequireTLS() {
        return requireTLS;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public int getMaxRecipients() {
        return maxRecipients;
    }
}
