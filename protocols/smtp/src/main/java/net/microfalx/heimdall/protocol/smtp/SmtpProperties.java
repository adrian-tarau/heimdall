package net.microfalx.heimdall.protocol.smtp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("heimdall.smtp")
public class SmtpProperties {

    private int port = 2525;
    private boolean requireTLS;
    private int maxMessageSize = 10_000_000;
    private int connectionTimeout = 5000;
    private int maxConnections = 20;
    private int maxRecipients = 20;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isRequireTLS() {
        return requireTLS;
    }

    public void setRequireTLS(boolean requireTLS) {
        this.requireTLS = requireTLS;
    }

    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    public void setMaxMessageSize(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getMaxRecipients() {
        return maxRecipients;
    }

    public void setMaxRecipients(int maxRecipients) {
        this.maxRecipients = maxRecipients;
    }
}
