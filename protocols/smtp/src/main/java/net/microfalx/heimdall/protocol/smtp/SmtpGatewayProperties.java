package net.microfalx.heimdall.protocol.smtp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("heimdall.smtp.gateway")
@Getter
@Setter
public class SmtpGatewayProperties {

    private String host = "localhost";
    private int port = 25;
    private boolean tls;
    private String userName;
    private String password;

}
