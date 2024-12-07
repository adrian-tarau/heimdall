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

    private int historySize = 100;
    private boolean self;
    private float apdexExcelent = 0.95f;
    private float apdexGood = 0.85f;
    private float apdexFair = 0.7f;
    private float apdexPoor = 0.5f;
}
