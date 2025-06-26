package net.microfalx.heimdall.protocol.syslog;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("heimdall.syslog")
@Getter
@Setter
public class SyslogProperties {

    private int udpPort = 2514;

    private int tcpPort = 2601;
}
