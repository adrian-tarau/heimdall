package net.microfalx.heimdall.rest.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("heimdall.rest")
@Getter
@Setter
public class RestProperties {
}
