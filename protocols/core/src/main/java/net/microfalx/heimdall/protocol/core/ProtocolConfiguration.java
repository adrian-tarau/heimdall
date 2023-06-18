package net.microfalx.heimdall.protocol.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ScheduledThreadPoolExecutor;

@Configuration
public class ProtocolConfiguration {

    @Bean("protocol-executor")
    public ThreadPoolTaskScheduler getTaskExecutor() {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setThreadNamePrefix("heimdall-protocol");
        executor.initialize();
        ScheduledThreadPoolExecutor poolExecutor = executor.getScheduledThreadPoolExecutor();
        poolExecutor.setCorePoolSize(5);
        poolExecutor.setMaximumPoolSize(10);
        return executor;
    }
}
