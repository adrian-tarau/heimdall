package net.microfalx.heimdall.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ComponentScan({"net.microfalx.bootstrap", "net.microfalx.heimdall"})
@EnableJpaRepositories({"net.microfalx.bootstrap", "net.microfalx.heimdall"})
@EntityScan({"net.microfalx.bootstrap", "net.microfalx.heimdall"})
@EnableTransactionManagement
public class HeimdallApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeimdallApplication.class, args);
    }
}
