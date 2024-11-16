package net.microfalx.heimdall.rest.core;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.core.async.AsynchronousProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("heimdall.rest")
@Getter
@Setter
public class RestProperties {

    private AsynchronousProperties scheduler = new AsynchronousProperties().setSuffix("rest").setCoreThreads(20)
            .setMaximumThreads(30);

    private boolean self;
}
