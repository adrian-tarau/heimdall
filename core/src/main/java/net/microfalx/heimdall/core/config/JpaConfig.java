package net.microfalx.heimdall.core.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories("net.microfalx.heimdall")
@EntityScan("net.microfalx.heimdall")
@Configuration("heimdall-jpa")
public class JpaConfig {
}
